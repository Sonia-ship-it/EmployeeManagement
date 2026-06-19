package com.employeemanagement.validation;

import com.employeemanagement.dto.PayrollRunRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class ValidPayrollYearValidator implements ConstraintValidator<ValidPayrollYear, PayrollRunRequest> {

    @Override
    public boolean isValid(PayrollRunRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getYear() == null) {
            return true;
        }
        int currentYear = Year.now().getValue();
        int year = request.getYear();
        return year >= currentYear - 10 && year <= currentYear + 1;
    }
}
