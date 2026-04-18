package com.oasisplatform.oasisapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig(
    @Value("\${app.frontend.base-url:http://localhost:5173}") private val frontendBaseUrl: String
) {

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        config.allowedOrigins = listOf(frontendBaseUrl)
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true

        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}
