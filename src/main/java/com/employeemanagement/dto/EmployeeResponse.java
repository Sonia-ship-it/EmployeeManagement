package com.employeemanagement.dto;

import com.employeemanagement.model.EmployeeStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String district;
    private LocalDate dateOfBirth;
    private String employeeCode;
    private String position;
    private BigDecimal baseSalary;
    private LocalDate joiningDate;
    private EmployeeStatus status;
    private String institutionName;
    private Long departmentId;
    private String departmentName;
}
