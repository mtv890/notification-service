package com.cobre.notification.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.cobre.com")
                                .description("Production server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Cobre Notification Service API")
                .description("Event notification and webhook delivery service. " +
                        "This API allows clients to query their notification events, " +
                        "view delivery status, and replay failed notifications.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Cobre API Team")
                        .email("api-support@cobre.com")
                        .url("https://docs.cobre.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from Keycloak. " +
                        "To get a token: POST to /realms/cobre/protocol/openid-connect/token " +
                        "with grant_type=password, client_id, client_secret, username, and password.");
    }
}