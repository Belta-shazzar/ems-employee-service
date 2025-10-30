package com.ems.employeeservice.employee.service;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.DepartmentRepository;
import com.ems.employeeservice.employee.Employee;
import com.ems.employeeservice.employee.EmployeeRepository;
import com.ems.employeeservice.employee.dto.AuthServiceEmployeeResponse;
import com.ems.employeeservice.employee.dto.EmployeeRequest;
import com.ems.employeeservice.employee.dto.EmployeeResponse;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import com.ems.employeeservice.event.EmployeeCreatedEvent;
import com.ems.employeeservice.exception.custom.ConflictException;
import com.ems.employeeservice.exception.custom.ResourceNotFoundException;
import com.ems.employeeservice.kafka.EmployeeEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

  private final PasswordEncoder passwordEncoder;
  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final EmployeeEventProducer employeeEventProducer;

  @Override
  @Transactional
  public EmployeeResponse createEmployee(EmployeeRequest request) {
    log.info("Creating employee with email: {}", request.email());
    boolean exists = employeeRepository.existsByEmail(request.email());
    if (exists) throw new ConflictException("Employee with email already exists");

    Department department = departmentRepository.findById(request.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));


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

    // Publish Kafka event
    EmployeeCreatedEvent event = EmployeeCreatedEvent.builder()
            .employeeId(savedEmployee.getId())
            .firstName(savedEmployee.getFirstName())
            .lastName(savedEmployee.getLastName())
            .email(savedEmployee.getEmail())
            .createdAt(savedEmployee.getCreatedAt())
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
      Department department = departmentRepository.findById(request.departmentId())
              .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
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
  public List<EmployeeResponse> getAllEmployees(UUID requesterId) {
    Employee employee = employeeRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + requesterId));
    List<Employee> employees;


    if (employee.getRole() == EmployeeRole.MANAGER) {
      //      Fetch all except the Manager's id
      UUID departmentId = employee.getDepartment().getId();
      employees = employeeRepository.findByDepartmentIdAndIdNot(departmentId, employee.getId());
    } else {
      //      Fetch all except the Admin id
      employees = employeeRepository.findByIdNot(employee.getId());
    }


    return employees.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public AuthServiceEmployeeResponse getEmployeeByEmail(String email) {
    log.info("Fetching employee with email: {}", email);

    Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

    return mapToAuthServiceResponse(employee);
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
