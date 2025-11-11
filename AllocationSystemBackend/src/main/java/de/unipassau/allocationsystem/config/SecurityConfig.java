package de.unipassau.allocationsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

/**
 * Development-friendly security configuration to allow using the H2 console.
 * This permits access to /h2-console/** and relaxes frame options and CSRF for that path.
 * Do NOT enable this in production.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Allow frames for H2 console
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Disable CSRF for H2 console so the console can post forms
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .authorizeHttpRequests(auth -> auth
                // permit H2 console paths
                .requestMatchers("/h2-console/**").permitAll()
                // require authentication for other requests (default)
                .anyRequest().authenticated()
            )
            // keep default form login / http basic for other endpoints
            .formLogin()
            .and()
            .httpBasic();
            // disable CSRF for local testing so Swagger UI can call POST/PUT without token
            .csrf(csrf -> csrf.disable())
            // allow anonymous access to swagger and OpenAPI endpoints for dev
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/v3/api-docs",
                    "/openapi.yaml",
                    "/openapi.yaml/**"
                ).permitAll()
                // allow anonymous access to API endpoints for local testing
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
