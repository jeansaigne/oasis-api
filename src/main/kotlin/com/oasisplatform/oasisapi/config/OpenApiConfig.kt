package com.oasisplatform.oasisapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Oasis Games API")
                    .description("API REST pour gérer une bibliothèque de jeux")
                    .version("1.0.0")
                    .license(License().name("Private"))
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Local server"),
                    Server().url("https://oasis-api-nujr.onrender.com").description("Production server")
                )
            )
    }
}
