package de.unipassau.allocationsystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Lightweight placeholder service to recalculate and update credit hour tracking for a teacher/year.
 * In the real system this would update a persistent CREDIT_HOUR_TRACKING table or similar.
 */
@Service
@Slf4j
public class CreditHourTrackingService {

    public void recalculateForTeacherAndYear(Long teacherId, Long yearId) {
        // Placeholder: the real implementation would adjust tracked credit hours
        log.debug("Recalculating credit hours for teacherId={} yearId={}", teacherId, yearId);
    }
}
