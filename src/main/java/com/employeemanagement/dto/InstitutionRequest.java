package com.employeemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstitutionRequest {

    @NotBlank
    private String name;

    private String location;
}
