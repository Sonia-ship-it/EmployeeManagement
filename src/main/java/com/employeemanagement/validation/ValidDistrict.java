package com.employeemanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidDistrictValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDistrict {
    String message() default "District must be a valid Rwanda district";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
