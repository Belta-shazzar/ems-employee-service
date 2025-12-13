package com.ems.employeeservice.employee.repository;

import com.ems.employeeservice.employee.Employee;
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
  Page<Employee> findByDepartmentIdAndIdNot(UUID departmentId, UUID managerId, Pageable pageable);
  Page<Employee> findByIdNot(UUID managerId, Pageable pageable);

}
