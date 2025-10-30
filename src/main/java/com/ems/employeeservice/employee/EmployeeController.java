package com.ems.employeeservice.employee;

import com.ems.employeeservice.config.security.enums.UserHttpHeaders;
import com.ems.employeeservice.employee.dto.AuthServiceEmployeeResponse;
import com.ems.employeeservice.employee.dto.EmployeeRequest;
import com.ems.employeeservice.employee.dto.EmployeeResponse;
import com.ems.employeeservice.employee.enums.EmployeeRole;
import com.ems.employeeservice.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employees")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

  private final EmployeeService employeeService;

  @Operation(
          summary = "Create a new employee",
          description = "Creates a new employee in the system. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Employee created successfully",
                  content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EmployeeResponse> createEmployee(
          @Parameter(description = "Employee details to create", required = true)
          @Valid @RequestBody EmployeeRequest request) {

    EmployeeResponse response = employeeService.createEmployee(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
          summary = "Update an employee",
          description = "Updates an existing employee's information. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                  content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
          @ApiResponse(responseCode = "404", description = "Employee not found")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EmployeeResponse> updateEmployee(
          @Parameter(description = "Employee ID", required = true) @PathVariable UUID id,
          @Parameter(description = "Updated employee details", required = true)
          @Valid @RequestBody EmployeeRequest request) {

    EmployeeResponse response = employeeService.updateEmployee(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "Delete an employee",
          description = "Deletes an employee from the system. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
          @ApiResponse(responseCode = "404", description = "Employee not found")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteEmployee(
          @Parameter(description = "Employee ID", required = true) @PathVariable UUID id) {

    employeeService.deleteEmployee(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
          summary = "Get employee by ID",
          description = "Retrieves employee details by ID with role-based access control. " +
                  "Admin can view any employee, Manager can view employees in their department, " +
                  "Employee can view only their own details."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Employee retrieved successfully",
                  content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
          @ApiResponse(responseCode = "404", description = "Employee not found")
  })
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
  public ResponseEntity<EmployeeResponse> getEmployeeById(
          @Parameter(description = "Employee ID", required = true) @PathVariable UUID id,
          @Parameter(hidden = true) @RequestHeader(UserHttpHeaders.X_EMPLOYEE_ROLE) EmployeeRole role,
          @Parameter(hidden = true) @RequestHeader(UserHttpHeaders.X_EMPLOYEE_ID) UUID requesterId) {
    EmployeeResponse response;

    if (role == EmployeeRole.MANAGER) {
      response = employeeService.getEmployeeById(id, requesterId);
    } else if (role == EmployeeRole.EMPLOYEE) {
      response = employeeService.getEmployeeById(requesterId, null);
    } else {
      response = employeeService.getEmployeeById(id, null);
    }

    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "Get all employees",
          description = "Retrieves all employees with role-based filtering. " +
                  "Admin can view all employees, Manager can view employees in their department."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin or Manager role required")
  })
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
          @Parameter(hidden = true) @RequestHeader(UserHttpHeaders.X_EMPLOYEE_ID) UUID requesterId
  ) {
    List<EmployeeResponse> response = employeeService.getAllEmployees(requesterId);
    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "Get employee by email",
          description = "Internal endpoint used by auth-service to retrieve employee details by email"
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Employee retrieved successfully",
                  content = @Content(schema = @Schema(implementation = AuthServiceEmployeeResponse.class))),
          @ApiResponse(responseCode = "404", description = "Employee not found")
  })
  @GetMapping("/email/{email}")
  public ResponseEntity<AuthServiceEmployeeResponse> getEmployeeByEmail(
          @Parameter(description = "Employee email", required = true) @PathVariable String email) {
    AuthServiceEmployeeResponse response = employeeService.getEmployeeByEmail(email);
    return ResponseEntity.ok(response);
  }
}
