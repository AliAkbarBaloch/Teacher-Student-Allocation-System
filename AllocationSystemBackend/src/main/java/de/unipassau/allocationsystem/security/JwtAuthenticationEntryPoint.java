package de.unipassau.allocationsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry point for handling unauthorized access attempts with JWT-based authentication.
 * <p>
 * This component is triggered when a request to a secured endpoint fails authentication,
 * typically due to missing or invalid JWT tokens. It responds by sending a structured JSON
 * error response with HTTP status 401 (Unauthorized).
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Intercept unauthorized requests and prevent access to protected resources.</li>
 *   <li>Return a consistent JSON error object including status code, error type, exception message, and request path.</li>
 *   <li>Integrates with Spring Security's authentication framework as an {@link AuthenticationEntryPoint}.</li>
 * </ul>
 * <p>
 * This improves API error handling and allows clients (such as frontends) to display meaningful messages for authentication failures.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
