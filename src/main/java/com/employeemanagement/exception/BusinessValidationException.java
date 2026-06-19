package com.employeemanagement.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public BusinessValidationException(String message) {
        super(message);
        this.errors = Map.of("error", message);
    }

    public BusinessValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}
