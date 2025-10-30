package com.ems.employeeservice.department;

import com.ems.employeeservice.department.dto.DepartmentRequest;
import com.ems.employeeservice.department.dto.DepartmentResponse;
import com.ems.employeeservice.department.service.DepartmentService;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "APIs for managing departments")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

  private final DepartmentService departmentService;

  @Operation(
          summary = "Create a new department",
          description = "Creates a new department in the system. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Department created successfully",
                  content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<DepartmentResponse> createDepartment(
          @Parameter(description = "Department details to create", required = true)
          @Valid @RequestBody DepartmentRequest request) {

    DepartmentResponse response = departmentService.createDepartment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
          summary = "Update a department",
          description = "Updates an existing department's information. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Department updated successfully",
                  content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
          @ApiResponse(responseCode = "404", description = "Department not found")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<DepartmentResponse> updateDepartment(
          @Parameter(description = "Department ID", required = true) @PathVariable UUID id,
          @Parameter(description = "Updated department details", required = true)
          @Valid @RequestBody DepartmentRequest request) {

    DepartmentResponse response = departmentService.updateDepartment(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "Delete a department",
          description = "Deletes a department from the system. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
          @ApiResponse(responseCode = "404", description = "Department not found")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteDepartment(
          @Parameter(description = "Department ID", required = true) @PathVariable UUID id) {

    departmentService.deleteDepartment(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
          summary = "Get department by ID",
          description = "Retrieves department details by ID. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Department retrieved successfully",
                  content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
          @ApiResponse(responseCode = "404", description = "Department not found")
  })
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<DepartmentResponse> getDepartmentById(
          @Parameter(description = "Department ID", required = true) @PathVariable UUID id) {
    DepartmentResponse response = departmentService.getDepartmentById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "Get all departments",
          description = "Retrieves all departments in the system. Admin role required."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Departments retrieved successfully"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
  })
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
    List<DepartmentResponse> responses = departmentService.getAllDepartments();
    return ResponseEntity.ok(responses);
  }
}
