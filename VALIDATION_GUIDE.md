# Validation Guide — All Four Levels

## 1. Database Level (Entity / JPA)

| Table | Constraint |
|-------|------------|
| `employees` | `email` UNIQUE, `employee_code` UNIQUE, `CHECK (base_salary > 0)` |
| `employees` | `first_name`, `last_name`, `mobile`, `district`, `date_of_birth`, `joining_date` NOT NULL |
| `employees` | `employee_code` not updatable after creation |
| `deductions` | `name` UNIQUE, `CHECK (percentage >= 0 AND percentage <= 100)` |
| `payslips` | `UNIQUE(employee_id, month, year)` |
| `payslips` | `CHECK (gross_salary > base_salary AND net_salary >= 0 AND month >= 1 AND month <= 12)` |

**Files:** `Employee.java`, `Deduction.java`, `Payslip.java`

---

## 2. API Level (DTO + `@Valid`)

### EmployeeRequest (POST)
- First/Last name: `@NotBlank`, `@Size(2-50)`, `@Pattern` letters only
- Email: `@NotBlank`, `@Email`
- Mobile: `@Pattern ^07[2389][0-9]{7}$`
- District: `@ValidDistrict` (Rwanda districts list)
- DOB: `@NotNull`, `@Past`
- Employee ID: `@Pattern ^EMP[0-9]{3}$`
- Department: `@NotNull`
- Position: `@NotBlank`
- Base salary: `@DecimalMin("1")`
- Joining date: `@NotNull`, `@PastOrPresent`
- Status: `@NotNull` (ACTIVE / INACTIVE)

### EmployeeUpdateRequest (PUT)
- Same as above **except** Employee ID is omitted (immutable)

### PayrollRunRequest
- Month: `@Min(1)`, `@Max(12)`
- Year: `@ValidPayrollYear` (current year −10 to current year +1)

### DeductionRequest
- Name: `@NotBlank`
- Percentage: `@DecimalMin(0)`, `@DecimalMax(100)`

**Error response format:**
```json
{
  "message": "Validation failed",
  "errors": {
    "email": "Email already exists",
    "baseSalary": "Salary must be greater than zero"
  },
  "timestamp": "2026-06-19T10:00:00"
}
```

**File:** `GlobalExceptionHandler.java`

---

## 3. Business Logic Level

### EmployeeBusinessValidator
| Rule | Validation |
|------|------------|
| Age ≥ 18 | `Period.between(dob, now).getYears() >= 18` |
| Joining ≥ DOB + 18 years | Cross-field check |
| Email unique | Repository lookup |
| Employee ID unique | Repository lookup |
| Employee ID immutable | Blocked on update |
| District valid | `RwandaDistricts.DISTRICTS` |
| Cannot delete with payroll | `existsByEmployeeId` |
| Entity bean validation | Jakarta `Validator.validate(employee)` |

### DeductionService
- Percentage 0–100 before save
- Unique deduction name
- Entity-level `@DecimalMin` / `@DecimalMax`

**Files:** `EmployeeBusinessValidator.java`, `DeductionService.java`

---

## 4. Payroll Processing Level

### PayrollBusinessValidator
| Rule | Check |
|------|-------|
| Month 1–12 | Validated |
| No duplicate month/year | `existsByMonthAndYear` |
| Only ACTIVE employees | `findByStatus(ACTIVE)` + per-employee check |
| No duplicate employee/month/year | `existsByEmployeeIdAndMonthAndYear` |
| Deduction % valid | 0–100 on all deductions |
| Employee exists | `employee != null` |
| Gross > base | Enforced |
| Total deductions ≤ gross | Enforced |
| Net ≥ 0 | Enforced |
| Gross/net system-calculated | Recalculated and compared (Rule 12) |
| Cannot modify payroll | No update endpoints on payslips |

### PayrollCalculationService
```
Gross = Base + 14% House + 14% Transport
Net   = Base − Tax(30%) − Pension(6%) − Medical(5%) − Others(5%)
```

**Files:** `PayrollBusinessValidator.java`, `PayrollCalculationService.java`, `PayrollService.java`

---

## Rwanda Districts (30 districts)

Burera, Gakenke, Gicumbi, Musanze, Rulindo, Gisagara, Huye, Kamonyi, Muhanga, Nyamagabe, Nyanza, Nyaruguru, Ruhango, Karongi, Ngororero, Nyabihu, Nyamasheke, Rubavu, Rusizi, Rutsiro, Bugesera, Gatsibo, Kayonza, Kirehe, Ngoma, Nyagatare, Rwamagana, Gasabo, Kicukiro, Nyarugenge

---

## Sample Valid Employee (POST)

```json
{
  "firstName": "Alice",
  "lastName": "Uwase",
  "email": "alice@rca.gov.rw",
  "mobile": "0788123456",
  "district": "Gasabo",
  "dateOfBirth": "1995-05-20",
  "employeeCode": "EMP003",
  "departmentId": 1,
  "position": "Analyst",
  "baseSalary": 50000,
  "joiningDate": "2020-01-15",
  "status": "ACTIVE",
  "institutionName": "Rwanda Coding Academy"
}
```

---

## Reset database after schema changes

```sql
DROP DATABASE IF EXISTS employee_db;
CREATE DATABASE employee_db;
```
