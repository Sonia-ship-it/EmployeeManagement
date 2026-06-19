package com.employeemanagement.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InstitutionResponse {
    private Long id;
    private String name;
    private String location;
}
