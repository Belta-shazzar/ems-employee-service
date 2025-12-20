package com.ems.employeeservice.employee.dto.request;

import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import com.ems.employeeservice.employee.enums.Order;
import com.ems.employeeservice.employee.enums.SortBy;

import java.util.UUID;

public record GetEmployeesParamDto(
        String keyword,
        EmployeeStatus status,
        EmployeeRole role,
        SortBy sortBy,
        Order orderBy,
        Integer page,
        Integer size,
        Boolean paginate,
        UUID departmentId
) {
  /**
   * Compact constructor to apply defaults and protect against invalid input.
   */
  public GetEmployeesParamDto {
    // Default pagination
    if (page == null || page < 1) page = 1;
    if (size == null || size <= 0) size = 10;

    // Default sorting
    if (sortBy == null) sortBy = SortBy.created_at;
    if (orderBy == null) orderBy = Order.ASC;

    // keyword normalization
    if (keyword != null && keyword.isBlank()) {
      keyword = null;
    }
  }
}
