package com.ems.employeeservice.department;

import com.ems.employeeservice.employee.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "departments",
        indexes = {
                @Index(name = "idx_department_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_department_name", columnNames = "name")
        }
)
@SQLDelete(sql = "UPDATE departments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Department {

  @Id
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false, name = "updated_at")
  private LocalDateTime updatedAt;

  @JsonIgnore
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @JsonIgnore
  @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
  private Set<Employee> employees;
}