/**
 * Service layer — core business logic.
 *
 * <p>{@link com.employeemanagement.service.PayrollCalculationService} contains the salary formulas.
 * {@link com.employeemanagement.service.PayrollService} orchestrates payroll processing.
 * {@link com.employeemanagement.service.EmployeeService} handles employee CRUD with validation.</p>
 *
 * <p>Services are {@code @Transactional} where database writes occur.</p>
 */
package com.employeemanagement.service;
