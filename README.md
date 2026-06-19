# Employee Management System

Spring Boot REST API for managing employees and departments, secured with JWT and role-based access control. Uses **PostgreSQL** as the database.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| PostgreSQL | 14+ |

---

## Database Setup

1. Start PostgreSQL.
2. Create the database:

```sql
CREATE DATABASE employee_db;
```

3. Connection settings (in `src/main/resources/application.yml`):

| Setting | Value |
|---------|-------|
| URL | `jdbc:postgresql://localhost:5432/employee_db` |
| Username | `postgres` |
| Password | `sonia` |

Tables are created automatically via Hibernate (`ddl-auto: update`).

---

## Run the Application

```bash
cd EmployeeManagement
mvn spring-boot:run
```

Server starts at **http://localhost:8080**

---

## Architecture Overview

```
Client (Postman/curl)
        │
        ▼
┌───────────────────┐
│  REST Controllers │  Auth, Employee, Department
└─────────┬─────────┘
          │
┌─────────▼─────────┐
│     Services      │  Business logic
└─────────┬─────────┘
          │
┌─────────▼─────────┐
│   JPA Repositories│  PostgreSQL via Spring Data JPA
└───────────────────┘

Security layer (every request except /api/auth/**):
  JwtAuthFilter → validates Bearer token → sets SecurityContext
```

---

## Dependencies Used

| Dependency | Purpose |
|------------|---------|
| **Spring Web** | REST controllers and JSON APIs |
| **Spring Data JPA** | ORM, repositories, PostgreSQL driver |
| **Validation** | `@NotNull`, `@Email`, `@NotBlank` on DTOs |
| **Lombok** | `@Getter`, `@Setter`, `@Builder` — less boilerplate |
| **Spring Security** | JWT auth, role-based access |
| **Spring Boot DevTools** | Hot reload during development |
| **PostgreSQL** | Primary database |
| **JJWT** | JWT token generation and validation |

---

## Authentication Flow

1. **Register** or use seeded users (see below).
2. **Login** with username/password → receive JWT token.
3. Send token on every protected request:

```
Authorization: Bearer <your-jwt-token>
```

4. `JwtAuthFilter` extracts the token, validates it, and loads the user into Spring Security context.
5. `SecurityConfig` enforces role rules per endpoint.

### Seeded Users (created on first startup)

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| employee | employee123 | EMPLOYEE |

---

## Role Permissions

| Endpoint | ADMIN | MANAGER | EMPLOYEE |
|----------|:-----:|:-------:|:--------:|
| `POST /api/auth/register` | Public | Public | Public |
| `POST /api/auth/login` | Public | Public | Public |
| `GET /api/employees/**` | Yes | Yes | Yes |
| `POST/PUT/DELETE /api/employees/**` | Yes | Yes | No |
| `GET/POST/PUT/DELETE /api/departments/**` | Yes | Yes | No |

---

## API Endpoints

### Auth (no token required)

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@company.com",
  "password": "password123",
  "role": "EMPLOYEE"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Departments (token required — ADMIN or MANAGER)

#### Create department
```http
POST /api/departments
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Finance",
  "description": "Accounting and payroll"
}
```

#### List all departments
```http
GET /api/departments
Authorization: Bearer <token>
```

#### Get / Update / Delete
```http
GET    /api/departments/{id}
PUT    /api/departments/{id}
DELETE /api/departments/{id}
Authorization: Bearer <token>
```

---

### Employees

#### Create employee (ADMIN or MANAGER)
```http
POST /api/employees
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@company.com",
  "phone": "+1234567890",
  "jobTitle": "Software Engineer",
  "salary": 75000,
  "hireDate": "2024-01-15",
  "status": "ACTIVE",
  "departmentId": 1
}
```

**Status values:** `ACTIVE`, `INACTIVE`, `ON_LEAVE`

#### List all employees (all roles)
```http
GET /api/employees
Authorization: Bearer <token>
```

#### Get / Update / Delete (update/delete: ADMIN or MANAGER only)
```http
GET    /api/employees/{id}
PUT    /api/employees/{id}
DELETE /api/employees/{id}
Authorization: Bearer <token>
```

---

## How to Test (Step by Step)

### 1. Start PostgreSQL and create database

```sql
CREATE DATABASE employee_db;
```

### 2. Run the app

```bash
mvn spring-boot:run
```

Wait for: `Started EmployeeManagementApplication`

### 3. Login as admin

**PowerShell:**
```powershell
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}'
$token = $login.token
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Copy the `token` from the response.

### 4. List departments (seeded: Engineering, HR)

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/departments" `
  -Headers @{ Authorization = "Bearer $token" }
```

### 5. Create an employee

```powershell
$body = @{
  firstName = "Jane"
  lastName = "Smith"
  email = "jane.smith@company.com"
  phone = "+1987654321"
  jobTitle = "HR Specialist"
  salary = 65000
  hireDate = "2025-03-01"
  status = "ACTIVE"
  departmentId = 2
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/employees" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body $body
```

### 6. List employees

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/employees" `
  -Headers @{ Authorization = "Bearer $token" }
```

### 7. Test role restrictions

Login as **employee** and try to create an employee — expect **403 Forbidden**:

```powershell
$empLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"employee","password":"employee123"}'

Invoke-RestMethod -Uri "http://localhost:8080/api/employees" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $($empLogin.token)" } `
  -Body $body
```

Employee **can** still `GET /api/employees` (read-only access).

### 8. Test validation

Send invalid email — expect **400 Bad Request**:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/employees" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body '{"firstName":"Bad","lastName":"Data","email":"not-an-email","status":"ACTIVE"}'
```

---

## Postman Testing

1. Create a collection variable `baseUrl` = `http://localhost:8080`
2. **Login** request → save token to collection variable `token`
3. Set collection auth: **Bearer Token** = `{{token}}`
4. Add requests for departments and employees as documented above

---

## Project Structure

```
src/main/java/com/employeemanagement/
├── EmployeeManagementApplication.java
├── config/
│   └── DataSeeder.java          # Default users & departments
├── controller/
│   ├── AuthController.java
│   ├── DepartmentController.java
│   └── EmployeeController.java
├── dto/                         # Request/response + validation
├── exception/
│   └── GlobalExceptionHandler.java
├── model/
│   ├── User.java
│   ├── Employee.java
│   ├── Department.java
│   ├── Role.java
│   └── EmployeeStatus.java
├── repository/                  # Spring Data JPA
├── security/
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   ├── SecurityConfig.java
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── EmployeeService.java
    └── DepartmentService.java
```

---

## Request Lifecycle (End to End)

1. **HTTP request** hits a controller (e.g. `POST /api/employees`).
2. **JwtAuthFilter** reads `Authorization: Bearer ...`, validates JWT, loads user.
3. **SecurityConfig** checks if user's role is allowed for that route.
4. **Controller** validates the JSON body (`@Valid` on DTO).
5. **Service** runs business logic (duplicate checks, department lookup).
6. **Repository** persists/fetches from PostgreSQL via JPA.
7. **Response** returned as JSON; errors handled by `GlobalExceptionHandler`.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Connection refused to PostgreSQL | Ensure PostgreSQL is running; verify host/port |
| `FATAL: database "employee_db" does not exist` | Run `CREATE DATABASE employee_db;` |
| Authentication failed for user postgres | Confirm password is `sonia` in `application.yml` |
| 403 Forbidden | User role lacks permission; use admin/manager token |
| 401 Unauthorized | Token missing, expired, or invalid |

---

## Build & Package

```bash
mvn clean package
java -jar target/employee-management-1.0.0.jar
```
