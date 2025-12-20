package com.ems.employeeservice.employee.dto.response;

public record DashboardDataStatResponseDto(
        long totalEmployees,
        long activeEmployees,
        long pendingEmployees,
        long activeDepartment
) {
}
