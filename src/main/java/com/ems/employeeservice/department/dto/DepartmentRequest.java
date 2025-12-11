package com.ems.employeeservice.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Department name is required")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        Boolean active
) {
}