package com.ems.employeeservice.employee.enums;

import org.springframework.data.domain.Sort;

public enum Order {
  ASC,
  DESC;

  public Sort toSort(SortBy sortBy) {
    return this == ASC
            ? Sort.by(sortBy.getField()).ascending()
            : Sort.by(sortBy.getField()).descending();
  }
}
