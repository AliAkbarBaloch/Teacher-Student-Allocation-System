package de.unipassau.allocationsystem.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.function.Function;

/**
 * Utility class for handling JSON Web Tokens (JWT) in the allocation system.
 * <p>
 * Provides methods to generate, parse, and validate JWT tokens using symmetric signing (HS256).
 * The class can extract claims (such as username and expiration), create new tokens for authenticated users,
 * and verify tokens' integrity and expiration status.
 * <p>
 * Configuration values for JWT secret and expiration time are injected using Spring's @Value annotation.
 * <p>
 * Main functionalities include:
 * <ul>
 *   <li>Generating JWT tokens for Spring Security UserDetails</li>
 *   <li>Extracting username and expiration date from a token</li>
 *   <li>Validating tokens against their expiration and expected username</li>
 *   <li>Internal claim resolution for custom use cases</li>
 * </ul>
 */
@Slf4j
@Component
public class JWTUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Extracts the username (subject) from the JWT token.
     * 
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     * 
     * @param token the JWT token
     * @return the expiration instant
     */
    public Instant extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration().toInstant());
    }

    /**
     * Extracts a specific claim from the JWT token.
     * 
     * @param <T> the type of the claim value
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(Instant.now());
    }

    /**
     * Validates a JWT token against user details.
     * Checks if the token username matches and if the token is not expired.
     * 
     * @param token the JWT token
     * @param userDetails the user details to validate against
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        return validateTokenInternal(token, username -> username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validates a JWT token's signature and structure.
     * 
     * @param token the JWT token
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        return validateTokenInternal(token, username -> true);
    }
    
    private Boolean validateTokenInternal(String token, java.util.function.Predicate<String> additionalCheck) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            String username = extractUsername(token);
            return additionalCheck.test(username);
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
