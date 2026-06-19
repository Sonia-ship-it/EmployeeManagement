package com.employeemanagement.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayrollDownloadResult {
    private byte[] content;
    private String filename;
    private String contentType;
}
