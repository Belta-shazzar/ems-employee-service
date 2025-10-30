package com.ems.employeeservice.employee;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees",
        indexes = {
                @Index(name = "idx_employee_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_email", columnNames = "email")
        })
@SQLDelete(sql = "UPDATE employees SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Employee {

  @Id
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @GeneratedValue
  private UUID id;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(nullable = false, length = 150)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EmployeeStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EmployeeRole role;

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false, name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_employee_department"))
  private Department department;
}