# Complete Testing Guide — Postman & Swagger

Test the full Payroll Management System end-to-end.

**Base URL:** `http://localhost:8080`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## Prerequisites

1. PostgreSQL running with database `employee_db` (password: `sonia`)
2. App started: `mvn spring-boot:run` or run from IntelliJ
3. **Fresh database recommended** after entity changes:

```sql
DROP DATABASE IF EXISTS employee_db;
CREATE DATABASE employee_db;
```

---

## Seeded Test Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| employee | employee123 | EMPLOYEE |

Seeded employees (ACTIVE):
- **Mugabo Javis** — EMP-123, base 70,000
- **Michou Michell** — EMP-234, base 35,000

---

# Part A — Swagger Testing

## Step 1: Open Swagger

Browser → `http://localhost:8080/swagger-ui.html`

## Step 2: Login (no auth required)

1. Expand **Authentication** → `POST /api/auth/login`
2. Click **Try it out**
3. Body:

```json
{
  "username": "manager",
  "password": "manager123"
}
```

4. Execute → copy the `token` value from the response

## Step 3: Authorize Swagger

1. Click the **Authorize** button (top right, lock icon)
2. Enter: `Bearer <paste_your_token_here>`
3. Click **Authorize** → **Close**

## Step 4: Register a new employee (POST)

1. **Employees** → `POST /api/employees`
2. Body:

```json
{
  "firstName": "Alice",
  "lastName": "Uwase",
  "email": "alice.uwase@rca.gov.rw",
  "baseSalary": 50000,
  "status": "ACTIVE",
  "employeeCode": "EMP-300",
  "institutionName": "Rwanda Coding Academy"
}
```

3. Expected: **201** with employee `id`, `baseSalary`, `status`

## Step 5: Process payroll (POST trigger)

1. **Payroll** → `POST /api/payroll/process`
2. Body:

```json
{
  "month": 7,
  "year": 2025
}
```

3. Expected: **200** with:
   - `employeesProcessed`: 3 (Mugabo + Michou + Alice)
   - `payslips[]` — each with `grossSalary`, `netSalary`, `status: "PAID"`
   - `messages[]` — each with `text` starting with `"Dear ..."`

### Verify Mugabo's payslip (base 70,000):

| Field | Expected |
|-------|----------|
| houseAllowance | 9800.00 |
| transportAllowance | 9800.00 |
| grossSalary | 89600.00 |
| totalDeductions | 31500.00 |
| netSalary | 58100.00 |
| status | PAID |

## Step 6: Test duplicate prevention

Repeat Step 5 with same month/year.

Expected: **400 Bad Request**
```json
{
  "status": 400,
  "message": "Payroll already processed for 7/2025"
}
```

## Step 7: Get payslips (GET)

1. `GET /api/payroll/payslips?month=7&year=2025`
2. Expected: list of all payslips for July 2025

## Step 7b: Download payroll (CSV or PDF)

1. **Payroll** → `GET /api/payroll/download`
2. Parameters: `month=7`, `year=2025`, `format=csv` (or `pdf`)
3. Expected: file download — `payroll_07_2025.csv` or `payroll_07_2025.pdf`

Or in browser/Postman:
```
GET http://localhost:8080/api/payroll/download?month=7&year=2025&format=pdf
Authorization: Bearer <token>
```

After `POST /api/payroll/process`, the response includes `csvDownloadUrl` and `pdfDownloadUrl`.

## Step 8: Get messages (GET)

1. **Messages** → `GET /api/messages`
2. Expected: messages with `id`, `text`, `dateTime`

Example message:
```
Dear Mugabo Your salary of July 2025 from Rwanda Coding Academy 58100.00 has been credited to your EMP-123 account Successfully
```

## Step 9: Employee views own payslips

1. Login as `employee` / `employee123` → get new token → Authorize
2. `GET /api/payroll/payslips/me`
3. `GET /api/messages/me`

Expected: only Mugabo Javis records (linked user)

## Step 10: Inactive employee excluded

1. Login as manager again
2. Register employee with `"status": "INACTIVE"`
3. Use a **new** month/year (e.g. month 8) for payroll
4. Verify inactive employee is NOT in the payslips list

---

# Part B — Postman Testing

## Environment Setup

Create Postman environment variable:
- `baseUrl` = `http://localhost:8080`
- `token` = (set after login)

---

## Request 1: Login

```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "manager",
  "password": "manager123"
}
```

**Tests tab (optional):**
```javascript
pm.test("Login successful", () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.environment.set("token", json.token);
```

---

## Request 2: Register Employee

