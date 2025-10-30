package com.ems.employeeservice.employee;

import com.ems.employeeservice.department.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
  boolean existsByEmail(String email);
  Optional<Employee> findByEmail(String email);
  Optional<Employee> findAllByIdAndDepartmentId(UUID employeeId, UUID departmentId);
  List<Employee> findByDepartmentIdAndIdNot(UUID departmentId, UUID managerId);
  List<Employee> findByIdNot(UUID managerId);

}
