package de.unipassau.allocationsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for Cross-Origin Resource Sharing (CORS) settings.
 * Binds properties with prefix "app.cors" from application configuration files.
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of();
    private List<String> allowedMethods = List.of();

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }
}
