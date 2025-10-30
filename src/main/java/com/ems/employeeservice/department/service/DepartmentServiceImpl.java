package com.ems.employeeservice.department.service;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.DepartmentRepository;
import com.ems.employeeservice.department.dto.DepartmentRequest;
import com.ems.employeeservice.department.dto.DepartmentResponse;
import com.ems.employeeservice.exception.custom.ConflictException;
import com.ems.employeeservice.exception.custom.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

  private final DepartmentRepository departmentRepository;

  @Override
  @Transactional
  public DepartmentResponse createDepartment(DepartmentRequest request) {
    log.info("Creating department with name: {}", request.name());
    boolean exists = departmentRepository.existsByName(request.name());

    if (exists) throw new ConflictException("Department with name already exists");

    Department department = Department.builder()
            .name(request.name())
            .build();

    Department savedDepartment = departmentRepository.save(department);
    log.info("Department created successfully with id: {}", savedDepartment.getId());

    return mapToResponse(savedDepartment);
  }

  @Override
  @Transactional
  public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {
    log.info("Updating department with id: {}", id);

    Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

    department.setName(request.name());

    Department updatedDepartment = departmentRepository.save(department);
    log.info("Department updated successfully with id: {}", updatedDepartment.getId());

    return mapToResponse(updatedDepartment);
  }

  @Override
  @Transactional
  public void deleteDepartment(UUID id) {
    log.info("Deleting department with id: {}", id);

    Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

    departmentRepository.delete(department);
    log.info("Department deleted successfully with id: {}", id);
  }

  @Override
  @Transactional(readOnly = true)
  public DepartmentResponse getDepartmentById(UUID id) {
    log.info("Fetching department with id: {}", id);

    Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

    return mapToResponse(department);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DepartmentResponse> getAllDepartments() {
    log.info("Fetching all departments");

    List<Department> departments = departmentRepository.findAll();

    return departments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  private DepartmentResponse mapToResponse(Department department) {
    return new DepartmentResponse(
            department.getId(),
            department.getName(),
            department.getCreatedAt(),
            department.getUpdatedAt());
  }
}
