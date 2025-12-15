// react
import { useLocation, Link, Outlet } from "react-router-dom";

// components
import { AppSidebar } from "./AppSidebar"
import { SidebarSkeleton } from "./SidebarSkeleton"
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Separator } from "@/components/ui/separator";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";

// translations
import { useTranslation } from "react-i18next";

// config
import { ROUTES } from "@/config/routes";
import { ThemeToggle } from "../common/ThemeToggle";
import { LanguageSwitcher } from "../common/LanguageSwitcher";
import { useAuth } from "@/features/auth/hooks/useAuth";

export default function Sidebar() {
  const location = useLocation();
  const { t } = useTranslation("common");
  const { isLoading, user } = useAuth();
  const showSidebarSkeleton = isLoading && !user;

  // Generate breadcrumbs based on current route
  const getBreadcrumbs = () => {
    const path = location.pathname;
    const crumbs: { label: string; path?: string }[] = [];

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
        crumbs.push({
          label: t("navigation.allocationPlanningZoneConstraints"),
        });
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
      }  else if (path === ROUTES.allocationPlanning.planChangeLogs) {
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

    // Default: just show the path
    crumbs.push({ label: path });
    return crumbs;
  };

  const breadcrumbs = getBreadcrumbs();

  return (
    <SidebarProvider>
      {showSidebarSkeleton ? <SidebarSkeleton /> : <AppSidebar />}
      <SidebarInset>
        <header
          className="flex h-16 shrink-0 items-center gap-2 border-b border-(--brand-border) backdrop-blur dark:bg-neutral-900"
        >
          <div className="flex items-center gap-2 px-4 flex-1">
            <SidebarTrigger className="-ml-1" />
            <Separator
              orientation="vertical"
              className="mr-2 data-[orientation=vertical]:h-4"
            />
            <Breadcrumb>
              <BreadcrumbList>
                {breadcrumbs.map((crumb, index) => (
                  <div key={index} className="flex items-center">
                    {index > 0 && <BreadcrumbSeparator className="hidden md:block" />}
                    <BreadcrumbItem className={index === 0 ? "hidden md:block" : ""}>
                      {crumb.path && index < breadcrumbs.length - 1 ? (
                        <BreadcrumbLink asChild>
                          <Link
                            to={crumb.path}
                            className="text-muted-foreground transition-colors hover:text-(--brand-strong)"
                          >
                            {crumb.label}
                          </Link>
                        </BreadcrumbLink>
                      ) : (
                        <BreadcrumbPage className="text-foreground font-medium">
                          {crumb.label}
                        </BreadcrumbPage>
                      )}
                    </BreadcrumbItem>
                  </div>
                ))}
              </BreadcrumbList>
            </Breadcrumb>
          </div>
          <div className="flex items-center gap-2 px-4">
            <LanguageSwitcher />
            <ThemeToggle />
          </div>
        </header>
        <div className="flex flex-1 flex-col overflow-auto p-4 min-w-0">
          <Outlet />
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
}
