package de.unipassau.allocationsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final int MAX_CACHE_AGE = 3600;
    public static final List<String> ALLOWED_ORIGINS = Arrays.asList(
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
    );

    public static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // This will apply to all endpoints
                .allowedOriginPatterns(String.valueOf(ALLOWED_ORIGINS))
                .allowedMethods(String.valueOf(ALLOWED_METHODS))
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(MAX_CACHE_AGE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Do not register any resource handlers to suppress static file serving
    }
} 