package com.ems.employeeservice.exception.custom;

public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
