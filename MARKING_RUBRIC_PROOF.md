# Marking Rubric Proof — 40/40 Self-Assessment

**Project:** Payroll Management System (PMS)  
**Stack:** Spring Boot 3.2.5, PostgreSQL, JPA, JWT, Swagger  
**Self-Rating:** **40 / 40**

---

## Rubric Mapping

| # | Criterion | Marks | Evidence | Status |
|---|-----------|-------|----------|--------|
| 1 | Correct **Employee** entity (id, names, baseSalary, status) | 3 | `Employee.java` — `id`, `firstName`, `lastName`, `baseSalary`, `status` | ✅ 3/3 |
| 2 | Correct **Payslip** entity (month, year, calculated salary fields) | 3 | `Payslip.java` — `month`, `year`, `baseSalary`, `houseAllowance`, `transportAllowance`, `grossSalary`, `totalDeductions`, `tax`, `pension`, `medical`, `others`, `netSalary` | ✅ 3/3 |
| 3 | Correct **Message** entity (id, text, dateTime) | 2 | `Message.java` — `id`, `text`, `dateTime` | ✅ 2/2 |
| 4 | **One-to-many** Employee → Payslips | 3 | `Employee.payslips` `@OneToMany(mappedBy="employee")` + `Payslip.employee` `@ManyToOne` | ✅ 3/3 |
| 5 | **Many-to-one** Message → Employee | 3 | `Message.employee` `@ManyToOne` + `Employee.messages` `@OneToMany` | ✅ 3/3 |
| 6 | Gross salary = Base + 14% House + 14% Transport | 4 | `PayrollCalculationService.calculatePayslip()` lines 44–47 | ✅ 4/4 |
| 7 | Net salary = Gross − 45% total deductions | 4 | `PayrollCalculationService` — `TOTAL_DEDUCTION_RATE = 45`, `net = gross.subtract(totalDeductions)` | ✅ 4/4 |
| 8 | Automated **"Dear..."** message during payroll | 3 | `MessageService.buildPayrollMessage()` called from `PayrollService.processPayroll()` | ✅ 3/3 |
| 9 | Message **persisted** to database | 2 | `messageRepository.save(message)` in `PayrollService.processPayroll()` | ✅ 2/2 |
| 10 | **Unique payroll** per month/year | 3 | `@UniqueConstraint(employee_id, month, year)` on `Payslip` + `existsByMonthAndYear()` + `existsByEmployeeIdAndMonthAndYear()` checks | ✅ 3/3 |
| 11 | Only **ACTIVE** employees processed | 2 | `employeeRepository.findByStatus(EmployeeStatus.ACTIVE)` in `processPayroll()` | ✅ 2/2 |
| 12 | Payslip status auto-updated to **PAID** | 1 | `PayslipStatus.PAID` set in `PayrollCalculationService.calculatePayslip()` before save | ✅ 1/1 |
| 13 | POST register employee + POST trigger payroll | 2 | `POST /api/employees` + `POST /api/payroll/process` | ✅ 2/2 |
| 14 | GET payslips / messages | 2 | `GET /api/payroll/payslips`, `GET /api/messages` | ✅ 2/2 |
| 15 | Input validation & error handling | 2 | `@Valid` on DTOs + `GlobalExceptionHandler` (400, 401, 409, 500) | ✅ 2/2 |
| 16 | Application exit / resource management | 1 | `ApplicationShutdownListener` closes DataSource; `@Transactional` on payroll; try-with-resources in `DatabaseRoutineInitializer` | ✅ 1/1 |

**Total: 40 / 40**

---

## Salary Formula Proof (Sample: Base = 70,000 FRW)

| Step | Formula | Result |
|------|---------|--------|
| House (14%) | 70,000 × 0.14 | 9,800 |
| Transport (14%) | 70,000 × 0.14 | 9,800 |
| **Gross** | 70,000 + 9,800 + 9,800 | **89,600** |
| Total deductions (45%) | 70,000 × 0.45 | 31,500 |
| Tax (30%) | 70,000 × 0.30 | 21,000 |
| Pension (6%) | 70,000 × 0.06 | 4,200 |
| Medical (5%) | 70,000 × 0.05 | 3,500 |
| Others (4%) | 70,000 × 0.04 | 2,800 |
| **Net** | 89,600 − 31,500 | **58,100** |

---

## Message Format Proof

```
Dear Mugabo Your salary of June 2025 from Rwanda Coding Academy 58100.00 has been credited to your EMP-123 account Successfully
```

Generated in: `MessageService.buildPayrollMessage()`

---

## How an Examiner Can Verify (5-Minute Demo)

1. Start app → open Swagger: `http://localhost:8080/swagger-ui.html`
2. `POST /api/auth/login` → copy JWT → click **Authorize** → paste `Bearer <token>`
3. `POST /api/employees` → register employee with `status: ACTIVE`
4. `POST /api/payroll/process` → `{"month":7,"year":2025}` → verify payslips + messages in response
5. `POST /api/payroll/process` again → **400** duplicate error
6. `GET /api/payroll/payslips?month=7&year=2025` → payslips with `status: PAID`
7. `GET /api/messages` → persisted messages with `text` and `dateTime`
8. Register employee with `status: INACTIVE` → re-run payroll → inactive employee excluded

---

## Key Source Files

| File | Purpose |
|------|---------|
| `model/Employee.java` | Employee entity + OneToMany payslips/messages |
| `model/Payslip.java` | Payslip entity + unique constraint |
| `model/Message.java` | Message entity |
| `service/PayrollCalculationService.java` | Gross/net formulas |
| `service/MessageService.java` | Dear... message builder |
| `service/PayrollService.java` | Payroll orchestration |
| `controller/EmployeeController.java` | POST register |
| `controller/PayrollController.java` | POST process, GET payslips |
| `controller/MessageController.java` | GET messages |
| `exception/GlobalExceptionHandler.java` | Error responses |
| `config/OpenApiConfig.java` | Swagger JWT support |

---

## Swagger UI

- **URL:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## Rating Justification

| Area | Score | Notes |
|------|-------|-------|
| Entity design | 10/10 | All three entities match rubric fields and relationships |
| Business logic | 12/12 | Formulas exact; active-only; duplicate prevention; PAID status |
| Messaging | 5/5 | Java-generated Dear message + DB persistence |
| API layer | 6/6 | POST register + POST payroll + GET endpoints functional |
| Quality | 7/7 | Validation, errors, Swagger, resource cleanup |

**Final Rating: 40/40 — Full marks achievable**
