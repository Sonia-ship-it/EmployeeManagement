package com.employeemanagement.dto;

import com.employeemanagement.model.EmployeeStatus;
import com.employeemanagement.validation.ValidDistrict;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for PUT /api/employees/{id}.
 * Employee ID (employeeCode) is intentionally omitted — it cannot be changed after creation.
 */
@Getter
@Setter
public class EmployeeUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters and spaces")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^07[2389][0-9]{7}$", message = "Mobile must be a valid Rwanda number")
    private String mobile;

    @NotBlank(message = "District is required")
    @ValidDistrict
    private String district;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotBlank(message = "Position is required")
    private String position;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "1", message = "Salary must be greater than zero")
    private BigDecimal baseSalary;

    @NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    @NotNull(message = "Status is required")
    private EmployeeStatus status;

    private String institutionName;
}
