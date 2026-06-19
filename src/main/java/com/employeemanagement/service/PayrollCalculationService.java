package com.employeemanagement.service;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Payslip;
import com.employeemanagement.model.PayslipStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates gross and net salary for one employee.
 *
 * <p><b>Important:</b> Gross and net are always computed here — never trust values from the client.</p>
 *
 * <pre>
 * Gross = BaseSalary + (14% House) + (14% Transport)
 * Net   = BaseSalary − (30% Tax + 6% Pension + 5% Medical + 5% Others)
 * </pre>
 *
 * <p>Validation during calculation:
 * gross &gt; base, totalDeductions ≤ gross, net ≥ 0</p>
 */
@Service
@Slf4j
public class PayrollCalculationService {

    private static final int SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** Allowance and deduction rates defined by the exam paper (percent of base salary). */
    public static final BigDecimal HOUSE_RATE = new BigDecimal("14");
    public static final BigDecimal TRANSPORT_RATE = new BigDecimal("14");
    public static final BigDecimal TAX_RATE = new BigDecimal("30");
    public static final BigDecimal PENSION_RATE = new BigDecimal("6");
    public static final BigDecimal MEDICAL_RATE = new BigDecimal("5");
    public static final BigDecimal OTHERS_RATE = new BigDecimal("5");

    /**
     * Builds a payslip with status PENDING. Caller sets PAID after validation passes.
     */
    public Payslip calculatePayslip(Employee employee, Integer month, Integer year) {
        BigDecimal base = employee.getBaseSalary();

        // Step 1: Allowances added to gross
        BigDecimal house = percentOf(base, HOUSE_RATE);
        BigDecimal transport = percentOf(base, TRANSPORT_RATE);
        BigDecimal gross = base.add(house).add(transport);

        // Step 2: Deductions subtracted from base (not from gross)
        BigDecimal tax = percentOf(base, TAX_RATE);
        BigDecimal pension = percentOf(base, PENSION_RATE);
        BigDecimal medical = percentOf(base, MEDICAL_RATE);
        BigDecimal others = percentOf(base, OTHERS_RATE);
        BigDecimal totalDeductions = tax.add(pension).add(medical).add(others);

        // Step 3: Business rule checks
        if (gross.compareTo(base) <= 0) {
            throw new IllegalArgumentException("Gross salary must be greater than base salary");
        }
        if (totalDeductions.compareTo(gross) > 0) {
            throw new IllegalArgumentException(
                    "Total deductions (" + totalDeductions + ") exceed gross salary (" + gross + ")");
        }

        BigDecimal net = base.subtract(totalDeductions);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Net salary cannot be negative");
        }

        return Payslip.builder()
                .employee(employee)
                .baseSalary(scale(base))
                .houseAllowance(scale(house))
                .transportAllowance(scale(transport))
                .grossSalary(scale(gross))
                .totalDeductions(scale(totalDeductions))
                .tax(scale(tax))
                .pension(scale(pension))
                .medical(scale(medical))
                .others(scale(others))
                .netSalary(scale(net))
                .status(PayslipStatus.PENDING)
                .month(month)
                .year(year)
                .build();
    }

    /** Converts a percentage (e.g. 14) to an amount based on base salary. */
    private BigDecimal percentOf(BigDecimal base, BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Deduction percentage must be between 0 and 100");
        }
        return base.multiply(percentage)
                .divide(HUNDRED, SCALE + 2, RoundingMode.HALF_UP)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
