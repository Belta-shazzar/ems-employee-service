package com.ems.employeeservice.event;

import com.ems.employeeservice.employee.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatusUpdateEvent {
  private UUID employeeId;
  private String firstName;
  private String email;
  private EmployeeStatus status;
}
