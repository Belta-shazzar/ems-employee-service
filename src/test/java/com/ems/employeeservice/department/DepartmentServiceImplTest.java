package com.ems.employeeservice.department;

import com.ems.employeeservice.department.service.DepartmentServiceImpl;
import com.ems.employeeservice.department.dto.DepartmentRequest;
import com.ems.employeeservice.department.dto.DepartmentResponse;
import com.ems.employeeservice.exception.custom.ConflictException;
import com.ems.employeeservice.exception.custom.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Department Service Unit Tests")
class DepartmentServiceImplTest {

  @Mock
  private DepartmentRepository departmentRepository;

  @InjectMocks
  private DepartmentServiceImpl departmentService;

  private Department testDepartment;
  private DepartmentRequest testRequest;
  private static final String DEPARTMENT_NAME = "Engineering";
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    now = LocalDateTime.now();

    testDepartment = Department.builder()
            .id(UUID.randomUUID())
            .name(DEPARTMENT_NAME)
            .createdAt(now)
            .updatedAt(now)
            .build();

    testRequest = new DepartmentRequest(DEPARTMENT_NAME);
  }

  @Nested
  @DisplayName("Create Department Tests")
  class CreateDepartmentTests {

    @Test
    @DisplayName("Should create department successfully")
    void shouldCreateDepartmentSuccessfully() {
      // Given
      when(departmentRepository.existsByName(DEPARTMENT_NAME)).thenReturn(false);
      when(departmentRepository.save(any(Department.class))).thenReturn(testDepartment);

      // When
      DepartmentResponse response = departmentService.createDepartment(testRequest);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(testDepartment.getId());
      assertThat(response.name()).isEqualTo(DEPARTMENT_NAME);
      assertThat(response.createdAt()).isEqualTo(now);
      assertThat(response.updatedAt()).isEqualTo(now);

      verify(departmentRepository).existsByName(DEPARTMENT_NAME);
      verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when department name already exists")
    void shouldThrowConflictExceptionWhenNameExists() {
      // Given
      when(departmentRepository.existsByName(DEPARTMENT_NAME)).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> departmentService.createDepartment(testRequest))
              .isInstanceOf(ConflictException.class)
              .hasMessage("Department with name already exists");

      verify(departmentRepository).existsByName(DEPARTMENT_NAME);
      verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create department with different name")
    void shouldCreateDepartmentWithDifferentName() {
      // Given
      String newDepartmentName = "Human Resources";
      DepartmentRequest newRequest = new DepartmentRequest(newDepartmentName);
      Department newDepartment = Department.builder()
              .id(UUID.randomUUID())
              .name(newDepartmentName)
              .createdAt(now)
              .updatedAt(now)
              .build();

      when(departmentRepository.existsByName(newDepartmentName)).thenReturn(false);
      when(departmentRepository.save(any(Department.class))).thenReturn(newDepartment);

      // When
      DepartmentResponse response = departmentService.createDepartment(newRequest);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.name()).isEqualTo(newDepartmentName);

      verify(departmentRepository).existsByName(newDepartmentName);
      verify(departmentRepository).save(any(Department.class));
    }
  }

  @Nested
  @DisplayName("Update Department Tests")
  class UpdateDepartmentTests {

    @Test
    @DisplayName("Should update department successfully")
    void shouldUpdateDepartmentSuccessfully() {
      // Given
      UUID departmentId = testDepartment.getId();
      String updatedName = "Updated Engineering";
      DepartmentRequest updateRequest = new DepartmentRequest(updatedName);

      Department updatedDepartment = Department.builder()
              .id(departmentId)
              .name(updatedName)
              .createdAt(now)
              .updatedAt(LocalDateTime.now())
              .build();

      when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
      when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

      // When
      DepartmentResponse response = departmentService.updateDepartment(departmentId, updateRequest);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(departmentId);
      assertThat(response.name()).isEqualTo(updatedName);

      verify(departmentRepository).findById(departmentId);
      verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when department not found")
    void shouldThrowExceptionWhenDepartmentNotFound() {
      // Given
      UUID departmentId = UUID.randomUUID();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> departmentService.updateDepartment(departmentId, testRequest))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage("Department not found with id: " + departmentId);

      verify(departmentRepository).findById(departmentId);
      verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update department name correctly")
    void shouldUpdateDepartmentNameCorrectly() {
      // Given
      UUID departmentId = testDepartment.getId();
      String newName = "Finance";
      DepartmentRequest updateRequest = new DepartmentRequest(newName);

      when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
      when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
        Department dept = invocation.getArgument(0);
        assertThat(dept.getName()).isEqualTo(newName);
        return dept;
      });

      // When
      departmentService.updateDepartment(departmentId, updateRequest);

      // Then
      verify(departmentRepository).findById(departmentId);
      verify(departmentRepository).save(any(Department.class));
    }
  }

  @Nested
  @DisplayName("Delete Department Tests")
  class DeleteDepartmentTests {

    @Test
    @DisplayName("Should delete department successfully")
    void shouldDeleteDepartmentSuccessfully() {
      // Given
      UUID departmentId = testDepartment.getId();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
      doNothing().when(departmentRepository).delete(testDepartment);

      // When
      departmentService.deleteDepartment(departmentId);

      // Then
      verify(departmentRepository).findById(departmentId);
      verify(departmentRepository).delete(testDepartment);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when department not found during deletion")
    void shouldThrowExceptionWhenDepartmentNotFoundDuringDeletion() {
      // Given
      UUID departmentId = UUID.randomUUID();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> departmentService.deleteDepartment(departmentId))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage("Department not found with id: " + departmentId);

      verify(departmentRepository).findById(departmentId);
      verify(departmentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should verify correct department is deleted")
    void shouldVerifyCorrectDepartmentIsDeleted() {
      // Given
      UUID departmentId = testDepartment.getId();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));

      // When
      departmentService.deleteDepartment(departmentId);

      // Then
      verify(departmentRepository).findById(departmentId);
      verify(departmentRepository).delete(testDepartment);
      verifyNoMoreInteractions(departmentRepository);
    }
  }

  @Nested
  @DisplayName("Get Department By ID Tests")
  class GetDepartmentByIdTests {

    @Test
    @DisplayName("Should get department by ID successfully")
    void shouldGetDepartmentByIdSuccessfully() {
      // Given
      UUID departmentId = testDepartment.getId();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));

      // When
      DepartmentResponse response = departmentService.getDepartmentById(departmentId);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(departmentId);
      assertThat(response.name()).isEqualTo(DEPARTMENT_NAME);
      assertThat(response.createdAt()).isEqualTo(now);
      assertThat(response.updatedAt()).isEqualTo(now);

      verify(departmentRepository).findById(departmentId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when department not found by ID")
    void shouldThrowExceptionWhenDepartmentNotFoundById() {
      // Given
      UUID departmentId = UUID.randomUUID();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> departmentService.getDepartmentById(departmentId))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage("Department not found with id: " + departmentId);

      verify(departmentRepository).findById(departmentId);
    }

    @Test
    @DisplayName("Should return correct department data")
    void shouldReturnCorrectDepartmentData() {
      // Given
      UUID departmentId = testDepartment.getId();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));

      // When
      DepartmentResponse response = departmentService.getDepartmentById(departmentId);

      // Then
      assertThat(response.id()).isEqualTo(testDepartment.getId());
      assertThat(response.name()).isEqualTo(testDepartment.getName());
      assertThat(response.createdAt()).isEqualTo(testDepartment.getCreatedAt());
      assertThat(response.updatedAt()).isEqualTo(testDepartment.getUpdatedAt());

      verify(departmentRepository).findById(departmentId);
    }
  }

  @Nested
  @DisplayName("Get All Departments Tests")
  class GetAllDepartmentsTests {

    @Test
    @DisplayName("Should get all departments successfully")
    void shouldGetAllDepartmentsSuccessfully() {
      // Given
      Department department2 = Department.builder()
              .id(UUID.randomUUID())
              .name("Human Resources")
              .createdAt(now)
              .updatedAt(now)
              .build();

      Department department3 = Department.builder()
              .id(UUID.randomUUID())
              .name("Finance")
              .createdAt(now)
              .updatedAt(now)
              .build();

      List<Department> departments = Arrays.asList(testDepartment, department2, department3);
      when(departmentRepository.findAll()).thenReturn(departments);

      // When
      List<DepartmentResponse> responses = departmentService.getAllDepartments();

      // Then
      assertThat(responses).isNotNull();
      assertThat(responses).hasSize(3);
      assertThat(responses.get(0).name()).isEqualTo(DEPARTMENT_NAME);
      assertThat(responses.get(1).name()).isEqualTo("Human Resources");
      assertThat(responses.get(2).name()).isEqualTo("Finance");

      verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no departments exist")
    void shouldReturnEmptyListWhenNoDepartmentsExist() {
      // Given
      when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

      // When
      List<DepartmentResponse> responses = departmentService.getAllDepartments();

      // Then
      assertThat(responses).isNotNull();
      assertThat(responses).isEmpty();

      verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("Should return single department in list")
    void shouldReturnSingleDepartmentInList() {
      // Given
      List<Department> departments = Collections.singletonList(testDepartment);
      when(departmentRepository.findAll()).thenReturn(departments);

      // When
      List<DepartmentResponse> responses = departmentService.getAllDepartments();

      // Then
      assertThat(responses).isNotNull();
      assertThat(responses).hasSize(1);
      assertThat(responses.get(0).id()).isEqualTo(testDepartment.getId());
      assertThat(responses.get(0).name()).isEqualTo(testDepartment.getName());

      verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("Should map all department fields correctly")
    void shouldMapAllDepartmentFieldsCorrectly() {
      // Given
      List<Department> departments = Collections.singletonList(testDepartment);
      when(departmentRepository.findAll()).thenReturn(departments);

      // When
      List<DepartmentResponse> responses = departmentService.getAllDepartments();

      // Then
      assertThat(responses).hasSize(1);
      DepartmentResponse response = responses.get(0);
      assertThat(response.id()).isEqualTo(testDepartment.getId());
      assertThat(response.name()).isEqualTo(testDepartment.getName());
      assertThat(response.createdAt()).isEqualTo(testDepartment.getCreatedAt());
      assertThat(response.updatedAt()).isEqualTo(testDepartment.getUpdatedAt());

      verify(departmentRepository).findAll();
    }
  }

  @Nested
  @DisplayName("Mapping Tests")
  class MappingTests {

    @Test
    @DisplayName("Should map department to response correctly")
    void shouldMapDepartmentToResponseCorrectly() {
      // Given
      UUID departmentId = testDepartment.getId();
      when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));

      // When
      DepartmentResponse response = departmentService.getDepartmentById(departmentId);

      // Then
      assertThat(response.id()).isEqualTo(testDepartment.getId());
      assertThat(response.name()).isEqualTo(testDepartment.getName());
      assertThat(response.createdAt()).isEqualTo(testDepartment.getCreatedAt());
      assertThat(response.updatedAt()).isEqualTo(testDepartment.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null timestamps in mapping")
    void shouldHandleNullTimestampsInMapping() {
      // Given
      Department deptWithNullTimestamps = Department.builder()
              .id(UUID.randomUUID())
              .name("Test Department")
              .createdAt(null)
              .updatedAt(null)
              .build();

      when(departmentRepository.findById(deptWithNullTimestamps.getId()))
              .thenReturn(Optional.of(deptWithNullTimestamps));

      // When
      DepartmentResponse response = departmentService.getDepartmentById(deptWithNullTimestamps.getId());

      // Then
      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(deptWithNullTimestamps.getId());
      assertThat(response.name()).isEqualTo(deptWithNullTimestamps.getName());
      assertThat(response.createdAt()).isNull();
      assertThat(response.updatedAt()).isNull();
    }
  }
}