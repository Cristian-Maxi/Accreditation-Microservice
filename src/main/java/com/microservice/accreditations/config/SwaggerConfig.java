package com.microservice.accreditations.config;

import io.swagger.v3.oas.models.Components;
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
public class SwaggerConfig {

    Contact contact = new Contact().name("Cristian Gomez") .url("https://github.com/Cristian-Maxi");

    License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

    Server devServer = new Server().
            url("http://localhost:8080").
            description("Server URL in Development environment");

    Server prodServer = new Server().
            url("").
            description("Server URL in Production environment");

    Info info = new Info()
            .title("Accreditation Microservice")
            .version("1.0")
            .contact(contact)
            .description("This microservice contains the accreditations endpoints").termsOfService("https://www.bezkoder.com/terms")
            .license(mitLicense);

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(info).servers(List.of(devServer));
    }
}
