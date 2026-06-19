package com.employeemanagement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String text;
    private LocalDateTime dateTime;
}
