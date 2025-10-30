package com.ems.employeeservice.employee;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.DepartmentRepository;
import com.ems.employeeservice.employee.dto.EmployeeRequest;
import com.ems.employeeservice.employee.dto.EmployeeResponse;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.service.EmployeeService;
import com.ems.employeeservice.exception.custom.ResourceNotFoundException;
import com.ems.employeeservice.kafka.EmployeeEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Unit Tests")
class EmployeeServiceTest {

  @Mock
  private EmployeeRepository employeeRepository;

  @Mock
  private DepartmentRepository departmentRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EmployeeEventProducer employeeEventProducer;

  @InjectMocks
  private EmployeeService employeeService;

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
            .password("encodedPassword")
            .role(EmployeeRole.EMPLOYEE)
            .department(testDepartment)
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

  @Test
  @DisplayName("Should create employee successfully")
  void shouldCreateEmployeeSuccessfully() {
    // Given
    when(departmentRepository.findById(testDepartment.getId()))
            .thenReturn(Optional.of(testDepartment));
    when(passwordEncoder.encode(testRequest.password()))
            .thenReturn("encodedPassword");
    when(employeeRepository.save(any(Employee.class)))
            .thenReturn(testEmployee);

    // When
    EmployeeResponse response = employeeService.createEmployee(testRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getFirstName()).isEqualTo("John");
    assertThat(response.getLastName()).isEqualTo("Doe");
    assertThat(response.getEmail()).isEqualTo("john.doe@example.com");

    verify(departmentRepository).findById(testDepartment.getId());
    verify(passwordEncoder).encode(testRequest.password());
    verify(employeeRepository).save(any(Employee.class));
    verify(employeeEventProducer).publishEmployeeCreatedEvent(any());
  }

  @Test
  @DisplayName("Should throw exception when department not found during employee creation")
  void shouldThrowExceptionWhenDepartmentNotFound() {
    // Given
    when(departmentRepository.findById(testDepartment.getId()))
            .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> employeeService.createEmployee(testRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Department not found");

    verify(departmentRepository).findById(testDepartment.getId());
    verify(employeeRepository, never()).save(any());
    verify(employeeEventProducer, never()).publishEmployeeCreatedEvent(any());
  }

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
    verify(employeeRepository).findById(employeeId);
    verify(departmentRepository).findById(testDepartment.getId());
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
  @DisplayName("Should get employee by ID successfully")
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
    verify(employeeRepository).findById(employeeId);
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
  @DisplayName("Should get employee by email successfully")
  void shouldGetEmployeeByEmailSuccessfully() {
    // Given
    String email = "john.doe@example.com";
    when(employeeRepository.findByEmail(email))
            .thenReturn(Optional.of(testEmployee));

    // When
    var response = employeeService.getEmployeeByEmail(email);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.email()).isEqualTo(email);
    verify(employeeRepository).findByEmail(email);
  }
}
