package com.ems.employeeservice.seeder;

import com.ems.employeeservice.department.Department;
import com.ems.employeeservice.department.DepartmentRepository;
import com.ems.employeeservice.employee.Employee;
import com.ems.employeeservice.employee.EmployeeRepository;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.enums.EmployeeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("seed")
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    log.info("Starting database seeding...");

    // Check if admin already exists
    if (employeeRepository.findByEmail("admin@company.com").isPresent()) {
      log.info("Admin user already exists. Skipping seeding.");
      return;
    }

    // Create Admin Department
    Department adminDepartment = Department.builder()
            .name("Administration")
            .build();
    adminDepartment = departmentRepository.save(adminDepartment);
    log.info("Created department: {}", adminDepartment.getName());

    // Create Admin User
    Employee admin = Employee.builder()
            .firstName("System")
            .lastName("Administrator")
            .email("admin@company.com")
            .password(passwordEncoder.encode("password123"))
            .role(EmployeeRole.ADMIN)
            .status(EmployeeStatus.ACTIVE)
            .department(adminDepartment)
            .build();
    admin = employeeRepository.save(admin);
    log.info("Created admin user: {} (ID: {})", admin.getEmail(), admin.getId());

    // Create some sample departments
    Department engineeringDept = Department.builder()
            .name("Engineering")
            .build();
    engineeringDept = departmentRepository.save(engineeringDept);
    log.info("Created department: {}", engineeringDept.getName());

    Department hrDept = Department.builder()
            .name("Human Resources")
            .build();
    hrDept = departmentRepository.save(hrDept);
    log.info("Created department: {}", hrDept.getName());

    Department salesDept = Department.builder()
            .name("Sales")
            .build();
    salesDept = departmentRepository.save(salesDept);
    log.info("Created department: {}", salesDept.getName());

    // Create a sample manager
    Employee manager = Employee.builder()
            .firstName("Jane")
            .lastName("Manager")
            .email("manager@company.com")
            .password(passwordEncoder.encode("password123"))
            .role(EmployeeRole.MANAGER)
            .status(EmployeeStatus.ACTIVE)
            .department(engineeringDept)
            .build();
    manager = employeeRepository.save(manager);
    log.info("Created manager user: {} (ID: {})", manager.getEmail(), manager.getId());

    // Create a sample employee
    Employee employee = Employee.builder()
            .firstName("John")
            .lastName("Employee")
            .email("employee@company.com")
            .password(passwordEncoder.encode("password123"))
            .role(EmployeeRole.EMPLOYEE)
            .status(EmployeeStatus.ACTIVE)
            .department(engineeringDept)
            .build();
    employee = employeeRepository.save(employee);
    log.info("Created employee user: {} (ID: {})", employee.getEmail(), employee.getId());

    log.info("Database seeding completed successfully!");
    log.info("===========================================");
    log.info("Admin credentials:");
    log.info("  Email: admin@company.com");
    log.info("  ID: {}", admin.getId());
    log.info("  Note: Set password using Auth Service");
    log.info("===========================================");
    log.info("Manager credentials:");
    log.info("  Email: manager@company.com");
    log.info("  ID: {}", manager.getId());
    log.info("===========================================");
    log.info("Employee credentials:");
    log.info("  Email: employee@company.com");
    log.info("  ID: {}", employee.getId());
    log.info("===========================================");

    // ✅ Exit application after seeding
    log.info("Seeding completed — shutting down application...");
    System.exit(0);
  }
}
