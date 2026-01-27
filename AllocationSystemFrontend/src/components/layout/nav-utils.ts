import { ROUTES } from "@/config/routes";

/**
 * Interface representing a navigation item in the system.
 */
export interface NavItem {
    /** The navigation path. If not provided, the item behaves as a parent for a submenu. */
    path?: string;
    /** The translation key for the item's label. */
    translationKey: string;
    /** Optional collection of sub-navigation items. */
    submenu?: NavItem[];
}

/**
 * Global navigation items configuration.
 */
export const NAV_ITEMS: NavItem[] = [
    { path: ROUTES.main.dashboard, translationKey: "navigation.dashboard" },
    {
        path: ROUTES.main.allocationReport,
        translationKey: "navigation.allocationReport",
    },
    {
        translationKey: "navigation.baseData",
        submenu: [
            {
                path: ROUTES.baseData.academicYears,
                translationKey: "navigation.baseDataAcademicYears",
            },
            {
                path: ROUTES.baseData.subjectCategories,
                translationKey: "navigation.baseDataSubjectCategories",
            },
            {
                path: ROUTES.baseData.subjects,
                translationKey: "navigation.baseDataSubjects",
            },
            {
                path: ROUTES.baseData.schools,
                translationKey: "navigation.baseDataSchools",
            },
            {
                path: ROUTES.baseData.teachers,
                translationKey: "navigation.baseDataTeachers",
            },
            {
                path: ROUTES.baseData.internshipTypes,
                translationKey: "navigation.baseDataInternshipTypes",
            },
            {
                path: ROUTES.baseData.zoneConstraints,
                translationKey: "navigation.baseDataZoneConstraints",
            },
        ],
    },
    {
        translationKey: "navigation.teacherManagement",
        submenu: [
            {
                path: ROUTES.teacherManagement.teacherSubjects,
                translationKey: "navigation.teacherManagementTeacherSubjects",
            },
            {
                path: ROUTES.teacherManagement.teacherAvailability,
                translationKey: "navigation.teacherManagementTeacherAvailability",
            },
            {
                path: ROUTES.teacherManagement.teacherSubmissions,
                translationKey: "navigation.teacherManagementTeacherSubmissions",
            },
        ],
    },
    {
        translationKey: "navigation.internshipDemand",
        submenu: [
            {
                path: ROUTES.internshipDemand.demandPerYear,
                translationKey: "navigation.internshipDemandDemandPerYear",
            },
        ],
    },
    {
        translationKey: "navigation.allocationPlanning",
        submenu: [
            {
                path: ROUTES.allocationPlanning.allocationPlans,
                translationKey: "navigation.allocationPlanningAllocationPlans",
            },
            {
                path: ROUTES.allocationPlanning.teacherAssignments,
                translationKey: "navigation.allocationPlanningTeacherAssignments",
            },
            {
                path: ROUTES.allocationPlanning.creditHourTracking,
                translationKey: "navigation.allocationPlanningCreditHourTracking",
            },
            {
                path: ROUTES.allocationPlanning.planChangeLogs,
                translationKey: "navigation.allocationPlanningPlanChangeLogs",
            },
        ],
    },
    {
        translationKey: "navigation.reports",
        submenu: [
            {
                path: ROUTES.reports.allocationReports,
                translationKey: "navigation.reportsAllocationReports",
            },
            {
                path: ROUTES.reports.teacherReports,
                translationKey: "navigation.reportsTeacherReports",
            },
            {
                path: ROUTES.reports.schoolReports,
                translationKey: "navigation.reportsSchoolReports",
            },
            {
                path: ROUTES.reports.auditReports,
                translationKey: "navigation.reportsAuditReports",
            },
        ],
    },
];
