package com.employeemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DeductionRequest {

    @NotBlank(message = "Deduction name is required")
    private String name;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0", message = "Percentage must be at least 0")
    @DecimalMax(value = "100", message = "Percentage must not exceed 100")
    private BigDecimal percentage;
}
