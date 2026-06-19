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
 * <p>Access the interactive API docs at: http://localhost:8080/swagger-ui.html</p>
 * <p>After login, click Authorize and enter: Bearer &lt;your-jwt-token&gt;</p>
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

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .name("Bearer Authentication")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
