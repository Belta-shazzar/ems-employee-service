package com.ems.employeeservice.employee.service;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.service.DepartmentService;
import com.ems.employeeservice.employee.Employee;
import com.ems.employeeservice.employee.dto.request.EmployeeStatusUpdateRequest;
import com.ems.employeeservice.employee.dto.response.DashboardDataStatResponseDto;
import com.ems.employeeservice.employee.dto.response.StringResponse;
import com.ems.employeeservice.employee.dto.response.paginated.PagedResponse;
import com.ems.employeeservice.employee.repository.EmployeeRepository;
import com.ems.employeeservice.employee.dto.request.GetEmployeesParamDto;
import com.ems.employeeservice.employee.dto.response.AuthServiceEmployeeResponse;
import com.ems.employeeservice.employee.dto.request.EmployeeRequest;
import com.ems.employeeservice.employee.dto.response.EmployeeResponse;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import com.ems.employeeservice.employee.repository.EmployeeSpecification;
import com.ems.employeeservice.event.EmployeeCreatedEvent;
import com.ems.employeeservice.event.EmployeeStatusUpdateEvent;
import com.ems.employeeservice.exception.custom.ConflictException;
import com.ems.employeeservice.exception.custom.ResourceNotFoundException;
import com.ems.employeeservice.kafka.EmployeeEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

  private final PasswordEncoder passwordEncoder;
  private final EmployeeRepository employeeRepository;
  private final DepartmentService departmentService;
  private final EmployeeEventProducer employeeEventProducer;

  @Override
  @Transactional
  public EmployeeResponse createEmployee(EmployeeRequest request) {
    log.info("Creating employee with email: {}", request.email());
    boolean exists = employeeRepository.existsByEmail(request.email());
    if (exists) throw new ConflictException("Employee with email already exists");

    Department department = departmentService.getDepartmentById(request.departmentId());


    Employee employee = Employee.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .password(passwordEncoder.encode("password123"))
            .role(request.role())
            .status(EmployeeStatus.ACTIVE)
            .department(department)
            .build();

    Employee savedEmployee = employeeRepository.save(employee);
    log.info("Employee created successfully with id: {}", savedEmployee.getId());
    System.out.println("The creation date: " + savedEmployee.getCreatedAt());

    // Publish Kafka event
    EmployeeCreatedEvent event = EmployeeCreatedEvent.builder()
            .employeeId(savedEmployee.getId())
            .firstName(savedEmployee.getFirstName())
            .lastName(savedEmployee.getLastName())
            .email(savedEmployee.getEmail())
            .createdAt(LocalDateTime.now())
            .build();

    employeeEventProducer.publishEmployeeCreatedEvent(event);

    return mapToResponse(savedEmployee);
  }

  @Override
  @Transactional
  public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
    log.info("Updating employee with id: {}", id);

    Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

    employee.setFirstName(request.firstName());
    employee.setLastName(request.lastName());
    employee.setEmail(request.email());
    employee.setRole(request.role());

    if (request.departmentId() != null) {
      Department department = departmentService.getDepartmentById(request.departmentId());
      employee.setDepartment(department);
    }

    Employee updatedEmployee = employeeRepository.save(employee);
    log.info("Employee updated successfully with id: {}", updatedEmployee.getId());

    return mapToResponse(updatedEmployee);
  }

  @Override
  @Transactional
  public void deleteEmployee(UUID id) {
    log.info("Deleting employee with id: {}", id);

    Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

    employeeRepository.delete(employee);
    log.info("Employee deleted successfully with id: {}", id);
  }

  @Override
  @Transactional(readOnly = true)
  public EmployeeResponse getEmployeeById(UUID employeeId, UUID managerId) {
    log.info("Fetching employee with id: {}", employeeId);

    Employee employee;

    if (managerId != null) {
      Employee manager = employeeRepository.findById(managerId)
              .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));

      employee = employeeRepository.findAllByIdAndDepartmentId(employeeId, manager.getDepartment().getId())
              .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
    } else {
      employee = employeeRepository.findById(employeeId)
              .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
    }

    return mapToResponse(employee);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<EmployeeResponse> getAllEmployees(UUID requesterId, GetEmployeesParamDto dto) {
    Employee employee = employeeRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + requesterId));

    int pageIndex = Math.max(dto.page() - 1, 0);
    // Paging
    Pageable pageable = PageRequest.of(
            pageIndex,
            dto.size(),
            dto.orderBy().toSort(dto.sortBy())
    );

    // Base specification from dto
    Specification<Employee> spec = EmployeeSpecification.filter(dto, employee.getRole())
            .and(EmployeeSpecification.excludeEmployee(employee.getId()));

    // Manager restriction: only see employees in same department
    if (employee.getRole() == EmployeeRole.MANAGER) {
      spec = spec.and(EmployeeSpecification.sameDepartment(
              employee.getDepartment().getId()
      ));
    }

    // Manager restriction: only see employees in same department
    if (dto.departmentId() != null && employee.getRole() != EmployeeRole.MANAGER) {
      Department department = departmentService.getDepartmentById(dto.departmentId());

      spec = spec.and(EmployeeSpecification.sameDepartment(
              department.getId()
      ));
    }

    Page<Employee> employees = employeeRepository.findAll(spec, pageable);

    Page<EmployeeResponse> mapped = employees.map(this::mapToResponse);

    return PagedResponse.from(mapped);
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardDataStatResponseDto getDashboardStatData(UUID requesterId) {
    long employeeCount;
    long employeeCountByActiveStatus;
    long employeeCountByPendingStatus;
    long activeDepartmentCount = 0;

    Employee employee = employeeRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + requesterId));

    if (employee.getRole() == EmployeeRole.ADMIN) {
      employeeCount = employeeRepository.countByIdNot(employee.getId());
      employeeCountByActiveStatus = employeeRepository
              .countByStatusAndIdNot(EmployeeStatus.ACTIVE, employee.getId());
      employeeCountByPendingStatus = employeeRepository
              .countByStatusAndIdNot(EmployeeStatus.PENDING, employee.getId());
      activeDepartmentCount = departmentService.getDepartmentsCountByStatus(true);
    } else {
//      Employee counts are based on the manager's department id
      UUID managerDepartmentId = employee.getDepartment().getId();
      employeeCount = employeeRepository
              .countByDepartmentIdAndIdNot(managerDepartmentId, employee.getId());
      employeeCountByActiveStatus = employeeRepository
              .countByStatusAndDepartmentIdAndIdNot(EmployeeStatus.ACTIVE, managerDepartmentId, employee.getId());
      employeeCountByPendingStatus = employeeRepository
              .countByStatusAndDepartmentIdAndIdNot(EmployeeStatus.PENDING, managerDepartmentId, employee.getId());
    }

    return new DashboardDataStatResponseDto(
            employeeCount,
            employeeCountByActiveStatus,
            employeeCountByPendingStatus,
            activeDepartmentCount
    );
  }

  @Override
  @Transactional(readOnly = true)
  public AuthServiceEmployeeResponse getEmployeeByEmail(String email) {
    log.info("Fetching employee with email: {}", email);

    Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

    return mapToAuthServiceResponse(employee);
  }

  @Override
  public StringResponse updateEmployeeStatus(UUID id, EmployeeStatusUpdateRequest updateRequestDto) {
    log.info("Updating employee status by id: {}", id);

    Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

    if (employee.getStatus() == updateRequestDto.status()) {
      throw new ConflictException("Employee is already in " + updateRequestDto.status().name() + " status");
    }

    employee.setStatus(updateRequestDto.status());
    employeeRepository.save(employee);
    log.info("Employee status updated successfully with id: {}", id);

    EmployeeStatusUpdateEvent event = EmployeeStatusUpdateEvent.builder()
            .employeeId(id)
            .firstName(employee.getFirstName())
            .email(employee.getEmail())
            .status(updateRequestDto.status())
            .build();

    employeeEventProducer.publishEmployeeStatusUpdateEvent(event);

    return new StringResponse("Employee status updated successfully");
  }

  private EmployeeResponse mapToResponse(Employee employee) {
    return EmployeeResponse.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .status(employee.getStatus())
            .role(employee.getRole())
            .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
            .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
            .createdAt(employee.getCreatedAt())
            .updatedAt(employee.getUpdatedAt())
            .build();
  }

  private AuthServiceEmployeeResponse mapToAuthServiceResponse(Employee employee) {
    return new AuthServiceEmployeeResponse(
            employee.getId(),
            employee.getEmail(),
            employee.getPassword(),
            employee.getStatus(),
            employee.getRole());
  }
}
