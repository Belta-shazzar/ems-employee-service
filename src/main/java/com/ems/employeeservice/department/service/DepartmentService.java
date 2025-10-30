package com.ems.employeeservice.department.service;

import com.ems.employeeservice.department.dto.DepartmentRequest;
import com.ems.employeeservice.department.dto.DepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
  
  DepartmentResponse createDepartment(DepartmentRequest request);
  
  DepartmentResponse updateDepartment(UUID id, DepartmentRequest request);
  
  void deleteDepartment(UUID id);
  
  DepartmentResponse getDepartmentById(UUID id);
  
  List<DepartmentResponse> getAllDepartments();
}
