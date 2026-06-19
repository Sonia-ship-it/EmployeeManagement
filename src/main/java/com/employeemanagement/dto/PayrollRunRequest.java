package com.employeemanagement.dto;

import com.employeemanagement.validation.ValidPayrollYear;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for POST /api/payroll/process — triggers payroll for all ACTIVE employees.
 */
@Getter
@Setter
@ValidPayrollYear
public class PayrollRunRequest {

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year is invalid")
    private Integer year;
}
