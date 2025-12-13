package com.ems.employeeservice.employee.repository;

import com.ems.employeeservice.employee.Employee;
import com.ems.employeeservice.employee.dto.request.GetEmployeesParamDto;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class EmployeeSpecification {
  public static Specification<Employee> filter(GetEmployeesParamDto dto, EmployeeRole role) {
    return (root, query, cb) -> {
      var predicate = cb.conjunction();

//      Keyword filter
      if (dto.keyword() != null && !dto.keyword().isBlank()) {
        String pattern = "%" + dto.keyword().toLowerCase() + "%";
        predicate = cb.and(predicate, cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)
        ));
      }

      // status filter
      if (dto.status() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("status"), dto.status()));
      }

      // role filter
      if (dto.role() != null && role == EmployeeRole.ADMIN) {
        predicate = cb.and(predicate, cb.equal(root.get("role"), dto.role()));
      }

      return predicate;
    };
  }

  public static Specification<Employee> excludeEmployee(UUID id) {
    return (root, query, cb) -> cb.notEqual(root.get("id"), id);
  }

  public static Specification<Employee> sameDepartment(UUID departmentId) {
    return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
  }
}
