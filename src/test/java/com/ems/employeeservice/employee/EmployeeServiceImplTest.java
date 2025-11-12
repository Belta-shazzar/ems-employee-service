package com.ems.employeeservice.employee;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.DepartmentRepository;
import com.ems.employeeservice.employee.dto.AuthServiceEmployeeResponse;
import com.ems.employeeservice.employee.dto.EmployeeRequest;
import com.ems.employeeservice.employee.dto.EmployeeResponse;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import com.ems.employeeservice.employee.service.EmployeeServiceImpl;
import com.ems.employeeservice.event.EmployeeCreatedEvent;
import com.ems.employeeservice.exception.custom.ConflictException;
import com.ems.employeeservice.exception.custom.ResourceNotFoundException;
import com.ems.employeeservice.kafka.EmployeeEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Unit Tests")
class EmployeeServiceImplTest {

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EmployeeRepository employeeRepository;

  @Mock
  private DepartmentRepository departmentRepository;

  @Mock
  private EmployeeEventProducer employeeEventProducer;

  @InjectMocks
  private EmployeeServiceImpl employeeService;

  private Department testDepartment;
  private Employee testEmployee;
  private EmployeeRequest testRequest;

  @BeforeEach
  void setUp() {
    testDepartment = Department.builder()
            .id(UUID.randomUUID())
            .name("Engineering")
            .build();

    testEmployee = Employee.builder()
            .id(UUID.randomUUID())
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .password("$2a$10$encodedPassword")
            .role(EmployeeRole.EMPLOYEE)
            .status(EmployeeStatus.ACTIVE)
            .department(testDepartment)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    testRequest = new EmployeeRequest(
            "John",
            "Doe",
            "john.doe@example.com",
            "password123",
            EmployeeRole.EMPLOYEE,
            testDepartment.getId()
    );
  }

  @Nested
  @DisplayName("Create Employee Tests")
  class CreateEmployeeTests {

    @Test
    @DisplayName("Should create employee successfully")
    void shouldCreateEmployeeSuccessfully() {
      // Given
      when(employeeRepository.existsByEmail(testRequest.email())).thenReturn(false);
      when(departmentRepository.findById(testDepartment.getId()))
              .thenReturn(Optional.of(testDepartment));
      when(passwordEncoder.encode("password123"))
              .thenReturn("$2a$10$encodedPassword");
      when(employeeRepository.save(any(Employee.class)))
              .thenReturn(testEmployee);

      // When
      EmployeeResponse response = employeeService.createEmployee(testRequest);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getFirstName()).isEqualTo("John");
      assertThat(response.getLastName()).isEqualTo("Doe");
      assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
      assertThat(response.getRole()).isEqualTo(EmployeeRole.EMPLOYEE);
      assertThat(response.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

      verify(employeeRepository).existsByEmail(testRequest.email());
      verify(departmentRepository).findById(testDepartment.getId());
      verify(passwordEncoder).encode("password123");
      verify(employeeRepository).save(any(Employee.class));
      verify(employeeEventProducer).publishEmployeeCreatedEvent(any(EmployeeCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when employee email already exists")
    void shouldThrowConflictExceptionWhenEmailExists() {
      // Given
      when(employeeRepository.existsByEmail(testRequest.email())).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> employeeService.createEmployee(testRequest))
              .isInstanceOf(ConflictException.class)
              .hasMessage("Employee with email already exists");

      verify(employeeRepository).existsByEmail(testRequest.email());
      verify(departmentRepository, never()).findById(any());
      verify(employeeRepository, never()).save(any());
      verify(employeeEventProducer, never()).publishEmployeeCreatedEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when department not found during employee creation")
    void shouldThrowExceptionWhenDepartmentNotFound() {
      // Given
      when(employeeRepository.existsByEmail(testRequest.email())).thenReturn(false);
      when(departmentRepository.findById(testDepartment.getId()))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.createEmployee(testRequest))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Department not found");

      verify(employeeRepository).existsByEmail(testRequest.email());
      verify(departmentRepository).findById(testDepartment.getId());
      verify(employeeRepository, never()).save(any());
      verify(employeeEventProducer, never()).publishEmployeeCreatedEvent(any());
    }

    @Test
    @DisplayName("Should publish Kafka event after creating employee")
    void shouldPublishKafkaEventAfterCreatingEmployee() {
      // Given
      when(employeeRepository.existsByEmail(testRequest.email())).thenReturn(false);
      when(departmentRepository.findById(testDepartment.getId()))
              .thenReturn(Optional.of(testDepartment));
      when(passwordEncoder.encode("password123"))
              .thenReturn("$2a$10$encodedPassword");
      when(employeeRepository.save(any(Employee.class)))
              .thenReturn(testEmployee);

      ArgumentCaptor<EmployeeCreatedEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeCreatedEvent.class);

      // When
      employeeService.createEmployee(testRequest);

      // Then
      verify(employeeEventProducer).publishEmployeeCreatedEvent(eventCaptor.capture());
      EmployeeCreatedEvent capturedEvent = eventCaptor.getValue();
      assertThat(capturedEvent.getEmployeeId()).isEqualTo(testEmployee.getId());
      assertThat(capturedEvent.getEmail()).isEqualTo(testEmployee.getEmail());
      assertThat(capturedEvent.getFirstName()).isEqualTo(testEmployee.getFirstName());
      assertThat(capturedEvent.getLastName()).isEqualTo(testEmployee.getLastName());
    }
  }

