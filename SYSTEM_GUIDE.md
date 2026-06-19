# Payroll Management System (PMS) — System Guide

Government ERP backend for **Employee Management**, **Deductions/Taxes**, and **Payroll/Payslip** processing. Built with **Spring Boot 3**, **PostgreSQL**, **Spring Data JPA**, **JWT Security**, and **PostgreSQL stored procedures/triggers**.

---

## 1. Architecture Overview

```
Client (Postman)
      │
      ▼
REST Controllers  (/api/auth, /api/employees, /api/deductions, /api/payroll, ...)
      │
      ▼
Services  (business logic, salary calculation, duplicate checks)
      │
      ▼
Spring Data JPA Repositories
      │
      ▼
PostgreSQL  (tables + triggers + approve_payroll procedure)
```

### Roles

| Role     | Permissions |
|----------|-------------|
| ADMIN    | Manage deductions, institutions, approve payroll, view all messages |
| MANAGER  | Manage employees/departments, generate monthly payroll |
| EMPLOYEE | View own payslips and salary notification messages |

---

## 2. Database Design (ERD)

```
┌─────────────┐       ┌──────────────┐       ┌───────────────┐
│   users     │       │  employees   │       │  employments  │
├─────────────┤       ├──────────────┤       ├───────────────┤
│ id          │◄──────│ user_id      │       │ employee_id   │──► employees
│ username    │       │ first_name   │◄──────│ employee_code │
│ email       │       │ last_name    │       │ position      │
│ password    │       │ email        │       │ base_salary   │
│ role        │       │ district     │       │ joining_date  │
└─────────────┘       │ mobile       │       │ status        │
                      │ date_of_birth│       │ department_id │──► departments
                      └──────────────┘       │ institution_id│──► institutions
                             │               └───────────────┘
                             │
                             ▼
                      ┌──────────────┐       ┌─────────────────┐
                      │   payslips   │       │ payroll_messages│
                      ├──────────────┤       ├─────────────────┤
                      │ employee_id  │       │ employee_id     │
                      │ base_salary  │       │ message         │
                      │ house        │       │ month, year     │
                      │ transport    │       │ created_at      │
                      │ gross        │       └─────────────────┘
                      │ tax,pension  │
                      │ medical,other│       ┌──────────────┐
                      │ net_salary   │       │  deductions  │
                      │ status       │       ├──────────────┤
                      │ month, year  │       │ name         │
                      └──────────────┘       │ percentage   │
                                             └──────────────┘
```

**Unique constraint:** One payslip per employee per `month/year` (prevents duplicate payroll).

---

## 3. Salary Calculation Logic

All percentages come from the `deductions` table (seeded on startup).

### Allowances (added to gross)
| Name      | % of Base |
|-----------|-----------|
| House     | 14%       |
| Transport | 14%       |

**Gross Salary** = `BaseSalary + House + Transport`

### Deductions (subtracted from gross)
| Name             | % of Base |
|------------------|-----------|
| EmployeeTax      | 30%       |
| Pension          | 6%        |
| MedicalInsurance | 5%        |
| Others           | 5%        |

**Net Salary** = `Gross - (Tax + Pension + Medical + Others)`

**Validation:** Total deductions must not exceed gross salary.

### Example (EMP-123, Base = 70,000 FRW)
| Field     | Value  |
|-----------|--------|
| House     | 10,000 |
| Transport | 10,000 |
| Gross     | 90,000 |
| Tax       | 21,000 |
| Pension   | 4,200  |
| Medical   | 3,500  |
| Others    | 3,500  |
| **Net**   | **57,800** |

---

## 4. Payroll Workflow

