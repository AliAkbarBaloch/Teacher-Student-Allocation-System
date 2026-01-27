import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "@/config/routes";

export interface BreadcrumbItem {
    label: string;
    path?: string;
}

/**
 * Hook to generate breadcrumbs based on the current location path.
 */
export function useBreadcrumbs() {
    const location = useLocation();
    const { t } = useTranslation("common");

    const getBreadcrumbs = (): BreadcrumbItem[] => {
        const path = location.pathname;
        const crumbs: BreadcrumbItem[] = [];

        // Home/Dashboard
        if (path === ROUTES.main.dashboard || path === ROUTES.main.home) {
            crumbs.push({ label: t("navigation.dashboard") });
            return crumbs;
        }

        // Settings
        if (path === ROUTES.main.settings) {
            crumbs.push({ label: t("userMenu.settings") });
            return crumbs;
        }

        // Base Data routes
        if (path.startsWith("/base-data")) {
            crumbs.push({ label: t("navigation.baseData"), path: ROUTES.baseData.academicYears });
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
        }

        // Teacher Management routes
        if (path.startsWith("/teacher-management")) {
            crumbs.push({ label: t("navigation.teacherManagement"), path: ROUTES.teacherManagement.teacherSubjects });
            if (path === ROUTES.teacherManagement.teacherSubjects) {
                crumbs.push({ label: t("navigation.teacherManagementTeacherSubjects") });
            } else if (path === ROUTES.teacherManagement.teacherAvailability) {
                crumbs.push({ label: t("navigation.teacherManagementTeacherAvailability") });
            } else if (path === ROUTES.teacherManagement.teacherSubmissions) {
                crumbs.push({ label: t("navigation.teacherManagementTeacherSubmissions") });
            }
            return crumbs;
        }

        // Internship Demand routes
        if (path === ROUTES.internshipDemand.demandPerYear) {
            crumbs.push({ label: t("navigation.internshipDemand") });
            return crumbs;
        }

        // Allocation Planning routes
        if (path.startsWith("/allocation-planning")) {
            crumbs.push({ label: t("navigation.allocationPlanning"), path: ROUTES.allocationPlanning.allocationPlans });
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
        }

        // Reports routes
        if (path.startsWith("/reports")) {
            crumbs.push({ label: t("navigation.reports"), path: ROUTES.reports.allocationReports });
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
        }

        // Admin routes
        if (path.startsWith("/admin")) {
            crumbs.push({ label: t("navigation.groups.admin") });
            if (path === ROUTES.admin.users) {
                crumbs.push({ label: t("navigation.users") });
            } else if (path === ROUTES.admin.roles) {
                crumbs.push({ label: t("navigation.roles") });
            } else {
                crumbs.push({ label: path });
            }
            return crumbs;
        }

        crumbs.push({ label: path });
        return crumbs;
    };

    return getBreadcrumbs();
}
