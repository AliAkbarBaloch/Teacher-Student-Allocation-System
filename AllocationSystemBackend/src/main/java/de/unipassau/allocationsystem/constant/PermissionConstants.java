package de.unipassau.allocationsystem.constant;

public class PermissionConstants {

    public static class Permissions {
        // user
        public static final String PERMISSION = "permission";
        public static final String ROLE = "role";
        public static final String ROLE_PERMISSION = "rolePermission";
        public static final String USER = "user";

        // base data
        public static final String ACADEMIC_YEAR = "academicYear";
        public static final String AUDIT_LOG = "auditLog";
        public static final String INTERNSHIP_TYPE = "internshipType";
        public static final String SCHOOL = "school";
        public static final String SUBJECT_CATEGORY = "subjectCategory";
        public static final String TEACHER = "teacher";
        public static final String TEACHER_AVAILABILITY = "teacherAvailability";
        public static final String TEACHER_FORM_SUBMISSION = "teacherFormSubmission";

        // Report
        public static final String DASHBOARD = "dashboard";
        public static final String REPORT = "report";
    }

    public static class AccessLevels {
        public static final String VIEW = "view";
        public static final String CREATE = "create";
        public static final String UPDATE = "update";
        public static final String DELETE = "delete";
    }
}
