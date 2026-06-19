package com.employeemanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

/**
 * Monthly payslip for one employee — all salary amounts are system-calculated only.
 *
 * <p>Unique constraint on (employee_id, month, year) prevents duplicate payroll.
 * Status flow: PENDING during calculation → PAID after successful processing.</p>
 *
 * <p>Salary formulas (see {@link com.employeemanagement.service.PayrollCalculationService}):
 * <ul>
 *   <li>Gross = Base + 14% House + 14% Transport</li>
 *   <li>Net = Base − Tax − Pension − Medical − Others</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "payslips", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
})
@Check(constraints = "gross_salary > base_salary AND net_salary >= 0 AND month >= 1 AND month <= 12")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of this payslip — many payslips belong to one employee. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // --- Calculated salary breakdown (never accepted from API input) ---

    @NotNull
    @DecimalMin("0")
    @Column(name = "base_salary", nullable = false)
    private BigDecimal baseSalary;

    @NotNull
    @Column(name = "house_allowance", nullable = false)
    private BigDecimal houseAllowance;

    @NotNull
    @Column(name = "transport_allowance", nullable = false)
    private BigDecimal transportAllowance;

    @NotNull
    @Column(name = "gross_salary", nullable = false)
    private BigDecimal grossSalary;

    @NotNull
    @Column(name = "total_deductions", nullable = false)
    private BigDecimal totalDeductions;

    @NotNull
    @Column(nullable = false)
    private BigDecimal tax;

    @NotNull
    @Column(nullable = false)
    private BigDecimal pension;

    @NotNull
    @Column(nullable = false)
    private BigDecimal medical;

    @NotNull
    @Column(nullable = false)
    private BigDecimal others;

    @NotNull
    @DecimalMin("0")
    @Column(name = "net_salary", nullable = false)
    private BigDecimal netSalary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PayslipStatus status = PayslipStatus.PENDING;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(nullable = false)
    private Integer month;

    @NotNull
    @Column(nullable = false)
    private Integer year;
}
