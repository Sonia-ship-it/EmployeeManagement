package com.employeemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. Starts Spring Boot on port 8080 (see application.yml).
 *
 * <p>On startup the app automatically:
 * <ul>
 *   <li>Creates/updates database tables (Hibernate ddl-auto: update)</li>
 *   <li>Seeds default users, departments, deductions, and sample employees</li>
 *   <li>Installs PostgreSQL helper functions from db/routines.sql</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class EmployeeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}
