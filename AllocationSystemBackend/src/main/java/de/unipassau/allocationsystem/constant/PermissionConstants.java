package de.unipassau.allocationsystem.constant;

/**
 * Constants for permission names and access levels used in the authorization system.
 * Provides standardized identifiers for resources and operations.
 */
public class PermissionConstants {

    /**
     * Resource permission identifiers for different entities in the system.
     */
    public static class Permissions {
        // user
        
        /** Permission identifier for Permission resource. */
        public static final String PERMISSION = "permission";
        
        /** Permission identifier for Role resource. */
        public static final String ROLE = "role";
        
        /** Permission identifier for RolePermission resource. */
        public static final String ROLE_PERMISSION = "rolePermission";
        
        /** Permission identifier for User resource. */
        public static final String USER = "user";

        // base data
        
        /** Permission identifier for AcademicYear resource. */
        public static final String ACADEMIC_YEAR = "academicYear";
        
        /** Permission identifier for AuditLog resource. */
        public static final String AUDIT_LOG = "auditLog";
        
        /** Permission identifier for InternshipType resource. */
        public static final String INTERNSHIP_TYPE = "internshipType";
        
        /** Permission identifier for School resource. */
        public static final String SCHOOL = "school";
        
        /** Permission identifier for SubjectCategory resource. */
        public static final String SUBJECT_CATEGORY = "subjectCategory";
        
        /** Permission identifier for Teacher resource. */
        public static final String TEACHER = "teacher";
        
        /** Permission identifier for TeacherAvailability resource. */
        public static final String TEACHER_AVAILABILITY = "teacherAvailability";
        
        /** Permission identifier for TeacherFormSubmission resource. */
        public static final String TEACHER_FORM_SUBMISSION = "teacherFormSubmission";
        
        /** Permission identifier for AllocationPlan resource. */
        public static final String ALLOCATION_PLAN = "allocationPlan";
        
        /** Permission identifier for ZoneConstraint resource. */
        public static final String ZONE_CONSTRAINT = "zoneConstraint";
        
        /** Permission identifier for PlanChangeLog resource. */
        public static final String PLAN_CHANGE_LOG = "planChangeLog";
        
        /** Permission identifier for TeacherSubject resource. */
        public static final String TEACHER_SUBJECT = "teacherSubject";
        
        /** Permission identifier for TeacherAssignment resource. */
        public static final String TEACHER_ASSIGNMENT = "teacherAssignment";
        
        /** Permission identifier for InternshipDemand resource. */
        public static final String INTERNSHIP_DEMAND = "internshipDemand";
        
        /** Permission identifier for CreditHourTracking resource. */
        public static final String CREDIT_HOUR_TRACKING = "creditHourTracking";

        
        // Report
        
        /** Permission identifier for Dashboard resource. */
        public static final String DASHBOARD = "dashboard";
        
        /** Permission identifier for Report resource. */
        public static final String REPORT = "report";
    }

    /**
     * Access level identifiers for different types of operations.
     */
    public static class AccessLevels {
        /** Access level for view/read operations. */
        public static final String VIEW = "view";
        
        /** Access level for create operations. */
        public static final String CREATE = "create";
        
        /** Access level for update operations. */
        public static final String UPDATE = "update";
        
        /** Access level for delete operations. */
        public static final String DELETE = "delete";
    }
}
