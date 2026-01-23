package de.unipassau.allocationsystem.constant;

/**
 * Constants for entity names used in audit logging.
 * Provides standardized entity name identifiers for tracking changes across the system.
 */
public final class AuditEntityNames {
    // user
    
    /** Entity name for Permission records. */
    public static final String PERMISSION = "PERMISSION";
    
    /** Entity name for Role records. */
    public static final String ROLE = "ROLE";
    
    /** Entity name for RolePermission association records. */
    public static final String ROLE_PERMISSION = "ROLE_PERMISSION";
    
    /** Entity name for User records. */
    public static final String USER = "USER";

    // BASE DATA
    
    /** Entity name for AcademicYear records. */
    public static final String ACADEMIC_YEAR = "ACADEMIC_YEAR";
    
    /** Entity name for AuditLog records. */
    public static final String AUDIT_LOG = "AUDIT_LOG";
    
    /** Entity name for InternshipType records. */
    public static final String INTERNSHIP_TYPE = "INTERNSHIP_TYPE";
    
    /** Entity name for School records. */
    public static final String SCHOOL = "SCHOOL";
    
    /** Entity name for Subject records. */
    public static final String SUBJECT = "SUBJECT";
    
    /** Entity name for SubjectCategory records. */
    public static final String SUBJECT_CATEGORY = "SUBJECT_CATEGORY";
    
    /** Entity name for Teacher records. */
    public static final String TEACHER = "TEACHER";
    
    /** Entity name for TeacherAvailability records. */
    public static final String TEACHER_AVAILABILITY = "TEACHER_AVAILABILITY";
    
    /** Entity name for TeacherFormSubmission records. */
    public static final String TEACHER_FORM_SUBMISSION = "TEACHER_FORM_SUBMISSION";
    
    /** Entity name for AllocationPlan records. */
    public static final String ALLOCATION_PLAN = "ALLOCATION_PLAN";
    
    /** Entity name for ZoneConstraint records. */
    public static final String ZONE_CONSTRAINT = "ZONE_CONSTRAINT";
    
    /** Entity name for PlanChangeLog records. */
    public static final String PLAN_CHANGE_LOG = "PLAN_CHANGE_LOG";
    
    /** Entity name for TeacherSubject records. */
    public static final String TEACHER_SUBJECT = "TEACHER_SUBJECT";
    
    /** Entity name for TeacherAssignment records. */
    public static final String TEACHER_ASSIGNMENT = "TEACHER_ASSIGNMENT";
    
    /** Entity name for InternshipDemand records. */
    public static final String INTERNSHIP_DEMAND = "INTERNSHIP_DEMAND";
    
    /** Entity name for CreditHourTracking records. */
    public static final String CREDIT_HOUR_TRACKING = "CREDIT_HOUR_TRACKING";


    // Report
    
    /** Entity name for Dashboard operations. */
    public static final String DASHBOARD = "DASHBOARD";
    
    /** Entity name for Report operations. */
    public static final String REPORT = "REPORT";

    // ...add all used entity names here

    private AuditEntityNames() {

    }
}