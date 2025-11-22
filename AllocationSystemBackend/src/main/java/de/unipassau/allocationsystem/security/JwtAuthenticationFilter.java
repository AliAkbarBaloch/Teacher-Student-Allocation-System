package de.unipassau.allocationsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.service.CustomUserDetailsService;
import de.unipassau.allocationsystem.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet filter for processing JWT-based authentication on incoming requests.
 * <p>
 * This filter intercepts HTTP requests, extracts and validates JWT tokens provided in the Authorization header,
 * and, upon successful validation, sets the authenticated user into the Spring Security context.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Checks for the presence of a Bearer JWT token in the "Authorization" header.</li>
 *   <li>Validates the JWT and extracts the username using {@link JWTUtil}.</li>
 *   <li>Loads user details from the database with {@link CustomUserDetailsService}.</li>
 *   <li>If the token is valid and matches the user, sets up security authentication for the request.</li>
 *   <li>For failed JWT validation, returns a JSON error response with 401 status.</li>
 *   <li>Skips authentication for endpoints starting with "/api/auth/".</li>
 * </ul>
 * <p>
 * Typically used to secure REST API endpoints in a Spring Boot application, ensuring that only requests
 * with valid JWTs can access protected resources.
*/
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX_LENGTH).trim();
        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (JwtException e) {
            sendErrorResponse(response, "Invalid JWT token: " + e.getMessage());
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT filter for public auth endpoints and public form submission endpoints
        // But NOT for protected endpoints like profile, logout, change-password
        return path.equals("/api/auth/login") || 
               path.equals("/api/auth/forgot-password") || 
               path.equals("/api/auth/reset-password") ||
               path.startsWith("/api/public/teacher-form-submission/");
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", message);

        new ObjectMapper().writeValue(response.getWriter(), errorDetails);
    }
}