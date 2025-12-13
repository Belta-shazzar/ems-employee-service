package com.ems.employeeservice.employee.enums;

public enum SortBy {
  first_name("firstName"),
  last_name("lastName"),
  email("email"),
  created_at("createdAt"),
  updated_at("updatedAt");

  private final String field;

  SortBy(String field) { this.field = field; }

  public String getField() {
    return field;
  }

}
