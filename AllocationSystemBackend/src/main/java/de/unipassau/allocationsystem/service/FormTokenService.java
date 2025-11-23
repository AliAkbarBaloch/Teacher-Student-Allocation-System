package de.unipassau.allocationsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
     * Inner class to hold decoded token data.
     */
    public static class TokenData {
        private final Long teacherId;
        private final Long yearId;

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
     * Format: base64(json({teacherId, yearId, uuid}))
     * Retries up to 5 times if token collision occurs.
     *
     * @param teacherId The teacher ID
     * @param yearId The academic year ID
     * @return A unique form token
     */
    public String generateUniqueFormToken(Long teacherId, Long yearId) {
        final int maxRetries = 5;
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("teacherId", teacherId);
        tokenData.put("yearId", yearId);

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            tokenData.put("uuid", UUID.randomUUID().toString());
            String formToken = encodeToken(tokenData);

            // Verify token is unique (should be extremely rare collision)
            if (!teacherFormSubmissionRepository.existsByFormToken(formToken)) {
                return formToken;
            }

            log.warn("Token collision detected on attempt {} for teacherId: {}, yearId: {}",
                    attempt + 1, teacherId, yearId);
        }

        // If all retries failed (extremely unlikely), throw exception
        log.error("Failed to generate unique token after {} attempts for teacherId: {}, yearId: {}",
                maxRetries, teacherId, yearId);
        throw new RuntimeException("Failed to generate unique form token after " + maxRetries + " attempts");
    }

    /**
     * Encode token data to base64 URL-safe string.
     *
     * @param tokenData The token data map
     * @return Base64 URL-safe encoded token string
     */
    public String encodeToken(Map<String, Object> tokenData) {
        try {
            String jsonData = objectMapper.writeValueAsString(tokenData);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(jsonData.getBytes());
        } catch (Exception e) {
            log.error("Failed to encode form token", e);
            throw new RuntimeException("Failed to generate form token", e);
        }
    }

    /**
     * Decode form token and extract teacherId and yearId.
     *
     * @param formToken The form token to decode
     * @return TokenData containing teacherId and yearId
     * @throws IllegalArgumentException if token is invalid or corrupted
     */
    public TokenData decodeFormToken(String formToken) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(formToken);
            String jsonData = new String(decodedBytes);
            Map<String, Object> tokenData = objectMapper.readValue(jsonData, Map.class);

            Long teacherId = Long.valueOf(tokenData.get("teacherId").toString());
            Long yearId = Long.valueOf(tokenData.get("yearId").toString());

            return new TokenData(teacherId, yearId);
        } catch (Exception e) {
            log.error("Failed to decode form token", e);
            throw new IllegalArgumentException("Invalid or corrupted form token");
        }
    }
}

