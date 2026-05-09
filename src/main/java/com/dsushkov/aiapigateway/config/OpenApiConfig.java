package com.dsushkov.aiapigateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openAPI() {
        String schemeName = "ApiKeyAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("AI API Gateway MCP")
                        .version("0.1.0")
                        .description("Production-style Java/Spring Boot gateway for explainable risk decisions and Spring AI MCP tool exposure.")
                        .contact(new Contact().name("Dmitriy Sushkov")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(schemeName, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")));
    }
}