  @Nested
  @DisplayName("Update Employee Tests")
  class UpdateEmployeeTests {

    @Test
    @DisplayName("Should update employee successfully")
    void shouldUpdateEmployeeSuccessfully() {
      // Given
      UUID employeeId = testEmployee.getId();
      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.of(testEmployee));
      when(departmentRepository.findById(testDepartment.getId()))
              .thenReturn(Optional.of(testDepartment));
      when(employeeRepository.save(any(Employee.class)))
              .thenReturn(testEmployee);

      // When
      EmployeeResponse response = employeeService.updateEmployee(employeeId, testRequest);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(employeeId);
      verify(employeeRepository).findById(employeeId);
      verify(departmentRepository).findById(testDepartment.getId());
      verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should update employee without changing department when departmentId is null")
    void shouldUpdateEmployeeWithoutChangingDepartment() {
      // Given
      UUID employeeId = testEmployee.getId();
      EmployeeRequest requestWithoutDept = new EmployeeRequest(
              "Jane",
              "Smith",
              "jane.smith@example.com",
              "password123",
              EmployeeRole.MANAGER,
              null
      );

      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.of(testEmployee));
      when(employeeRepository.save(any(Employee.class)))
              .thenReturn(testEmployee);

      // When
      EmployeeResponse response = employeeService.updateEmployee(employeeId, requestWithoutDept);

      // Then
      assertThat(response).isNotNull();
      verify(employeeRepository).findById(employeeId);
      verify(departmentRepository, never()).findById(any());
      verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when employee not found during update")
    void shouldThrowExceptionWhenEmployeeNotFoundDuringUpdate() {
      // Given
      UUID employeeId = UUID.randomUUID();
      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, testRequest))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Employee not found");

