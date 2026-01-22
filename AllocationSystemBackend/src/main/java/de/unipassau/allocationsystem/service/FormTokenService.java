package de.unipassau.allocationsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling form token generation, encoding, and decoding.
 * Extracted from TeacherFormSubmissionService for better separation of concerns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FormTokenService {

    private final TeacherFormSubmissionRepository teacherFormSubmissionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Decoded token payload containing the teacher ID and academic year ID.
     */
    public static class TokenData {
        private final Long teacherId;
        private final Long yearId;

        /**
         * Creates a decoded token data holder.
         *
         * @param teacherId teacher identifier
         * @param yearId academic year identifier
         */
        public TokenData(Long teacherId, Long yearId) {
            this.teacherId = teacherId;
            this.yearId = yearId;
        }

        public Long getTeacherId() {
            return teacherId;
        }

        public Long getYearId() {
            return yearId;
        }
    }

    /**
     * Generate a unique form token encoding teacherId and yearId.
     * Format: base64url(json({teacherId, yearId, uuid}))
     * Retries up to 5 times if token collision occurs.
     *
     * @param teacherId The teacher ID
     * @param yearId The academic year ID
     * @return A unique form token
     * @throws IllegalStateException if token uniqueness cannot be ensured (extremely unlikely)
     */
    public String generateUniqueFormToken(Long teacherId, Long yearId) {
        final int maxRetries = 5;

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("teacherId", teacherId);
        tokenData.put("yearId", yearId);

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            tokenData.put("uuid", UUID.randomUUID().toString());
            String formToken = encodeToken(tokenData);

            if (!teacherFormSubmissionRepository.existsByFormToken(formToken)) {
                return formToken;
            }

            log.warn("Token collision detected on attempt {} for teacherId: {}, yearId: {}",
                    attempt + 1, teacherId, yearId);
        }

        log.error("Failed to generate unique token after {} attempts for teacherId: {}, yearId: {}",
                maxRetries, teacherId, yearId);
        throw new IllegalStateException("Failed to generate unique form token after " + maxRetries + " attempts");
    }

    /**
     * Encode token data to a Base64 URL-safe string.
     *
     * @param tokenData The token data map
     * @return Base64 URL-safe encoded token string
     * @throws IllegalStateException if encoding fails
     */
    public String encodeToken(Map<String, Object> tokenData) {
        try {
            String jsonData = objectMapper.writeValueAsString(tokenData);
            byte[] bytes = jsonData.getBytes(StandardCharsets.UTF_8);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (JsonProcessingException e) {
            log.error("Failed to encode form token", e);
            throw new IllegalStateException("Failed to encode form token", e);
        }
    }

    /**
     * Decode a form token and extract teacherId and yearId.
     *
     * @param formToken The form token to decode
     * @return TokenData containing teacherId and yearId
     * @throws IllegalArgumentException if token is invalid or corrupted
     */
    public TokenData decodeFormToken(String formToken) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(formToken);
            String jsonData = new String(decodedBytes, StandardCharsets.UTF_8);

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenData = objectMapper.readValue(jsonData, Map.class);

            Long teacherId = parseRequiredLong(tokenData, "teacherId");
            Long yearId = parseRequiredLong(tokenData, "yearId");

            return new TokenData(teacherId, yearId);

        } catch (IllegalArgumentException e) {
            // Base64 decode throws IllegalArgumentException for invalid input
            log.error("Failed to decode form token (invalid base64)", e);
            throw new IllegalArgumentException("Invalid or corrupted form token", e);
        } catch (IOException e) {
            // Jackson readValue throws IOException
            log.error("Failed to decode form token (invalid json)", e);
            throw new IllegalArgumentException("Invalid or corrupted form token", e);
        }
    }

    private Long parseRequiredLong(Map<String, Object> tokenData, String key) {
        Object value = tokenData.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Invalid or corrupted form token");
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid or corrupted form token", e);
        }
    }
}
