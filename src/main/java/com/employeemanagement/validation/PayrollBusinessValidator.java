package com.employeemanagement.validation;

import com.employeemanagement.exception.BusinessValidationException;
import com.employeemanagement.model.Deduction;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.EmployeeStatus;
import com.employeemanagement.model.Payslip;
import com.employeemanagement.repository.DeductionRepository;
import com.employeemanagement.repository.PayslipRepository;
import com.employeemanagement.service.PayrollCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Payroll-specific validations — runs before and during payroll processing.
 *
 * <p>Enforces: no duplicate month/year, ACTIVE employees only, salary formula integrity.</p>
 */
@Component
@RequiredArgsConstructor
public class PayrollBusinessValidator {

    private final PayslipRepository payslipRepository;
    private final DeductionRepository deductionRepository;
    private final PayrollCalculationService calculationService;

    /** Rule 5: Payroll generated only once per month/year globally. */
    public void validatePayrollRequest(Integer month, Integer year) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (month == null || month < 1 || month > 12) {
            errors.put("month", "Month must be between 1 and 12");
        }

        if (payslipRepository.existsByMonthAndYear(month, year)) {
            errors.put("payroll", "Payroll already generated for this month/year");
        }

        if (!errors.isEmpty()) {
            throw new BusinessValidationException("Validation failed", errors);
        }
    }

    /** Rule 6: Only ACTIVE employees; no duplicate payslip for same employee/month/year. */
    public void validateEmployeeEligible(Employee employee, Integer month, Integer year) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            errors.put("status", "Payroll can only be generated for ACTIVE employees: " + employee.getEmployeeCode());
        }

        if (payslipRepository.existsByEmployeeIdAndMonthAndYear(employee.getId(), month, year)) {
            errors.put("payroll", "Duplicate payroll for employee " + employee.getEmployeeCode()
                    + " in " + month + "/" + year);
        }

        if (!errors.isEmpty()) {
            throw new BusinessValidationException("Validation failed", errors);
        }
    }

    /** Rule 9: All deduction percentages must be between 0 and 100. */
    public void validateDeductionRates() {
        List<Deduction> deductions = deductionRepository.findAll();
        Map<String, String> errors = new LinkedHashMap<>();

        for (Deduction d : deductions) {
            if (d.getPercentage().compareTo(BigDecimal.ZERO) < 0
                    || d.getPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                errors.put(d.getName(), "Deduction percentage must be between 0 and 100");
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessValidationException("Validation failed", errors);
        }
    }

    /**
     * Rule 12: Gross and net must match system recalculation — prevents manual tampering.
     * Also checks gross &gt; base, deductions ≤ gross, net ≥ 0.
     */
    public void validatePayslipBeforeSave(Payslip payslip, Employee employee) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (employee == null) {
            errors.put("employee", "Employee must exist");
        }

        if (payslip.getMonth() == null || payslip.getMonth() < 1 || payslip.getMonth() > 12) {
            errors.put("month", "Month is required and must be between 1 and 12");
        }

        if (payslip.getYear() == null) {
            errors.put("year", "Year is required");
        }

        if (payslip.getGrossSalary().compareTo(payslip.getBaseSalary()) <= 0) {
            errors.put("grossSalary", "Gross salary must be greater than base salary");
        }

        if (payslip.getTotalDeductions().compareTo(payslip.getGrossSalary()) > 0) {
            errors.put("totalDeductions", "Total deductions must not exceed gross salary");
        }

        if (payslip.getNetSalary().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("netSalary", "Net salary cannot be negative");
        }

        // Recalculate and compare — ensures values were not manually overridden
        Payslip recalculated = calculationService.calculatePayslip(
                employee, payslip.getMonth(), payslip.getYear());

        if (payslip.getGrossSalary().compareTo(recalculated.getGrossSalary()) != 0) {
            errors.put("grossSalary", "Gross salary must match system-calculated value");
        }

        if (payslip.getNetSalary().compareTo(recalculated.getNetSalary()) != 0) {
            errors.put("netSalary", "Net salary must match system-calculated value");
        }

        if (!errors.isEmpty()) {
            throw new BusinessValidationException("Validation failed", errors);
        }
    }

    /** Rule 11: No payslip update endpoints exist — this guards against future misuse. */
    public void validateNoPayrollModification(Payslip existing) {
        if (existing != null && existing.getId() != null) {
            throw new BusinessValidationException("Validation failed",
                    Map.of("payroll", "Cannot modify payroll after payslip generation"));
        }
    }
}
