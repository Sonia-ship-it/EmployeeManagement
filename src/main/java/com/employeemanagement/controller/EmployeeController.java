package com.employeemanagement.controller;

import com.employeemanagement.dto.EmployeeRequest;
import com.employeemanagement.dto.EmployeeResponse;
import com.employeemanagement.dto.EmployeeUpdateRequest;
import com.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Register and manage employees")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new employee")
    public EmployeeResponse register(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.register(request);
    }

    @GetMapping
    @Operation(summary = "List all employees")
    public List<EmployeeResponse> findAll() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public EmployeeResponse findById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee (Employee ID cannot be changed)")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete employee (blocked if payroll records exist)")
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }
}
