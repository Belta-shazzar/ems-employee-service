package com.ems.employeeservice.employee;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.DepartmentRepository;
import com.ems.employeeservice.employee.dto.EmployeeRequest;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"employee-created-topic"})
@Transactional
@DisplayName("Employee Controller Integration Tests")
class EmployeeControllerIntegrationTest {

  @Container
  @SuppressWarnings("resource")
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("test_db")
          .withUsername("test")
          .withPassword("test");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private EmployeeRepository employeeRepository;

  @Autowired
  private DepartmentRepository departmentRepository;

  private Department testDepartment;

  @BeforeEach
  void setUp() {
    employeeRepository.deleteAll();
    departmentRepository.deleteAll();

    testDepartment = departmentRepository.save(
            Department.builder()
                    .name("Engineering")
                    .build()
    );
  }

  @Test
  @DisplayName("Should create employee with admin role")
  @WithMockUser(roles = "ADMIN")
  void shouldCreateEmployeeWithAdminRole() throws Exception {
    EmployeeRequest request = new EmployeeRequest(
            "John",
            "Doe",
            "john.doe@example.com",
            "password123",
            EmployeeRole.EMPLOYEE,
            testDepartment.getId()
    );

    mockMvc.perform(post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));
  }

  @Test
  @DisplayName("Should return 403 when non-admin tries to create employee")
  @WithMockUser(roles = "EMPLOYEE")
  void shouldReturn403WhenNonAdminTriesToCreateEmployee() throws Exception {
    EmployeeRequest request = new EmployeeRequest(
            "John",
            "Doe",
            "john.doe@example.com",
            "password123",
            EmployeeRole.EMPLOYEE,
            testDepartment.getId()
    );

    mockMvc.perform(post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should get employee by ID with admin role")
  @WithMockUser(roles = "ADMIN")
  void shouldGetEmployeeByIdWithAdminRole() throws Exception {
    Employee employee = employeeRepository.save(
            Employee.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .password("encodedPassword")
                    .role(EmployeeRole.EMPLOYEE)
                    .department(testDepartment)
                    .build()
    );

    mockMvc.perform(get("/api/employees/{id}", employee.getId())
                    .header("X-Employee-Id", UUID.randomUUID().toString())
                    .header("X-Employee-Role", "ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Smith"));
  }

  @Test
  @DisplayName("Should return 404 when employee not found")
  @WithMockUser(roles = "ADMIN")
  void shouldReturn404WhenEmployeeNotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/api/employees/{id}", nonExistentId)
                    .header("X-Employee-Id", UUID.randomUUID().toString())
                    .header("X-Employee-Role", "ADMIN"))
            .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should update employee with admin role")
  @WithMockUser(roles = "ADMIN")
  void shouldUpdateEmployeeWithAdminRole() throws Exception {
    Employee employee = employeeRepository.save(
            Employee.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .password("encodedPassword")
                    .role(EmployeeRole.EMPLOYEE)
                    .department(testDepartment)
                    .build()
    );

    EmployeeRequest updateRequest = new EmployeeRequest(
            "Jane",
            "Doe",
            "jane.doe@example.com",
            "newPassword123",
            EmployeeRole.MANAGER,
            testDepartment.getId()
    );

    mockMvc.perform(put("/api/employees/{id}", employee.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.role").value("MANAGER"));
  }

  @Test
  @DisplayName("Should delete employee with admin role")
  @WithMockUser(roles = "ADMIN")
  void shouldDeleteEmployeeWithAdminRole() throws Exception {
    Employee employee = employeeRepository.save(
            Employee.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .password("encodedPassword")
                    .role(EmployeeRole.EMPLOYEE)
                    .department(testDepartment)
                    .build()
    );

    mockMvc.perform(delete("/api/employees/{id}", employee.getId()))
            .andExpect(status().isNoContent());
  }
}
