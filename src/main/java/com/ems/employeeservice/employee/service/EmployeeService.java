package com.ems.employeeservice.employee.service;

import com.ems.employeeservice.employee.dto.AuthServiceEmployeeResponse;
import com.ems.employeeservice.employee.dto.EmployeeRequest;
import com.ems.employeeservice.employee.dto.EmployeeResponse;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
  
  EmployeeResponse createEmployee(EmployeeRequest request);
  
  EmployeeResponse updateEmployee(UUID id, EmployeeRequest request);
  
  void deleteEmployee(UUID id);
  
  EmployeeResponse getEmployeeById(UUID employeeId, UUID managerId);
  
  List<EmployeeResponse> getAllEmployees(UUID requesterId);
  
  AuthServiceEmployeeResponse getEmployeeByEmail(String email);
}
