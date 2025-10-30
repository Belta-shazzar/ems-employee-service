package com.ems.employeeservice.event;

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
public class EmployeeCreatedEvent {

  private UUID employeeId;
  private String firstName;
  private String lastName;
  private String email;
  private LocalDateTime createdAt;
}