      verify(employeeRepository).findById(employeeId);
      verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when department not found during update")
    void shouldThrowExceptionWhenDepartmentNotFoundDuringUpdate() {
      // Given
      UUID employeeId = testEmployee.getId();
      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.of(testEmployee));
      when(departmentRepository.findById(testDepartment.getId()))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, testRequest))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Department not found");

      verify(employeeRepository).findById(employeeId);
      verify(departmentRepository).findById(testDepartment.getId());
      verify(employeeRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Delete Employee Tests")
  class DeleteEmployeeTests {

    @Test
    @DisplayName("Should delete employee successfully")
    void shouldDeleteEmployeeSuccessfully() {
      // Given
      UUID employeeId = testEmployee.getId();
      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.of(testEmployee));

      // When
      employeeService.deleteEmployee(employeeId);

      // Then
      verify(employeeRepository).findById(employeeId);
      verify(employeeRepository).delete(testEmployee);
    }

    @Test
    @DisplayName("Should throw exception when employee not found during deletion")
    void shouldThrowExceptionWhenEmployeeNotFoundDuringDeletion() {
      // Given
      UUID employeeId = UUID.randomUUID();
      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Employee not found");

      verify(employeeRepository).findById(employeeId);
      verify(employeeRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("Get Employee By ID Tests")
  class GetEmployeeByIdTests {

    @Test
    @DisplayName("Should get employee by ID successfully without manager")
    void shouldGetEmployeeByIdSuccessfully() {
      // Given
      UUID employeeId = testEmployee.getId();
      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.of(testEmployee));

      // When
      EmployeeResponse response = employeeService.getEmployeeById(employeeId, null);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(employeeId);
      assertThat(response.getEmail()).isEqualTo(testEmployee.getEmail());
      verify(employeeRepository).findById(employeeId);
      verify(employeeRepository, never()).findAllByIdAndDepartmentId(any(), any());
    }

    @Test
    @DisplayName("Should get employee by ID with manager validation")
    void shouldGetEmployeeByIdWithManagerValidation() {
      // Given
      UUID employeeId = testEmployee.getId();
      UUID managerId = UUID.randomUUID();

      Employee manager = Employee.builder()
              .id(managerId)
              .firstName("Manager")
              .lastName("User")
              .email("manager@example.com")
              .role(EmployeeRole.MANAGER)
              .department(testDepartment)
              .build();

      when(employeeRepository.findById(managerId))
              .thenReturn(Optional.of(manager));
      when(employeeRepository.findAllByIdAndDepartmentId(employeeId, testDepartment.getId()))
              .thenReturn(Optional.of(testEmployee));

      // When
      EmployeeResponse response = employeeService.getEmployeeById(employeeId, managerId);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(employeeId);
      verify(employeeRepository).findById(managerId);
      verify(employeeRepository).findAllByIdAndDepartmentId(employeeId, testDepartment.getId());
    }

    @Test
    @DisplayName("Should throw exception when employee not found by ID")
    void shouldThrowExceptionWhenEmployeeNotFoundById() {
      // Given
      UUID employeeId = UUID.randomUUID();
      when(employeeRepository.findById(employeeId))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.getEmployeeById(employeeId, null))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Employee not found");

      verify(employeeRepository).findById(employeeId);
    }

    @Test
    @DisplayName("Should throw exception when manager not found")
    void shouldThrowExceptionWhenManagerNotFound() {
      // Given
      UUID employeeId = testEmployee.getId();
      UUID managerId = UUID.randomUUID();

      when(employeeRepository.findById(managerId))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.getEmployeeById(employeeId, managerId))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Manager not found");

      verify(employeeRepository).findById(managerId);
      verify(employeeRepository, never()).findAllByIdAndDepartmentId(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when employee not in manager's department")
    void shouldThrowExceptionWhenEmployeeNotInManagerDepartment() {
      // Given
      UUID employeeId = testEmployee.getId();
      UUID managerId = UUID.randomUUID();

      Employee manager = Employee.builder()
              .id(managerId)
              .firstName("Manager")
              .lastName("User")
              .email("manager@example.com")
              .role(EmployeeRole.MANAGER)
              .department(testDepartment)
              .build();

      when(employeeRepository.findById(managerId))
              .thenReturn(Optional.of(manager));
      when(employeeRepository.findAllByIdAndDepartmentId(employeeId, testDepartment.getId()))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.getEmployeeById(employeeId, managerId))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Employee not found");

      verify(employeeRepository).findById(managerId);
      verify(employeeRepository).findAllByIdAndDepartmentId(employeeId, testDepartment.getId());
    }
  }

  @Nested
  @DisplayName("Get All Employees Tests")
  class GetAllEmployeesTests {

    @Test
    @DisplayName("Should get all employees for admin (excluding admin)")
    void shouldGetAllEmployeesForAdmin() {
      // Given
      UUID adminId = UUID.randomUUID();
      Employee admin = Employee.builder()
              .id(adminId)
              .firstName("Admin")
              .lastName("User")
              .email("admin@example.com")
              .role(EmployeeRole.ADMIN)
              .department(testDepartment)
              .build();

      Employee employee2 = Employee.builder()
              .id(UUID.randomUUID())
              .firstName("Jane")
              .lastName("Smith")
              .email("jane@example.com")
              .role(EmployeeRole.EMPLOYEE)
              .department(testDepartment)
              .build();

      List<Employee> employees = Arrays.asList(testEmployee, employee2);

      when(employeeRepository.findById(adminId))
              .thenReturn(Optional.of(admin));
      when(employeeRepository.findByIdNot(adminId))
              .thenReturn(employees);

      // When
      List<EmployeeResponse> responses = employeeService.getAllEmployees(adminId);

      // Then
      assertThat(responses).isNotNull();
      assertThat(responses).hasSize(2);
      verify(employeeRepository).findById(adminId);
      verify(employeeRepository).findByIdNot(adminId);
      verify(employeeRepository, never()).findByDepartmentIdAndIdNot(any(), any());
    }

    @Test
    @DisplayName("Should get department employees for manager (excluding manager)")
    void shouldGetDepartmentEmployeesForManager() {
      // Given
      UUID managerId = UUID.randomUUID();
      Employee manager = Employee.builder()
              .id(managerId)
              .firstName("Manager")
              .lastName("User")
              .email("manager@example.com")
              .role(EmployeeRole.MANAGER)
              .department(testDepartment)
              .build();

      List<Employee> employees = Arrays.asList(testEmployee);

      when(employeeRepository.findById(managerId))
              .thenReturn(Optional.of(manager));
      when(employeeRepository.findByDepartmentIdAndIdNot(testDepartment.getId(), managerId))
              .thenReturn(employees);

      // When
      List<EmployeeResponse> responses = employeeService.getAllEmployees(managerId);

      // Then
      assertThat(responses).isNotNull();
      assertThat(responses).hasSize(1);
      assertThat(responses.get(0).getId()).isEqualTo(testEmployee.getId());
      verify(employeeRepository).findById(managerId);
      verify(employeeRepository).findByDepartmentIdAndIdNot(testDepartment.getId(), managerId);
      verify(employeeRepository, never()).findByIdNot(any());
    }

    @Test
    @DisplayName("Should throw exception when requester not found")
    void shouldThrowExceptionWhenRequesterNotFound() {
      // Given
      UUID requesterId = UUID.randomUUID();
      when(employeeRepository.findById(requesterId))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.getAllEmployees(requesterId))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Employee not found");

      verify(employeeRepository).findById(requesterId);
    }
  }

  @Nested
  @DisplayName("Get Employee By Email Tests")
  class GetEmployeeByEmailTests {

    @Test
    @DisplayName("Should get employee by email successfully")
    void shouldGetEmployeeByEmailSuccessfully() {
      // Given
      String email = "john.doe@example.com";
      when(employeeRepository.findByEmail(email))
              .thenReturn(Optional.of(testEmployee));

      // When
      AuthServiceEmployeeResponse response = employeeService.getEmployeeByEmail(email);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.email()).isEqualTo(email);
      assertThat(response.id()).isEqualTo(testEmployee.getId());
      assertThat(response.password()).isEqualTo(testEmployee.getPassword());
      assertThat(response.status()).isEqualTo(testEmployee.getStatus());
      assertThat(response.role()).isEqualTo(testEmployee.getRole());
      verify(employeeRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when employee not found by email")
    void shouldThrowExceptionWhenEmployeeNotFoundByEmail() {
      // Given
      String email = "notfound@example.com";
      when(employeeRepository.findByEmail(email))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> employeeService.getEmployeeByEmail(email))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Employee not found with email");

      verify(employeeRepository).findByEmail(email);
    }
  }
}