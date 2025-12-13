package com.ems.employeeservice.employee.dto.response.paginated;

public record PaginationMeta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}