package de.unipassau.allocationsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for API-related settings.
 * Configures path matching to automatically prefix all controller endpoints with "/api".
 */
@Configuration
public class ApiConfiguration implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", c -> c.getPackage().getName().contains(".controller"));
    }
}

