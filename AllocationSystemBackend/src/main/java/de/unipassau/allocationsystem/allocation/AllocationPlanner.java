package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner component that orchestrates the teacher allocation process.
 * Uses TeacherAllocationService to perform the allocation algorithm.
 * 
 * Usage:
 * - Can be triggered automatically on application startup (if enabled)
 * - Can accept academic year ID as command line argument: java -jar app.jar 1
 * - Or can be called programmatically via the service
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!test")
public class AllocationPlanner implements CommandLineRunner {

    private final AcademicYearRepository academicYearRepository;
    private final TeacherAllocationService teacherAllocationService;
    @Value("${allocation.auto-run.enabled:true}")
    private boolean autoRunEnabled;
    @Value("${allocation.auto-run.academic-year-id:2}")
    private Long autoRunAcademicYearId;

    /**
     * Temporary holder for the academic year ID requested at startup via CLI args.
     * We defer execution to ApplicationReadyEvent so Flyway migrations are finished.
     */
    private Long requestedAcademicYearId;

    /**
     * Executes the allocation process when the application starts.
     * 
     * Command line arguments:
     * - If an argument is provided, it will be used as the academic year ID
     * - If no argument is provided, the allocation will not run automatically
     *   (to prevent accidental execution on startup)
     * 
     * Example: java -jar app.jar 1  (runs allocation for academic year with ID 1)
     * 
     * @param args Command line arguments (first argument can be academic year ID)
     */
    @Override
    public void run(String... args) {
        if (args.length > 0) {
            try {
                requestedAcademicYearId = Long.parseLong(args[0]);
                log.info("Allocation will auto-run after startup using academic year ID from CLI: {}", requestedAcademicYearId);
            } catch (NumberFormatException e) {
                log.error("Invalid academic year ID format: {}. Expected a number.", args[0]);
            }
        } else if (autoRunEnabled) {
            requestedAcademicYearId = autoRunAcademicYearId;
            log.info("Allocation auto-run enabled. Will trigger after startup using academic year ID: {}", requestedAcademicYearId);
        } else {
            log.info("AllocationPlanner ready. Auto-run disabled; provide academic year ID as argument or enable allocation.auto-run.enabled=true.");
        }
    }

    /**
     * Triggers allocation after the application is fully started and Flyway migrations are done.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterMigrations() {
        if (!autoRunEnabled && requestedAcademicYearId == null) {
            log.debug("Allocation auto-run disabled and no CLI academic year provided. Skipping.");
            return;
        }

        Long academicYearIdToUse = requestedAcademicYearId != null ? requestedAcademicYearId : autoRunAcademicYearId;
        if (academicYearIdToUse == null) {
            log.warn("No academic year ID available for allocation run. Skipping.");
            return;
        }

        triggerAllocation(academicYearIdToUse);
    }

    private void triggerAllocation(Long academicYearId) {
        log.info("Starting allocation process for academic year ID: {}", academicYearId);

        if (!academicYearRepository.existsById(academicYearId)) {
            log.error("Academic year with ID {} not found", academicYearId);
            return;
        }

        AllocationPlan allocationPlan = teacherAllocationService.performAllocation(academicYearId);

        log.info("Allocation process completed successfully!");
        log.info("Allocation Plan ID: {}, Status: {}, Version: {}",
                allocationPlan.getId(),
                allocationPlan.getStatus(),
                allocationPlan.getPlanVersion());
    }
}
