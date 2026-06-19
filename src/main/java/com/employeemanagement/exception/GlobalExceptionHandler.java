package com.employeemanagement.exception;

import com.employeemanagement.dto.ApiError;
import com.employeemanagement.dto.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts exceptions into consistent JSON error responses for API clients.
 *
 * <p>Validation errors return a map of field names → messages:
 * {@code {"message":"Validation failed","errors":{"email":"Email already exists"}}}</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Business rules from EmployeeBusinessValidator / PayrollBusinessValidator. */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleBusinessValidation(BusinessValidationException ex) {
        log.warn("Business validation failed: {}", ex.getErrors());
        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .message("Validation failed")
                .errors(ex.getErrors())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .message("Validation failed")
                .errors(Map.of("error", ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Database UNIQUE/CHECK constraint violations (e.g. duplicate payroll). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        Map<String, String> errors = new LinkedHashMap<>();
        String msg = ex.getMessage() != null ? ex.getMessage() : "";
        if (msg.contains("payslips") || msg.contains("employee_id")) {
            errors.put("payroll", "Duplicate payroll for this employee in the same month/year");
        } else if (msg.contains("email")) {
            errors.put("email", "Email already exists");
        } else if (msg.contains("employee_code")) {
            errors.put("employeeCode", "Employee ID must be unique");
        } else {
            errors.put("error", "Duplicate record or constraint violation");
        }
        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    /** Jakarta @Valid failures on DTOs (e.g. @NotBlank, @Email, @Pattern). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
