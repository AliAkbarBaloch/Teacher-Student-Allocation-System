package com.example.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // This will apply to all endpoints
                .allowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:80",
                    "http://localhost:8080",
                    "http://localhost",
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:80",
                    "http://127.0.0.1:8080",
                    "http://127.0.0.1",
                    "http://frontend:3000",
                    "http://frontend:80",
                    "http://frontend"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 