package com.ems.employeeservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI employeeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management Service API: Employee-Service")
                        .description("REST API for managing employees and departments in the Employee Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("EMS Team")
                                .email("support@ems.com"))
                        )
                .servers(List.of(
                        new Server().url("http://localhost:8020").description("Local Development Server"),
                        new Server().url("http://localhost:8000/employment-service").description("API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from auth-service")));
    }
}
