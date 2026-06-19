package com.employeemanagement.model;

/**
 * Employee work status. Only ACTIVE employees receive payroll.
 */
public enum EmployeeStatus {
    /** Eligible for payroll processing. */
    ACTIVE,
    /** Excluded from payroll generation. */
    INACTIVE
}
