package com.ems.employeeservice.department.service;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.dto.DepartmentRequest;
import com.ems.employeeservice.department.dto.DepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
  
  DepartmentResponse createDepartment(DepartmentRequest request);
  
  DepartmentResponse updateDepartment(UUID id, DepartmentRequest request);
  
  void deleteDepartment(UUID id);
  
  Department getDepartmentById(UUID id);
  
  List<DepartmentResponse> getAllDepartments();

  long getDepartmentsCountByStatus(boolean status);
}
