package com.employeemanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration.
 *
 * <p>Access the interactive API docs at: <a href="http://localhost:8080/swagger-ui.html">/swagger-ui.html</a></p>
 * <p>After login, click Authorize and enter: {@code Bearer &lt;your-jwt-token&gt;}</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Payroll Management System API",
                version = "1.0",
                description = "Employee registration, payroll processing, payslips and messages",
                contact = @Contact(name = "RCA Employee Management")
        )
)
public class OpenApiConfig {