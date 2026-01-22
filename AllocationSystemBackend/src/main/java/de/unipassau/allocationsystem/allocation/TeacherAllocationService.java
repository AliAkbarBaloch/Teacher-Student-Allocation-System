package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for performing teacher allocation.
 * Refactored to use specialized allocation services for each internship type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherAllocationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AllocationDataLoader dataLoader;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SFPAllocationService sfpAllocationService;
    private final ZSPAllocationService zspAllocationService;
    private final PDPAllocationService pdpAllocationService;

    // Self-reference for transactional method calls
    @Autowired
    private TeacherAllocationService self;

    @Transactional
    public AllocationPlan performAllocation(Long academicYearId) {
        return self.performAllocation(academicYearId, false, null);
    }

    @Transactional
    public AllocationPlan performAllocation(Long academicYearId, Boolean isCurrent) {
        return self.performAllocation(academicYearId, isCurrent, null);
    }

    @Transactional
    public AllocationPlan performAllocation(Long academicYearId, Boolean isCurrent, String customVersion) {
        log.info("=== Starting Allocation Process for Academic Year ID: {} ===", academicYearId);
        
        AcademicYear academicYear = validateAcademicYear(academicYearId);
        String version = determineVersion(academicYearId, customVersion);
        AllocationPlan plan = createAllocationPlan(academicYear, version, isCurrent);
        
        clearExistingAssignments(plan.getId());
        LegacyAllocationContext context = buildContext(plan, academicYearId);
        
        logContextStatistics(context);
        
        if (!context.getDemands().isEmpty() && !context.getTeachers().isEmpty()) {
            executeAllocation(context);
            updateCreditHourTracking(plan, academicYear, context.getAssignmentsCount());
        }
        
        finalizePlan(plan);
        logAllocationComplete(plan);
        
        return plan;
    }

    private AcademicYear validateAcademicYear(Long academicYearId) {
        AcademicYear year = entityManager.find(AcademicYear.class, academicYearId);
        if (year == null) {
            throw new IllegalArgumentException("Academic year with ID " + academicYearId + " not found");
        }
        if (Boolean.TRUE.equals(year.getIsLocked())) {
            throw new IllegalArgumentException("Academic year is locked and cannot be modified");
        }
        return year;
    }

    private String determineVersion(Long academicYearId, String customVersion) {
        if (customVersion != null && !customVersion.trim().isEmpty()) {
            log.info("Using custom plan version: {}", customVersion.trim());
            return customVersion.trim();
        }
        String generated = generateNextVersion(academicYearId);
        log.info("Generated plan version: {}", generated);
        return generated;
    }

    private AllocationPlan createAllocationPlan(AcademicYear year, String version, Boolean isCurrent) {
        AllocationPlan plan = new AllocationPlan();
        plan.setAcademicYear(year);
        plan.setPlanName("Allocation Plan for " + year.getYearName() + " v" + version);
        plan.setPlanVersion(version);
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        plan.setIsCurrent(isCurrent != null ? isCurrent : false);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(plan);
        entityManager.flush();
        log.info("Created allocation plan with ID: {}", plan.getId());
        return plan;
    }

    private void clearExistingAssignments(Long planId) {
        teacherAssignmentRepository.deleteAll(
            teacherAssignmentRepository.findByAllocationPlanId(planId, 
                org.springframework.data.domain.Pageable.unpaged()).getContent()
        );
    }

    private LegacyAllocationContext buildContext(AllocationPlan plan, Long academicYearId) {
        Map<Teacher, Integer> assignmentsCount = new HashMap<>();
        Map<Teacher, List<InternshipType>> assignedTypes = new HashMap<>();
        
        List<Teacher> teachers = dataLoader.loadAvailableTeachers(academicYearId);
        for (Teacher teacher : teachers) {
            assignmentsCount.put(teacher, 0);
            assignedTypes.put(teacher, new ArrayList<>());
        }

        return LegacyAllocationContext.builder()
                .allocationPlan(plan)
                .teachers(teachers)
                .demands(dataLoader.loadInternshipDemands(academicYearId))
                .teacherQualifications(dataLoader.loadTeacherQualifications())
                .teacherExclusions(dataLoader.loadTeacherExclusions(academicYearId))
                .teacherAvailabilities(dataLoader.loadTeacherAvailabilities(academicYearId))
                .teacherSubjects(dataLoader.loadTeacherSubjects(academicYearId))
                .zoneConstraints(dataLoader.loadZoneConstraints())
                .assignmentsCount(assignmentsCount)
                .assignedTypes(assignedTypes)
                .combinationRules(dataLoader.loadCombinationRules())
                .build();
    }

    private void logContextStatistics(LegacyAllocationContext context) {
        log.info("Loaded data - Teachers: {}, Demands: {}", 
                context.getTeachers().size(), context.getDemands().size());
        
        if (context.getDemands().isEmpty()) {
            log.warn("WARNING: No internship demands found. No assignments will be created.");
        } else {
            Map<String, Long> demandsByType = context.getDemands().stream()
                .collect(Collectors.groupingBy(
                    d -> d.getInternshipType().getInternshipCode(), Collectors.counting()));
            log.info("Demands breakdown: {}", demandsByType);
        }
        
        if (context.getTeachers().isEmpty()) {
            log.warn("WARNING: No available teachers found. No assignments will be created.");
        }
    }

    private void executeAllocation(LegacyAllocationContext context) {
        List<InternshipType> types = dataLoader.loadInternshipTypes();
        
        allocateForType(context, types, "SFP", sfpAllocationService);
        allocateForType(context, types, "ZSP", zspAllocationService);
        allocateForType(context, types, "PDP1", pdpAllocationService);
        allocateForType(context, types, "PDP2", pdpAllocationService);
    }

    private void allocateForType(LegacyAllocationContext context, List<InternshipType> types, 
                                  String code, Object service) {
        InternshipType type = getInternshipTypeByCode(code, types);
        if (type != null) {
            List<InternshipDemand> demands = filterDemandsByType(context.getDemands(), type.getId());
            log.info("=== Starting {} Allocation === (Demands: {})", code, demands.size());
            
            if (!demands.isEmpty()) {
                LegacyAllocationContext typeContext = createTypeContext(context, demands);
                
                if (service instanceof SFPAllocationService) {
                    ((SFPAllocationService) service).allocate(typeContext);
                } else if (service instanceof ZSPAllocationService) {
                    ((ZSPAllocationService) service).allocate(typeContext);
                } else if (service instanceof PDPAllocationService) {
                    ((PDPAllocationService) service).allocate(typeContext);
                }
            }
        } else {
            log.warn("{} InternshipType not found!", code);
        }
    }

    private List<InternshipDemand> filterDemandsByType(List<InternshipDemand> demands, Long typeId) {
        return demands.stream()
                .filter(d -> d.getInternshipType().getId().equals(typeId))
                .collect(Collectors.toList());
    }

    private LegacyAllocationContext createTypeContext(LegacyAllocationContext original, 
                                                      List<InternshipDemand> typeDemands) {
        return LegacyAllocationContext.builder()
                .allocationPlan(original.getAllocationPlan())
                .teachers(original.getTeachers())
                .demands(typeDemands)
                .teacherQualifications(original.getTeacherQualifications())
                .teacherExclusions(original.getTeacherExclusions())
                .teacherAvailabilities(original.getTeacherAvailabilities())
                .teacherSubjects(original.getTeacherSubjects())
                .zoneConstraints(original.getZoneConstraints())
                .assignmentsCount(original.getAssignmentsCount())
                .assignedTypes(original.getAssignedTypes())
                .combinationRules(original.getCombinationRules())
                .build();
    }

    private void updateCreditHourTracking(AllocationPlan plan, AcademicYear year, 
                                           Map<Teacher, Integer> assignmentsCount) {
        entityManager.createQuery("DELETE FROM CreditHourTracking c WHERE c.academicYear.id = :yearId")
                .setParameter("yearId", year.getId())
                .executeUpdate();

        for (Map.Entry<Teacher, Integer> entry : assignmentsCount.entrySet()) {
            if (entry.getValue() > 0) {
                CreditHourTracking tracking = new CreditHourTracking();
                tracking.setTeacher(entry.getKey());
                tracking.setAcademicYear(year);
                tracking.setAssignmentsCount(entry.getValue());
                tracking.setCreditHoursAllocated(entry.getValue() >= 2 ? 1.0 : 0.0);
                tracking.setCreditBalance(0.0);
                tracking.setNotes("Allocation Plan v" + plan.getPlanVersion());
                tracking.setCreatedAt(LocalDateTime.now());
                entityManager.persist(tracking);
            }
        }
    }

    private void finalizePlan(AllocationPlan plan) {
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setStatus(AllocationPlan.PlanStatus.APPROVED);
        entityManager.merge(plan);
        entityManager.flush();
    }

    private void logAllocationComplete(AllocationPlan plan) {
        Long assignmentCount = entityManager.createQuery(
                "SELECT COUNT(t) FROM TeacherAssignment t WHERE t.allocationPlan.id = :planId", Long.class)
                .setParameter("planId", plan.getId())
                .getSingleResult();
        log.info("Allocation completed. Plan ID: {}, Total assignments created: {}", 
                plan.getId(), assignmentCount);
    }

    private InternshipType getInternshipTypeByCode(String code, List<InternshipType> types) {
        return types.stream()
                .filter(t -> t.getInternshipCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    private String generateNextVersion(Long academicYearId) {
        List<AllocationPlan> plans = entityManager.createQuery(
                "SELECT ap FROM AllocationPlan ap WHERE ap.academicYear.id = :yearId ORDER BY ap.id DESC",
                AllocationPlan.class)
                .setParameter("yearId", academicYearId)
                .getResultList();

        if (plans.isEmpty()) {
            return "1.0";
        }

        int maxVersion = plans.stream()
                .map(AllocationPlan::getPlanVersion)
                .filter(v -> v != null && v.matches("\\d+\\.\\d+"))
                .mapToInt(v -> {
                    try {
                        return Integer.parseInt(v.split("\\.")[0]);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return (maxVersion + 1) + ".0";
    }
}

