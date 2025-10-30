package com.ems.employeeservice.employee.dto;

import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private EmployeeStatus status;
  private EmployeeRole role;
  private UUID departmentId;
  private String departmentName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
