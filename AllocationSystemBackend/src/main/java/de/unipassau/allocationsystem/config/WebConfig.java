package de.unipassau.allocationsystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    private static final int MAX_CACHE_AGE = 3600;
    public static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // This will apply to all endpoints
                .allowedOriginPatterns(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
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