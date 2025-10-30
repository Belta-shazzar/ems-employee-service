package com.ems.employeeservice.employee.dto;

import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;

import java.util.UUID;

public record AuthServiceEmployeeResponse(
        UUID id,
        String email,
        String password,
        EmployeeStatus status,
        EmployeeRole role
) {}