```
MANAGER                          ADMIN                         EMPLOYEE
   │                                │                              │
   │ POST /api/payroll/generate     │                              │
   │ {month, year}                  │                              │
   ▼                                │                              │
Creates PENDING payslips            │                              │
for all ACTIVE employees            │                              │
   │                                │                              │
   │                                │ POST /api/payroll/approve    │
   │                                │ {month, year}                │
   │                                ▼                              │
   │                    PostgreSQL procedure: approve_payroll       │
   │                    - Cursor over PENDING payslips              │
   │                    - Insert payroll_messages                   │
   │                    - Update status → PAID                     │
   │                                │                              │
   │                                │                              │ GET /api/payroll/payslips/me
   │                                │                              │ GET /api/payroll/messages/me
```

### PostgreSQL Routines (`src/main/resources/db/routines.sql`)
- **`fn_build_payroll_message`** — builds the notification text
- **`fn_payslip_after_insert`** — trigger: on INSERT with status=PAID, logs a message
- **`approve_payroll(month, year)`** — procedure with **CURSOR**: approves all pending payslips and sends messages

**Message format:**
> Dear `<FIRSTNAME>` Your salary of `<Month Year>` from `<INSTITUTION>` `<AMOUNT>` has been credited to your `<EMPLOYEE_CODE>` account Successfully

---

## 5. Prerequisites

1. **Java 17+**
2. **Maven 3.8+**
3. **PostgreSQL** running on `localhost:5432`

### Create database
```sql
CREATE DATABASE employee_db;
```

### Application config (`application.yml`)
```yaml
spring.datasource.url: jdbc:postgresql://localhost:5432/employee_db
spring.datasource.username: postgres
spring.datasource.password: sonia
server.port: 8080
```

---

## 6. Run the Application

```bash
cd EmployeeManagement
mvn spring-boot:run
```

On startup the app:
1. Creates/updates tables via Hibernate (`ddl-auto: update`)
2. Seeds users, departments, institutions, deductions, sample employees
3. Installs PostgreSQL triggers and `approve_payroll` procedure

---

## 7. API Reference

Base URL: `http://localhost:8080`

All protected endpoints require header:
```
Authorization: Bearer <JWT_TOKEN>
```

### Auth (public)

| Method | Endpoint | Body |
|--------|----------|------|
| POST | `/api/auth/register` | `{"username","email","password","role"}` |
| POST | `/api/auth/login` | `{"username","password"}` |

**Seeded accounts:**

| Username  | Password    | Role     |
|-----------|-------------|----------|
| admin     | admin123    | ADMIN    |
| manager   | manager123  | MANAGER  |
| employee  | employee123 | EMPLOYEE |

### Employees (ADMIN/MANAGER)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/employees` | Create employee + employment |
| GET    | `/api/employees` | List all |
| GET    | `/api/employees/{id}` | Get one |
| PUT    | `/api/employees/{id}` | Update |
| DELETE | `/api/employees/{id}` | Delete |

**Create employee body:**
```json
{
  "firstName": "Mugabo",
  "lastName": "Javis",
  "email": "mugabo@rca.gov.rw",
  "district": "Kigali",
  "mobile": "0788000001",
  "dateOfBirth": "1995-03-15",
  "employeeCode": "EMP-123",
  "position": "Software Developer",
  "baseSalary": 70000,
  "joiningDate": "2022-01-10",
  "status": "ACTIVE",
  "departmentId": 1,
  "institutionId": 1
}
```

### Deductions (ADMIN write, all roles read)

| Method | Endpoint |
|--------|----------|
| GET/POST/PUT/DELETE | `/api/deductions` |

### Institutions (ADMIN write)

| Method | Endpoint |
|--------|----------|
| GET/POST/PUT/DELETE | `/api/institutions` |

### Payroll

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/payroll/generate` | MANAGER | Generate payslips for month/year |
| POST | `/api/payroll/approve` | ADMIN | Approve payroll & send messages |
| GET  | `/api/payroll/payslips?month=6&year=2025` | ADMIN/MANAGER | View payslips |
| GET  | `/api/payroll/payslips/me` | EMPLOYEE | View own payslips |
| GET  | `/api/payroll/messages` | ADMIN | All notification messages |
| GET  | `/api/payroll/messages/me` | EMPLOYEE | Own messages |

---

## 8. Step-by-Step Testing (Postman)

### Step 1 — Login as Manager
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"username": "manager", "password": "manager123"}
```
Copy the `token` from the response.

