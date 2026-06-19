package com.employeemanagement.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PayrollProcessResponse {
    private Integer month;
    private Integer year;
    private int employeesProcessed;
    private List<PayslipResponse> payslips;
    private List<MessageResponse> messages;
    private String csvDownloadUrl;
    private String pdfDownloadUrl;
}
