import React from "react"
// icons
import {
  LayoutDashboard,
  UserCog,
  ClipboardList,
  TrendingUp,
  BarChart3,
  Database,
  LucideLayoutDashboard,
  Users
} from "lucide-react"
// translations
import { useTranslation } from "react-i18next"
// router
import { useLocation, Link } from "react-router-dom"
// config
import { ROUTES } from "@/config/routes"

// components
import { NavMain, type NavGroup } from "@/components/layout/NavMain"
import { NavUser } from "@/components/layout/NavUser"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
// hooks
import { useAuth } from "@/features/auth/hooks/useAuth"
// common
import { Logo } from "../common/Logo"

function AppSidebarHeader() {
  const { t } = useTranslation("common")

  return (
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton size="lg" asChild>
            <Link to={ROUTES.main.dashboard}>
              <div className="flex items-center gap-4 lg:gap-8">
                <Logo size="sm" showText={false} className="shrink-0" linkTo={null} />
              </div>
              <div className="grid flex-1 text-left text-sm leading-tight">
                <span className="truncate font-medium">{t("app.name")}</span>
              </div>
            </Link>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>
  )
}

function useSidebarNav() {
  const { t } = useTranslation("common")
  const location = useLocation()

  const navGroups: NavGroup[] = [
    {
      label: t("navigation.groups.overview"),
      items: [
        {
          title: t("navigation.dashboard"),
          url: ROUTES.main.dashboard,
          icon: LayoutDashboard,
          isActive: location.pathname === ROUTES.main.dashboard,
        },
        {
          title: t("navigation.allocationReport"),
          url: ROUTES.main.allocationReport,
          icon: LucideLayoutDashboard,
          isActive: location.pathname === ROUTES.main.allocationReport,
        },
      ],
    },
    {
      label: t("navigation.groups.dataManagement"),
      items: [
        {
          title: t("navigation.baseData"),
          url: ROUTES.baseData.academicYears,
          icon: Database,
          isActive: location.pathname.startsWith("/base-data"),
          items: [
            {
              title: t("navigation.baseDataAcademicYears"),
              url: ROUTES.baseData.academicYears,
            },
            {
              title: t("navigation.baseDataSubjectCategories"),
              url: ROUTES.baseData.subjectCategories,
            },
            {
              title: t("navigation.baseDataSubjects"),
              url: ROUTES.baseData.subjects,
            },
            {
              title: t("navigation.baseDataSchools"),
              url: ROUTES.baseData.schools,
            },
            {
              title: t("navigation.baseDataTeachers"),
              url: ROUTES.baseData.teachers,
            },
            {
              title: t("navigation.baseDataInternshipTypes"),
              url: ROUTES.baseData.internshipTypes,
            },
            {
              title: t("navigation.baseDataZoneConstraints"),
              url: ROUTES.baseData.zoneConstraints,
            },
          ],
        },
        {
          title: t("navigation.teacherManagement"),
          url: ROUTES.teacherManagement.teacherSubjects,
          icon: UserCog,
          isActive: location.pathname.startsWith("/teacher-management"),
          items: [
            {
              title: t("navigation.teacherManagementTeacherSubjects"),
              url: ROUTES.teacherManagement.teacherSubjects,
            },
            {
              title: t("navigation.teacherManagementTeacherAvailability"),
              url: ROUTES.teacherManagement.teacherAvailability,
            },
            {
              title: t("navigation.teacherManagementTeacherSubmissions"),
              url: ROUTES.teacherManagement.teacherSubmissions,
            },
          ],
        },
      ],
    },
    {
      label: t("navigation.groups.planning"),
      items: [
        {
          title: t("navigation.internshipDemand"),
          url: ROUTES.internshipDemand.demandPerYear,
          icon: TrendingUp,
          isActive: location.pathname === ROUTES.internshipDemand.demandPerYear,
        },
        {
          title: t("navigation.allocationPlanning"),
          url: ROUTES.allocationPlanning.allocationPlans,
          icon: ClipboardList,
          isActive: location.pathname.startsWith("/allocation-planning"),
          items: [
            {
              title: t("navigation.allocationPlanningAllocationPlans"),
              url: ROUTES.allocationPlanning.allocationPlans,
            },
            {
              title: t("navigation.allocationPlanningTeacherAssignments"),
              url: ROUTES.allocationPlanning.teacherAssignments,
            },
            {
              title: t("navigation.allocationPlanningCreditHourTracking"),
              url: ROUTES.allocationPlanning.creditHourTracking,
            },
            {
              title: t("navigation.allocationPlanningPlanChangeLogs"),
              url: ROUTES.allocationPlanning.planChangeLogs,
            },
          ],
        },
      ],
    },
    {
      label: t("navigation.groups.reports"),
      items: [
        {
          title: t("navigation.reports"),
          url: ROUTES.reports.allocationReports,
          icon: BarChart3,
          isActive: location.pathname.startsWith("/reports"),
          items: [
            {
              title: t("navigation.reportsAllocationReports"),
              url: ROUTES.reports.allocationReports,
            },
            {
              title: t("navigation.reportsPlanAnalyticReport"),
              url: ROUTES.reports.planAnalyticReport,
            },
            {
              title: t("navigation.reportsTeacherReports"),
              url: ROUTES.reports.teacherReports,
            },
            {
              title: t("navigation.reportsSchoolReports"),
              url: ROUTES.reports.schoolReports,
            },
            {
              title: t("navigation.reportsAuditReports"),
              url: ROUTES.reports.auditReports,
            },
          ],
        },
      ],
    },
    {
      label: t("navigation.groups.admin"),
      items: [
        {
          title: t("navigation.users"),
          url: ROUTES.admin.users,
          icon: Users,
          isActive: location.pathname.startsWith("/admin/users"),
        },
        {
          title: t("navigation.roles"),
          url: ROUTES.admin.roles,
          icon: UserCog,
          isActive: location.pathname === ROUTES.admin.roles,
        },
      ],
    },
  ]

  return { navGroups }
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { user } = useAuth()
  const { navGroups } = useSidebarNav()

  let userData = {
    name: "User",
    email: "",
    avatar: "",
  }

  if (user) {
    userData = {
      name: user.name || user.email?.split("@")[0] || "User",
      email: user.email || "",
      avatar: "",
    }
  }

  return (
    <Sidebar variant="inset" {...props}>
      <AppSidebarHeader />
      <SidebarContent>
        <NavMain groups={navGroups} />
      </SidebarContent>
      <SidebarFooter>
        <NavUser user={userData} />
      </SidebarFooter>
    </Sidebar>
  );
}