### Step 2 — List deductions (verify seeded rates)
```
GET http://localhost:8080/api/deductions
Authorization: Bearer <manager_token>
```

### Step 3 — List employees
```
GET http://localhost:8080/api/employees
Authorization: Bearer <manager_token>
```

### Step 4 — Generate payroll for June 2025
```
POST http://localhost:8080/api/payroll/generate
Authorization: Bearer <manager_token>
Content-Type: application/json

{"month": 6, "year": 2025}
```
Expected: Payslips with `status: PENDING`. Mugabo Javis net salary = **57,800**.

### Step 5 — Try duplicate (should fail)
Repeat Step 4 — expect `400 Bad Request` with duplicate payroll message.

### Step 6 — View payslips as Manager
```
GET http://localhost:8080/api/payroll/payslips?month=6&year=2025
Authorization: Bearer <manager_token>
```

### Step 7 — Login as Admin and approve
```
POST http://localhost:8080/api/auth/login
{"username": "admin", "password": "admin123"}
```

```
POST http://localhost:8080/api/payroll/approve
Authorization: Bearer <admin_token>
Content-Type: application/json

{"month": 6, "year": 2025}
```
Expected: All payslips `status: PAID`, messages created in DB.

### Step 8 — View messages as Admin
```
GET http://localhost:8080/api/payroll/messages
Authorization: Bearer <admin_token>
```

### Step 9 — Employee views own payslip
```
POST http://localhost:8080/api/auth/login
{"username": "employee", "password": "employee123"}
```

```
GET http://localhost:8080/api/payroll/payslips/me
Authorization: Bearer <employee_token>
```

```
GET http://localhost:8080/api/payroll/messages/me
Authorization: Bearer <employee_token>
```

---

## 9. Project Structure

```
src/main/java/com/employeemanagement/
├── config/          DataSeeder, DatabaseRoutineInitializer
├── controller/      REST endpoints
├── dto/             Request/response objects
├── exception/       GlobalExceptionHandler
├── model/           JPA entities
├── repository/      Spring Data JPA
├── security/        JWT + SecurityConfig
└── service/         Business logic

src/main/resources/
├── application.yml
└── db/routines.sql  PostgreSQL triggers & procedures
```

---

## 10. Dependencies Used

| Dependency | Purpose |
|------------|---------|
| Spring Web | REST APIs |
| Spring Data JPA | ORM + repositories |
| Validation | `@NotNull`, `@Email`, etc. |
| Lombok | Reduce boilerplate |
| Spring Security + JWT | Authentication & RBAC |
| PostgreSQL Driver | Database |
| DevTools | Hot reload (dev) |

---

## 11. Troubleshooting

| Problem | Solution |
|---------|----------|
| Connection refused to PostgreSQL | Start PostgreSQL service; verify password `sonia` |
| Database does not exist | Run `CREATE DATABASE employee_db;` |
| 401 Unauthorized | Login again and pass `Authorization: Bearer <token>` |
| Duplicate payroll error | Use a different month/year or delete existing payslip |
| Employee can't view payslips | Ensure employee user is linked (`user_id` on employees table) — seeded user `employee` is linked to Mugabo Javis |
| DB routines not installed | Check logs for `PostgreSQL payroll routines installed successfully` after tables exist |

---

## 12. Exam Requirements Checklist

- [x] Task 1: Employee personal + employment details
- [x] Task 2: Deduction/tax percentage management endpoints
- [x] Task 3: Employee, Employment, Deductions, Payslip tables
- [x] Task 4: Manager generates payroll, Admin approves, Employee views payslips
- [x] Task 4: Gross/Net formulas, active employees only, no duplicate month/year
- [x] Task 5: PostgreSQL trigger + procedure + cursor + message logging
- [x] Spring Boot + JPA + JWT roles (ADMIN/MANAGER/EMPLOYEE)
