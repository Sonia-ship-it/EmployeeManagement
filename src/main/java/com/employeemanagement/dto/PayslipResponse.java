package com.employeemanagement.dto;

import com.employeemanagement.model.PayslipStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PayslipResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private BigDecimal baseSalary;
    private BigDecimal houseAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal tax;
    private BigDecimal pension;
    private BigDecimal medical;
    private BigDecimal others;
    private BigDecimal netSalary;
    private PayslipStatus status;
    private Integer month;
    private Integer year;
}
