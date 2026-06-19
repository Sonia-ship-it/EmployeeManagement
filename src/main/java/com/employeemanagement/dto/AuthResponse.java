package com.employeemanagement.dto;

import com.employeemanagement.model.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String type;
    private String username;
    private Role role;
}
