# Codebase Guide — For New Developers

This document explains how to navigate the project. **Inline Java comments and JavaDoc** are in the source files.

## Where to start

| If you want to… | Open this file |
|-----------------|----------------|
| Understand the app startup | `EmployeeManagementApplication.java` |
| See package overview | `com/employeemanagement/package-info.java` |
| Learn salary formulas | `PayrollCalculationService.java` |
| See payroll flow | `PayrollService.java` → `processPayroll()` |
| Register an employee | `EmployeeController.java` → `EmployeeService.java` |
| Understand validations | `validation/EmployeeBusinessValidator.java`, `PayrollBusinessValidator.java` |
| See API error format | `GlobalExceptionHandler.java` |
| Configure security/JWT | `security/SecurityConfig.java`, `JwtAuthFilter.java` |
| Test the API | Swagger: `http://localhost:8080/swagger-ui.html` |

## Project structure

```
src/main/java/com/employeemanagement/
├── model/          ← Database tables (Employee, Payslip, Message, …)
├── dto/            ← API input/output + @Valid annotations
├── repository/     ← Database queries (Spring Data JPA)
├── service/        ← Business logic
├── controller/     ← REST endpoints
├── validation/     ← Custom rules (age, district, payroll checks)
├── security/       ← JWT login and role checks
├── config/         ← Seed data, Swagger, DB routines
└── exception/      ← Error responses
```

## Comment conventions used in this project

- **Class JavaDoc** — what the class does and how it fits in the system
- **Method JavaDoc** — on important public methods (payroll, validation)
- **Inline comments** — only for non-obvious steps (formula steps, security skips)
- **package-info.java** — overview of each major package

## Related documentation

- `SYSTEM_GUIDE.md` — architecture and setup
- `TESTING_GUIDE.md` — Postman and Swagger testing
- `VALIDATION_GUIDE.md` — all validation rules
- `MARKING_RUBRIC_PROOF.md` — exam rubric mapping
