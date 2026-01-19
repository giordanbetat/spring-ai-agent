package com.poc.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevLifeline API - Career & Health Mentor")
                        .version("1.0.0-POC")
                        .description("API do Assistente de IA focado em Carreira de Engenharia de Software e Saúde Ocupacional (Ergonomia/Treino).")
                        .contact(new Contact()
                                .name("Tech Lead Team")
                                .email("tech@devlifeline.com")
                                .url("https://devlifeline.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://spring.io/"))
                )
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:8080")
                                        .description("Servidor Local"),
                                new Server()
                                        .url("https://api.devlifeline.com")
                                        .description("Servidor de Produção (Simulado)")
                        ));
    }
}
