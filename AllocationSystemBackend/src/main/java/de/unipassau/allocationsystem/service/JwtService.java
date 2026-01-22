package de.unipassau.allocationsystem.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token operations: generation, validation, and extraction.
 *
 * Notes:
 * - Avoids java.util.Date entirely by setting standard JWT time claims (iat/exp) as epoch-seconds numbers.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Expiration duration in milliseconds.
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Generate JWT token for a user.
     */
    public String generateToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails.getUsername());
    }

    /**
     * Generate token with custom claims.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Create JWT token with claims and subject.
     * Uses numeric date claims:
     * - iat: epoch seconds
     * - exp: epoch seconds
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expiration);

        Map<String, Object> mergedClaims = new HashMap<>(claims);
        mergedClaims.put(Claims.ISSUED_AT, now.getEpochSecond());
        mergedClaims.put(Claims.EXPIRATION, expiresAt.getEpochSecond());

        return Jwts.builder()
                .claims(mergedClaims)
                .subject(subject)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract username (subject) from token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration timestamp from token as Instant.
     * Supports both numeric and Date-based exp representations (in case older tokens exist).
     */
    public Instant extractExpirationInstant(String token) {
        Claims claims = extractAllClaims(token);

        Object exp = claims.get(Claims.EXPIRATION);
        if (exp instanceof Number n) {
            return Instant.ofEpochSecond(n.longValue());
        }

        // Fallback for unexpected token formats without referencing java.util.Date:
        // attempt to parse as string seconds
        if (exp instanceof String s) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(s));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }

        throw new IllegalStateException("Unsupported 'exp' claim type: " + (exp == null ? "null" : exp.getClass()));
    }

    /**
     * Extract a specific claim from token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpirationInstant(token).isBefore(Instant.now());
    }

    /**
     * Validate token against user details.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Get signing key from secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
