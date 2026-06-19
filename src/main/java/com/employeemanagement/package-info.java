/**
 * Payroll Management System (PMS) — Government ERP backend.
 *
 * <h2>Package layout</h2>
 * <ul>
 *   <li>{@code model} — JPA entities (Employee, Payslip, Message, Deduction, …)</li>
 *   <li>{@code dto} — API request/response objects with Jakarta validation annotations</li>
 *   <li>{@code repository} — Spring Data JPA database access</li>
 *   <li>{@code service} — Business logic (payroll calculation, employee registration, exports)</li>
 *   <li>{@code controller} — REST endpoints (see Swagger UI at /swagger-ui.html)</li>
 *   <li>{@code validation} — Custom validators and cross-field business rules</li>
 *   <li>{@code security} — JWT authentication and role-based access (ADMIN, MANAGER, EMPLOYEE)</li>
 *   <li>{@code config} — Startup seed data, OpenAPI, PostgreSQL routines</li>
 *   <li>{@code exception} — Global error handling with field-level validation responses</li>
 * </ul>
 *
 * <h2>Typical flow</h2>
 * <ol>
 *   <li>Login via {@code POST /api/auth/login} → receive JWT token</li>
 *   <li>Register employees via {@code POST /api/employees}</li>
 *   <li>Manager/Admin processes payroll via {@code POST /api/payroll/process}</li>
 *   <li>Download payroll CSV/PDF via {@code GET /api/payroll/download}</li>
 *   <li>View messages via {@code GET /api/messages}</li>
 * </ol>
 *
 * <p>See project docs: SYSTEM_GUIDE.md, TESTING_GUIDE.md, VALIDATION_GUIDE.md, MARKING_RUBRIC_PROOF.md</p>
 */
package com.employeemanagement;
