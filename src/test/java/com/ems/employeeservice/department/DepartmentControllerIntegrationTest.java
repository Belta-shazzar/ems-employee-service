package com.ems.employeeservice.department;

import com.ems.employeeservice.department.dto.DepartmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@EmbeddedKafka
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Department Controller Integration Tests")
class DepartmentControllerIntegrationTest {

  @Container
  @SuppressWarnings({"resource", "unused"})
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("test_db")
          .withUsername("test")
          .withPassword("test");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private DepartmentRepository departmentRepository;

  @Nested
  @DisplayName("POST /api/departments - Create Department")
  class CreateDepartmentTests {

    @Test
    @DisplayName("Should create department with admin role")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateDepartmentWithAdminRole() throws Exception {
      // Given
      DepartmentRequest request = new DepartmentRequest("Engineering");

      // When & Then
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.id").exists())
              .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    @DisplayName("Should return 409 when creating department with duplicate name")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenCreatingDepartmentWithDuplicateName() throws Exception {
      // Given
      departmentRepository.save(Department.builder().name("Engineering").build());
      DepartmentRequest request = new DepartmentRequest("Engineering");

      // When & Then
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to create department")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldReturn403WhenNonAdminTriesToCreateDepartment() throws Exception {
      // Given
      DepartmentRequest request = new DepartmentRequest("Engineering");

      // When & Then
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when manager tries to create department")
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenManagerTriesToCreateDepartment() throws Exception {
      // Given
      DepartmentRequest request = new DepartmentRequest("Engineering");

      // When & Then
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 when creating department with invalid data")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenCreatingDepartmentWithInvalidData() throws Exception {
      // Given
      DepartmentRequest request = new DepartmentRequest(null);

      // When & Then
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andDo(print())
              .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create multiple departments successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateMultipleDepartmentsSuccessfully() throws Exception {
      // Given
      DepartmentRequest request1 = new DepartmentRequest("Engineering");
      DepartmentRequest request2 = new DepartmentRequest("Human Resources");

      // When & Then
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request1)))
              .andDo(print())
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.name").value("Engineering"));

      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request2)))
              .andDo(print())
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.name").value("Human Resources"));
    }
  }

  @Nested
  @DisplayName("PUT /api/departments/{id} - Update Department")
  class UpdateDepartmentTests {

    @Test
    @DisplayName("Should update department with admin role")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateDepartmentWithAdminRole() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );
      DepartmentRequest updateRequest = new DepartmentRequest("Updated Engineering");

      // When & Then
      mockMvc.perform(put("/api/departments/{id}", department.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(department.getId().toString()))
              .andExpect(jsonPath("$.name").value("Updated Engineering"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent department")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUpdatingNonExistentDepartment() throws Exception {
      // Given
      UUID nonExistentId = UUID.randomUUID();
      DepartmentRequest updateRequest = new DepartmentRequest("Updated Name");

      // When & Then
      mockMvc.perform(put("/api/departments/{id}", nonExistentId)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andDo(print())
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to update department")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldReturn403WhenNonAdminTriesToUpdateDepartment() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );
      DepartmentRequest updateRequest = new DepartmentRequest("Updated Engineering");

      // When & Then
      mockMvc.perform(put("/api/departments/{id}", department.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andDo(print())
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when manager tries to update department")
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenManagerTriesToUpdateDepartment() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );
      DepartmentRequest updateRequest = new DepartmentRequest("Updated Engineering");

      // When & Then
      mockMvc.perform(put("/api/departments/{id}", department.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andDo(print())
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 when updating department with invalid data")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenUpdatingDepartmentWithInvalidData() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );
      DepartmentRequest updateRequest = new DepartmentRequest(null);

      // When & Then
      mockMvc.perform(put("/api/departments/{id}", department.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andDo(print())
              .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /api/departments/{id} - Delete Department")
  class DeleteDepartmentTests {

    @Test
    @DisplayName("Should delete department with admin role")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteDepartmentWithAdminRole() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );

      // When & Then
      mockMvc.perform(delete("/api/departments/{id}", department.getId()))
              .andDo(print())
              .andExpect(status().isNoContent());

      // Verify department is deleted
      mockMvc.perform(get("/api/departments/{id}", department.getId()))
              .andDo(print())
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent department")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenDeletingNonExistentDepartment() throws Exception {
      // Given
      UUID nonExistentId = UUID.randomUUID();

      // When & Then
      mockMvc.perform(delete("/api/departments/{id}", nonExistentId))
              .andDo(print())
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to delete department")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldReturn403WhenNonAdminTriesToDeleteDepartment() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );

      // When & Then
      mockMvc.perform(delete("/api/departments/{id}", department.getId()))
              .andDo(print())
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when manager tries to delete department")
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenManagerTriesToDeleteDepartment() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );

      // When & Then
      mockMvc.perform(delete("/api/departments/{id}", department.getId()))
              .andDo(print())
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /api/departments/{id} - Get Department By ID")
  class GetDepartmentByIdTests {

    @Test
    @DisplayName("Should get department by ID with admin role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetDepartmentByIdWithAdminRole() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );

      // When & Then
      mockMvc.perform(get("/api/departments/{id}", department.getId()))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(department.getId().toString()))
              .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    @DisplayName("Should return 404 when department not found")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenDepartmentNotFound() throws Exception {
      // Given
      UUID nonExistentId = UUID.randomUUID();

      // When & Then
      mockMvc.perform(get("/api/departments/{id}", nonExistentId))
              .andDo(print())
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to get department by ID")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldReturn403WhenNonAdminTriesToGetDepartmentById() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );

      // When & Then
      mockMvc.perform(get("/api/departments/{id}", department.getId()))
              .andDo(print())
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when manager tries to get department by ID")
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenManagerTriesToGetDepartmentById() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );

      // When & Then
      mockMvc.perform(get("/api/departments/{id}", department.getId()))
              .andDo(print())
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /api/departments - Get All Departments")
  class GetAllDepartmentsTests {

    @Test
    @DisplayName("Should get all departments with admin role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllDepartmentsWithAdminRole() throws Exception {
      // Given
      departmentRepository.save(Department.builder().name("Engineering").build());
      departmentRepository.save(Department.builder().name("Human Resources").build());
      departmentRepository.save(Department.builder().name("Finance").build());

      // When & Then
      mockMvc.perform(get("/api/departments"))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(3)))
              .andExpect(jsonPath("$[0].name").exists())
              .andExpect(jsonPath("$[1].name").exists())
              .andExpect(jsonPath("$[2].name").exists());
    }

    @Test
    @DisplayName("Should return empty list when no departments exist")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnEmptyListWhenNoDepartmentsExist() throws Exception {
      // When & Then
      mockMvc.perform(get("/api/departments"))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return single department in list")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnSingleDepartmentInList() throws Exception {
      // Given
      Department department = departmentRepository.save(
              Department.builder().name("Engineering").build()
      );

      // When & Then
      mockMvc.perform(get("/api/departments"))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].id").value(department.getId().toString()))
              .andExpect(jsonPath("$[0].name").value("Engineering"));
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to get all departments")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldReturn403WhenNonAdminTriesToGetAllDepartments() throws Exception {
      // When & Then
      mockMvc.perform(get("/api/departments"))
              .andDo(print())
              .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when manager tries to get all departments")
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenManagerTriesToGetAllDepartments() throws Exception {
      // When & Then
      mockMvc.perform(get("/api/departments"))
              .andDo(print())
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("End-to-End Flow Tests")
  class EndToEndFlowTests {

    @Test
    @DisplayName("Should complete full CRUD lifecycle")
    @WithMockUser(roles = "ADMIN")
    void shouldCompleteFullCRUDLifecycle() throws Exception {
      // Create
      DepartmentRequest createRequest = new DepartmentRequest("Engineering");
      String createResponse = mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(createRequest)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.name").value("Engineering"))
              .andReturn()
              .getResponse()
              .getContentAsString();

      String departmentId = objectMapper.readTree(createResponse).get("id").asText();

      // Read
      mockMvc.perform(get("/api/departments/{id}", departmentId))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.name").value("Engineering"));

      // Update
      DepartmentRequest updateRequest = new DepartmentRequest("Updated Engineering");
      mockMvc.perform(put("/api/departments/{id}", departmentId)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.name").value("Updated Engineering"));

      // Delete
      mockMvc.perform(delete("/api/departments/{id}", departmentId))
              .andExpect(status().isNoContent());

      // Verify deletion
      mockMvc.perform(get("/api/departments/{id}", departmentId))
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify department persistence across operations")
    @WithMockUser(roles = "ADMIN")
    void shouldVerifyDepartmentPersistenceAcrossOperations() throws Exception {
      // Create first department
      DepartmentRequest request1 = new DepartmentRequest("Engineering");
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request1)))
              .andExpect(status().isCreated());

      // Create second department
      DepartmentRequest request2 = new DepartmentRequest("Human Resources");
      mockMvc.perform(post("/api/departments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request2)))
              .andExpect(status().isCreated());

      // Verify both departments exist
      mockMvc.perform(get("/api/departments"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(2)));
    }
  }
}