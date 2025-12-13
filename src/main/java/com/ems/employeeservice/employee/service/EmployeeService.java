package com.ems.employeeservice.employee.service;

import com.ems.employeeservice.employee.dto.request.GetEmployeesParamDto;
import com.ems.employeeservice.employee.dto.response.AuthServiceEmployeeResponse;
import com.ems.employeeservice.employee.dto.request.EmployeeRequest;
import com.ems.employeeservice.employee.dto.response.EmployeeResponse;
import com.ems.employeeservice.employee.dto.response.paginated.PagedResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface EmployeeService {
  
  EmployeeResponse createEmployee(EmployeeRequest request);
  
  EmployeeResponse updateEmployee(UUID id, EmployeeRequest request);
  
  void deleteEmployee(UUID id);
  
  EmployeeResponse getEmployeeById(UUID employeeId, UUID managerId);

  PagedResponse<EmployeeResponse> getAllEmployees(UUID requesterId, GetEmployeesParamDto paramDto);
  
  AuthServiceEmployeeResponse getEmployeeByEmail(String email);
}
