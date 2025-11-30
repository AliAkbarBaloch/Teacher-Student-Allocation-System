export const ROUTES = {
  auth: {
    login: "/login",
    forgotPassword: "/forgot-password",
    resetPassword: "/reset-password",
  },
  main: {
    home: "/home",
    dashboard: "/dashboard",
    settings: "/settings",
  },
  baseData: {
    academicYears: "/base-data/academic-years",
    subjectCategories: "/base-data/subject-categories",
    subjects: "/base-data/subjects",
    schools: "/base-data/schools",
    teachers: "/base-data/teachers",
    internshipTypes: "/base-data/internship-types",
  },
  teacherManagement: {
    teacherSubjects: "/teacher-management/teacher-subjects",
    teacherAvailability: "/teacher-management/teacher-availability",
    teacherFormSubmissions: "/teacher-management/teacher-form-submissions",
  },
  internshipDemand: {
    demandPerYear: "/internship-demand",
  },
  allocationPlanning: {
    allocationPlans: "/allocation-planning/allocation-plans",
    teacherAssignments: "/allocation-planning/teacher-assignments",
    creditHourTracking: "/allocation-planning/credit-hour-tracking",
    zoneConstraints: "/allocation-planning/zone-constraints",
    planChangeLogs: "/allocation-planning/plan-change-logs",
  },
  reports: {
    allocationReports: "/reports/allocation-reports",
    teacherReports: "/reports/teacher-reports",
    schoolReports: "/reports/school-reports",
    auditReports: "/reports/audit-reports",
  },
  admin: {
    users: "/admin/users",
    createUser: "/admin/users/create",
    editUser: "/admin/users/edit/:id",
    roles: "/settings/admin/roles",
  },
} as const;
