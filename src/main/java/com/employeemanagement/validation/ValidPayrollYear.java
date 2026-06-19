package com.employeemanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidPayrollYearValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPayrollYear {
    String message() default "Year must be within the last 10 years and not more than 1 year in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
