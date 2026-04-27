package com.ems.employeeservice.employee.dto.request;

import com.ems.employeeservice.employee.enums.EmployeeStatus;
import jakarta.validation.constraints.NotNull;

public record EmployeeStatusUpdateRequest(
        @NotNull(message = "Status is required")
        EmployeeStatus status
) {
}