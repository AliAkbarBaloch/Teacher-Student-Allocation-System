import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "@/config/routes";

/**
 * Interface for breadcrumb navigation items
 * @interface BreadcrumbItem
 */
export interface BreadcrumbItem {
    label: string;
    path?: string;
}

/**
 * Type for translation function
 */
type TranslationFunction = (key: string) => string;

/**
 * Handles dashboard and home routes
 */
const handleDashboardRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (path === ROUTES.main.dashboard || path === ROUTES.main.home) {
        return [{ label: t("navigation.dashboard") }];
    }
    return null;
};

/**
 * Handles settings routes
 */
const handleSettingsRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (path === ROUTES.main.settings) {
        return [{ label: t("userMenu.settings") }];
    }
    return null;
};

/**
 * Handles base data routes
 */
const handleBaseDataRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (!path.startsWith("/base-data")) {
        return null;
    }

    const crumbs: BreadcrumbItem[] = [
        { label: t("navigation.baseData"), path: ROUTES.baseData.academicYears }
    ];

    if (path === ROUTES.baseData.academicYears) {
        crumbs.push({ label: t("navigation.baseDataAcademicYears") });
    } else if (path === ROUTES.baseData.subjectCategories) {
        crumbs.push({ label: t("navigation.baseDataSubjectCategories") });
    } else if (path === ROUTES.baseData.subjects) {
        crumbs.push({ label: t("navigation.baseDataSubjects") });
    } else if (path === ROUTES.baseData.schools) {
        crumbs.push({ label: t("navigation.baseDataSchools") });
    } else if (path === ROUTES.baseData.teachers) {
        crumbs.push({ label: t("navigation.baseDataTeachers") });
    } else if (path === ROUTES.baseData.internshipTypes) {
        crumbs.push({ label: t("navigation.baseDataInternshipTypes") });
    } else if (path === ROUTES.baseData.zoneConstraints) {
        crumbs.push({ label: t("navigation.baseDataZoneConstraints") });
    }

    return crumbs;
};

/**
 * Handles teacher management routes
 */
const handleTeacherManagementRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (!path.startsWith("/teacher-management")) {
        return null;
    }

    const crumbs: BreadcrumbItem[] = [
        { label: t("navigation.teacherManagement"), path: ROUTES.teacherManagement.teacherSubjects }
    ];

    if (path === ROUTES.teacherManagement.teacherSubjects) {
        crumbs.push({ label: t("navigation.teacherManagementTeacherSubjects") });
    } else if (path === ROUTES.teacherManagement.teacherAvailability) {
        crumbs.push({ label: t("navigation.teacherManagementTeacherAvailability") });
    } else if (path === ROUTES.teacherManagement.teacherSubmissions) {
        crumbs.push({ label: t("navigation.teacherManagementTeacherSubmissions") });
    }

    return crumbs;
};

/**
 * Handles internship demand routes
 */
const handleInternshipDemandRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (path === ROUTES.internshipDemand.demandPerYear) {
        return [{ label: t("navigation.internshipDemand") }];
    }
    return null;
};

/**
 * Handles allocation planning routes
 */
const handleAllocationPlanningRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (!path.startsWith("/allocation-planning")) {
        return null;
    }

    const crumbs: BreadcrumbItem[] = [
        { label: t("navigation.allocationPlanning"), path: ROUTES.allocationPlanning.allocationPlans }
    ];

    if (path === ROUTES.allocationPlanning.allocationPlans) {
        crumbs.push({ label: t("navigation.allocationPlanningAllocationPlans") });
    } else if (path === ROUTES.allocationPlanning.teacherAssignments) {
        crumbs.push({ label: t("navigation.allocationPlanningTeacherAssignments") });
    } else if (path === ROUTES.allocationPlanning.creditHourTracking) {
        crumbs.push({ label: t("navigation.allocationPlanningCreditHourTracking") });
    } else if (path === ROUTES.allocationPlanning.planChangeLogs) {
        crumbs.push({ label: t("navigation.allocationPlanningPlanChangeLogs") });
    }

    return crumbs;
};

/**
 * Handles reports routes
 */
const handleReportsRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (!path.startsWith("/reports")) {
        return null;
    }

    const crumbs: BreadcrumbItem[] = [
        { label: t("navigation.reports"), path: ROUTES.reports.allocationReports }
    ];

    if (path === ROUTES.reports.allocationReports) {
        crumbs.push({ label: t("navigation.reportsAllocationReports") });
    } else if (path === ROUTES.reports.teacherReports) {
        crumbs.push({ label: t("navigation.reportsTeacherReports") });
    } else if (path === ROUTES.reports.schoolReports) {
        crumbs.push({ label: t("navigation.reportsSchoolReports") });
    } else if (path === ROUTES.reports.auditReports) {
        crumbs.push({ label: t("navigation.reportsAuditReports") });
    }

    return crumbs;
};

/**
 * Handles admin routes
 */
const handleAdminRoutes = (path: string, t: TranslationFunction): BreadcrumbItem[] | null => {
    if (!path.startsWith("/admin")) {
        return null;
    }

    const crumbs: BreadcrumbItem[] = [
        { label: t("navigation.groups.admin") }
    ];

    if (path === ROUTES.admin.users) {
        crumbs.push({ label: t("navigation.users") });
    } else if (path === ROUTES.admin.roles) {
        crumbs.push({ label: t("navigation.roles") });
    } else {
        crumbs.push({ label: path });
    }

    return crumbs;
};

/**
 * Main breadcrumb generation logic
 */
const generateBreadcrumbs = (path: string, t: TranslationFunction): BreadcrumbItem[] => {
    const handlers = [
        handleDashboardRoutes,
        handleSettingsRoutes,
        handleBaseDataRoutes,
        handleTeacherManagementRoutes,
        handleInternshipDemandRoutes,
        handleAllocationPlanningRoutes,
        handleReportsRoutes,
        handleAdminRoutes,
    ];

    for (const handler of handlers) {
        const result = handler(path, t);
        if (result) {
            return result;
        }
    }

    return [{ label: path }];
};

/**
 * Hook to generate breadcrumbs based on the current location path.
 */
export function useBreadcrumbs() {
    const location = useLocation();
    const { t } = useTranslation("common");

    const getBreadcrumbs = (): BreadcrumbItem[] => {
        return generateBreadcrumbs(location.pathname, t);
    };

    return getBreadcrumbs();
}