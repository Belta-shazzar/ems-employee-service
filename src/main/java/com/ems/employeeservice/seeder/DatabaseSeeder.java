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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Component
@Slf4j
@Profile("seed")
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;

  private static final int TOTAL_EMPLOYEES = 100;

  @Override
  public void run(String... args) {

    log.info("======== Starting Tech Company Database Seeding ========");

//    if (employeeRepository.count() > 1) {
//      log.info("Employees already exist. Skipping seeding.");
//      return;
//    }

    // ---------------------------------------------------------
    // 1. Seed Departments
    // ---------------------------------------------------------
    List<String> departmentNames = List.of(
            "Engineering",
            "Human Resources",
            "Product",
            "Design",
            "Marketing",
            "Sales",
            "Finance",
            "IT Support",
            "Security",
            "Administration"
    );

    List<Department> departments = departmentNames.stream()
            .map(name -> Department.builder().name(name).description("").active(true).build())
            .map(departmentRepository::save)
            .toList();

    log.info("Created {} departments", departments.size());

    // ---------------------------------------------------------
    // 2. Create User Counts by Status
    // ---------------------------------------------------------
    int activeCount = (int) (TOTAL_EMPLOYEES * 0.70);     // 70 ACTIVE
    int pendingCount = (int) (TOTAL_EMPLOYEES * 0.20);    // 20 PENDING
    int othersCount = TOTAL_EMPLOYEES - activeCount - pendingCount; // 10

    int suspendedCount = othersCount / 2;  // 5 suspended
    int deletedCount = othersCount - suspendedCount; // 5 deleted

    List<EmployeeStatus> statuses = new ArrayList<>();
    statuses.addAll(Collections.nCopies(activeCount, EmployeeStatus.ACTIVE));
    statuses.addAll(Collections.nCopies(pendingCount, EmployeeStatus.PENDING));
    statuses.addAll(Collections.nCopies(suspendedCount, EmployeeStatus.SUSPENDED));
    statuses.addAll(Collections.nCopies(deletedCount, EmployeeStatus.DELETED));

    Collections.shuffle(statuses);

    // ---------------------------------------------------------
    // 3. Create Employees
    // Includes Admins + Managers + Employees
    // ---------------------------------------------------------

    List<String> firstNames = List.of(
            "John", "Jane", "Michael", "Sarah", "Daniel", "Grace", "David", "Helen",
            "Samuel", "Cynthia", "Mark", "Ifeoma", "Matthew", "Victoria", "Peter",
            "Sophia", "Paul", "Abigail", "Timothy", "Olivia"
    );

    List<String> lastNames = List.of(
            "Johnson", "Williams", "Brown", "Ejiofor", "Smith", "Garcia", "Musa",
            "Okafor", "Ibrahim", "Adams", "Chukwu", "Eze", "Ojo", "James", "Richards"
    );

    // Ensure at least:
    // - 3 Admins
    // - 7 Managers
    int adminCount = 3;
    int managerCount = 7;

    List<EmployeeRole> roles = new ArrayList<>();
    roles.addAll(Collections.nCopies(adminCount, EmployeeRole.ADMIN));
    roles.addAll(Collections.nCopies(managerCount, EmployeeRole.MANAGER));
    roles.addAll(Collections.nCopies(TOTAL_EMPLOYEES - adminCount - managerCount, EmployeeRole.EMPLOYEE));

    Collections.shuffle(roles);

    List<Employee> employees = IntStream.range(0, TOTAL_EMPLOYEES)
            .mapToObj(i -> {

              String first = firstNames.get(ThreadLocalRandom.current().nextInt(firstNames.size()));
              String last = lastNames.get(ThreadLocalRandom.current().nextInt(lastNames.size()));
              String email = (first + "." + last + i + "@company.com")
                      .toLowerCase()
                      .replace(" ", "");

              Department randomDept = departments.get(
                      ThreadLocalRandom.current().nextInt(departments.size())
              );

              return Employee.builder()
                      .firstName(first)
                      .lastName(last)
                      .email(email)
                      .password(passwordEncoder.encode("password123"))
                      .role(roles.get(i))
                      .status(statuses.get(i))
                      .department(randomDept)
                      .build();
            })
            .map(employeeRepository::save)
            .toList();

    log.info("Created {} employees", employees.size());

    // ---------------------------------------------------------
    // 4. Summary Logs
    // ---------------------------------------------------------

    long admins = employees.stream().filter(e -> e.getRole() == EmployeeRole.ADMIN).count();
    long managers = employees.stream().filter(e -> e.getRole() == EmployeeRole.MANAGER).count();
    long active = employees.stream().filter(e -> e.getStatus() == EmployeeStatus.ACTIVE).count();
    long pending = employees.stream().filter(e -> e.getStatus() == EmployeeStatus.PENDING).count();
    long suspended = employees.stream().filter(e -> e.getStatus() == EmployeeStatus.SUSPENDED).count();
    long deleted = employees.stream().filter(e -> e.getStatus() == EmployeeStatus.DELETED).count();

    log.info("========= SEEDING SUMMARY =========");
    log.info("Total employees: {}", employees.size());
    log.info("Admins: {}", admins);
    log.info("Managers: {}", managers);
    log.info("Active: {}", active);
    log.info("Pending: {}", pending);
    log.info("Suspended: {}", suspended);
    log.info("Deleted: {}", deleted);
    log.info("===================================");

    log.info("Seeding completed — shutting down application...");
    System.exit(0);
  }
}