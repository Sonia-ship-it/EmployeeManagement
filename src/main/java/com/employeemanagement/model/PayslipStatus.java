package com.employeemanagement.model;

/**
 * Payslip payment status.
 */
public enum PayslipStatus {
    /** Calculated but not yet finalized (internal step). */
    PENDING,
    /** Payroll approved and payment recorded. */
    PAID
}
