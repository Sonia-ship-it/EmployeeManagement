package com.employeemanagement.validation;

import com.employeemanagement.exception.BusinessValidationException;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.EmployeeStatus;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.PayslipRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Cross-field business rules for employees that cannot be expressed with a single annotation.
 *
 * <p>Called from {@link com.employeemanagement.service.EmployeeService} before save/update/delete.</p>
 */
@Component
@RequiredArgsConstructor
public class EmployeeBusinessValidator {

    private static final int MINIMUM_AGE = 18;

    private final Validator validator;
    private final EmployeeRepository employeeRepository;
    private final PayslipRepository payslipRepository;

    /** Validates a new employee before first save. */
    public void validateForCreate(Employee employee) {
        validateEntity(employee);
        validateBusinessRules(employee, null);
        validateUniqueness(employee, null);
    }

    /** Validates an existing employee before update. */
    public void validateForUpdate(Employee employee, Long existingId) {
        validateEntity(employee);
        validateBusinessRules(employee, existingId);
        validateUniqueness(employee, existingId);
    }

    /** Rule 10: Cannot delete employee if payroll records exist. */
    public void validateForDelete(Long employeeId) {
        if (payslipRepository.existsByEmployeeId(employeeId)) {
            throw new BusinessValidationException("Validation failed",
                    Map.of("employee", "Cannot delete employee with existing payroll records"));
        }
    }

    /** Rule: Employee ID is immutable after creation. */
    public void validateEmployeeCodeImmutable(String existingCode, String requestedCode) {
        if (requestedCode != null && !existingCode.equals(requestedCode)) {
            throw new BusinessValidationException("Validation failed",
                    Map.of("employeeCode", "Employee ID cannot be changed after creation"));
        }
    }

    /** Runs Jakarta Bean Validation on the entity (DB-level annotations). */
    private void validateEntity(Employee employee) {
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new LinkedHashMap<>();
            violations.forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
            throw new BusinessValidationException("Validation failed", errors);
        }
    }

    /**
     * Rules 1–2: age ≥ 18, joining date ≥ DOB + 18 years.
     * Also validates district, status, and positive salary.
     */
    private void validateBusinessRules(Employee employee, Long existingId) {
        Map<String, String> errors = new LinkedHashMap<>();

        LocalDate dob = employee.getDateOfBirth();
        if (dob != null) {
            if (dob.isAfter(LocalDate.now())) {
                errors.put("dateOfBirth", "Date of birth cannot be in the future");
            } else if (Period.between(dob, LocalDate.now()).getYears() < MINIMUM_AGE) {
                errors.put("dateOfBirth", "Employee must be at least 18 years old");
            }
        }

        LocalDate joiningDate = employee.getJoiningDate();
        if (dob != null && joiningDate != null) {
            LocalDate minJoiningDate = dob.plusYears(MINIMUM_AGE);
            if (joiningDate.isBefore(minJoiningDate)) {
                errors.put("joiningDate", "Joining date must be at least 18 years after date of birth");
            }
        }

        if (employee.getStatus() != EmployeeStatus.ACTIVE && employee.getStatus() != EmployeeStatus.INACTIVE) {
            errors.put("status", "Status must be ACTIVE or INACTIVE");
        }

        if (employee.getBaseSalary() != null && employee.getBaseSalary().signum() <= 0) {
            errors.put("baseSalary", "Salary must be greater than zero");
        }

        if (!RwandaDistricts.isValid(employee.getDistrict())) {
            errors.put("district", "District must be a valid Rwanda district");
        }

        if (!errors.isEmpty()) {
            throw new BusinessValidationException("Validation failed", errors);
        }
    }

    /** Rules 3–4: unique email and employee code. */
    private void validateUniqueness(Employee employee, Long existingId) {
        Map<String, String> errors = new LinkedHashMap<>();

        employeeRepository.findByEmail(employee.getEmail()).ifPresent(existing -> {
            if (existingId == null || !existing.getId().equals(existingId)) {
                errors.put("email", "Email already exists");
            }
        });

        employeeRepository.findByEmployeeCode(employee.getEmployeeCode()).ifPresent(existing -> {
            if (existingId == null || !existing.getId().equals(existingId)) {
                errors.put("employeeCode", "Employee ID must be unique");
            }
        });

        if (!errors.isEmpty()) {
            throw new BusinessValidationException("Validation failed", errors);
        }
    }
}
