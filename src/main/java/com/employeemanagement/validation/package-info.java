/**
 * Validation layer — rules that go beyond simple {@code @NotBlank} checks.
 *
 * <p>Validation happens at four levels in this project:
 * <ol>
 *   <li><b>Database</b> — CHECK/UNIQUE constraints on entities</li>
 *   <li><b>API</b> — {@code @Valid} on DTOs in controllers</li>
 *   <li><b>Business</b> — {@link com.employeemanagement.validation.EmployeeBusinessValidator}</li>
 *   <li><b>Payroll</b> — {@link com.employeemanagement.validation.PayrollBusinessValidator}</li>
 * </ol>
 * </p>
 */
package com.employeemanagement.validation;