```
POST {{baseUrl}}/api/employees
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "firstName": "Peter",
  "lastName": "Habimana",
  "email": "peter.habimana@rca.gov.rw",
  "baseSalary": 60000,
  "status": "ACTIVE",
  "employeeCode": "EMP-400",
  "institutionName": "MINEDUC"
}
```

Expected: **201 Created**

---

## Request 3: List Employees

```
GET {{baseUrl}}/api/employees
Authorization: Bearer {{token}}
```

Expected: **200** — array including seeded + new employees

---

## Request 4: Process Payroll

```
POST {{baseUrl}}/api/payroll/process
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "month": 9,
  "year": 2025
}
```

Expected: **200** — `payslips` and `messages` arrays populated

---

## Request 5: Duplicate Payroll (should fail)

```
POST {{baseUrl}}/api/payroll/process
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "month": 9,
  "year": 2025
}
```

Expected: **400** — duplicate payroll message

---

## Request 6: Get Payslips

```
GET {{baseUrl}}/api/payroll/payslips?month=9&year=2025
Authorization: Bearer {{token}}
```

Expected: **200** — payslips with calculated fields

---

## Request 7: Get All Messages

```
GET {{baseUrl}}/api/messages
Authorization: Bearer {{token}}
```

Expected: **200** — messages with `text` and `dateTime`

---

## Request 8: Validation Error Test

```
POST {{baseUrl}}/api/employees
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "firstName": "",
  "lastName": "Test",
  "email": "not-an-email",
  "baseSalary": -100,
  "status": "ACTIVE",
  "employeeCode": "EMP-999"
}
```

Expected: **400** — validation error messages

---

## Request 9: Unauthorized Test

```
GET {{baseUrl}}/api/employees
```

(No Authorization header)

Expected: **401** or **403**

---

## Request 10: Admin Login + View Messages

```
POST {{baseUrl}}/api/auth/login

{"username": "admin", "password": "admin123"}
```

Then:
```
GET {{baseUrl}}/api/messages
Authorization: Bearer {{admin_token}}
```

---

# Postman Collection Import (Quick Setup)

Create a collection with these folders:

```
PMS API/
├── Auth/
│   ├── Login Manager
│   ├── Login Admin
│   └── Login Employee
├── Employees/
│   ├── Register Employee
│   ├── List Employees
│   └── Get Employee by ID
├── Payroll/
│   ├── Process Payroll
│   ├── Get Payslips by Period
│   ├── Get All Payslips
│   └── Duplicate Payroll (expect 400)
└── Messages/
    ├── Get All Messages
    └── Get My Messages
```

Set collection-level Authorization: **Bearer Token** → `{{token}}`

---

# API Endpoint Summary

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | Public | Get JWT token |
| POST | `/api/auth/register` | Public | Register user |
| POST | `/api/employees` | ADMIN/MANAGER | Register employee |
| GET | `/api/employees` | ALL | List employees |
| POST | `/api/payroll/process` | ADMIN/MANAGER | **Trigger payroll** |
| GET | `/api/payroll/download?month=&year=&format=csv\|pdf` | ADMIN/MANAGER | **Download payroll file** |
| GET | `/api/payroll/payslips/download/me?month=&year=` | EMPLOYEE | Download own payslip PDF |
| GET | `/api/payroll/payslips/all` | ADMIN/MANAGER | All payslips |
| GET | `/api/payroll/payslips/me` | EMPLOYEE | Own payslips |
| GET | `/api/messages` | ADMIN/MANAGER | All messages |
| GET | `/api/messages/me` | EMPLOYEE | Own messages |

---

# Troubleshooting

| Issue | Fix |
|-------|-----|
| 401 Unauthorized | Login again; add `Bearer ` prefix to token |
| 403 Forbidden | Use manager/admin for payroll; employee for /me endpoints |
| Duplicate payroll 400 | Use a different month/year |
| Empty payslips | Ensure employees have `status: ACTIVE` |
| Swagger Authorize fails | Format: `Bearer eyJhbG...` (include the word Bearer) |
| Schema errors after update | Drop and recreate `employee_db` database |

---

# Success Checklist

- [ ] Swagger UI loads at `/swagger-ui.html`
- [ ] Login returns JWT token
- [ ] POST employee creates record with baseSalary and status
- [ ] POST payroll/process returns payslips with PAID status
- [ ] Gross = base + 14% + 14%
- [ ] Net = gross − 45% of base
- [ ] Messages contain "Dear ... Successfully"
- [ ] GET messages returns persisted records
- [ ] Duplicate month/year returns 400
- [ ] INACTIVE employees excluded from payroll
