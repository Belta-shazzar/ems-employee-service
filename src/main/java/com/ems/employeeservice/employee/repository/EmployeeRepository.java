package com.ems.employeeservice.employee.repository;

import com.ems.employeeservice.employee.Employee;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {
  boolean existsByEmail(String email);
  Optional<Employee> findByEmail(String email);
  Optional<Employee> findAllByIdAndDepartmentId(UUID employeeId, UUID departmentId);
  long countByIdNot(UUID id);
  long countByDepartmentIdAndIdNot(UUID departmentId, UUID managerId);
  long countByStatusAndIdNot(EmployeeStatus status, UUID managerId);
  long countByStatusAndDepartmentIdAndIdNot(EmployeeStatus status, UUID departmentId, UUID managerId);
}
