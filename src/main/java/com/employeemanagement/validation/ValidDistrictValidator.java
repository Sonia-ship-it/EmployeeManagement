package com.employeemanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDistrictValidator implements ConstraintValidator<ValidDistrict, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return RwandaDistricts.isValid(value);
    }
}
