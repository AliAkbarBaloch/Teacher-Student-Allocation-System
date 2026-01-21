package de.unipassau.allocationsystem.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import de.unipassau.allocationsystem.security.JwtAuthenticationEntryPoint;
import de.unipassau.allocationsystem.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

/**
 * Configures Spring Security for the allocation system application.
 * <p>
 * This class defines security policies, authentication mechanisms, and filters to manage user access and protect API endpoints.
 * It integrates JWT authentication for stateless security and customizes the filter chain for fine-grained control.
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Enables web and method-level security annotations via EnableWebSecurity and EnableMethodSecurity.</li>
 *   <li>Sets up the CustomUserDetailsService for loading user details from the database.</li>
 *   <li>Configures an AuthenticationProvider using BCrypt password encoding.</li>
 *   <li>Defines JWT authentication and exception handling using JwtAuthenticationFilter and JwtAuthenticationEntryPoint.</li>
 *   <li>Establishes CORS rules to control allowed origins, methods, and headers for cross-domain requests.</li>
 *   <li>Configures HTTP security policy:
 *     - Allows unauthenticated access to certain endpoints (authentication, public APIs, H2 console).
 *     - Secures all other /api/** routes to require authentication.
 *     - Permits all other routes for frontend SPA compatibility.
 *     - Disables CSRF (for stateless JWT usage) and restricts frame options for H2 console access.
 *     - Sets session creation policy to stateless, suitable for JWT authentication.
 *   </li>
 * </ul>
 * <p>
 * The configuration ensures robust security for RESTful endpoints, while maintaining flexibility for public routes and development tools.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsProperties corsProperties;

    @Value("${app.security.permit-all-url-patterns}")
    private String permitAllUrlPatterns;

    /**
     * Provides BCrypt password encoder for secure password hashing.
     * 
     * @return PasswordEncoder instance using BCrypt algorithm
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the authentication manager for processing authentication requests.
     * 
     * @param config Authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures CORS settings for cross-origin requests.
     * Allows specified origins, methods, and headers based on application properties.
     * 
     * @return CorsConfigurationSource with configured CORS rules
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the security filter chain for HTTP requests.
     * Defines authorization rules, CORS, CSRF, session management, and JWT filter integration.
     * 
     * @param http HttpSecurity configuration object
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String[] patterns = permitAllUrlPatterns.split(",");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(patterns).permitAll()
                        .requestMatchers("/api/meta/**").permitAll()   
                        .requestMatchers("/api/**", "/reports/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
