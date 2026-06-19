/**
 * JPA entity layer — maps Java objects to PostgreSQL tables.
 *
 * <p>Entities carry both <b>database constraints</b> ({@code @Check}, {@code UNIQUE}, {@code NOT NULL})
 * and <b>Jakarta validation</b> annotations so invalid data is rejected before persistence.</p>
 *
 * <p>Key relationships:
 * Employee 1→N Payslip, Employee 1→N Message, Employee N→1 Department</p>
 */
package com.employeemanagement.model;
