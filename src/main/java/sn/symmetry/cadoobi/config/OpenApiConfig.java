package sn.symmetry.cadoobi.config;

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
public class OpenApiConfig {

    @Bean
    public OpenAPI cadoobiTransactionsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cadoobi Transactions API")
                        .description("REST API for managing payments, gift cards, and operator configurations in the Cadoobi platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Symmetry SN")
                                .email("contact@symmetry.sn")
                                .url("https://symmetry.sn"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://symmetry.sn/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development server"),
                        new Server()
                                .url("https://api.cadoobi.com")
                                .description("Production server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from POST /auth/login")));
    }
}
