import AuthLayout from "@/components/layout/AuthLayout";
import { Suspense } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import { ROUTES } from "../config/routes";
import App from "./App";

// Pages
import CreditHourTrackingPage from "@/pages/allocation-planning/CreditHourTrackingPage";
import ForgotPasswordPage from "@/pages/auth/ForgotPasswordPage";
import LoginPage from "@/pages/auth/LoginPage";
import ResetPasswordPage from "@/pages/auth/ResetPasswordPage";
import AcademicYearPage from "@/pages/base-data/AcademicYearPage";
import AllocationPlanPage from "@/pages/base-data/AllocationPlanPage";
import InternshipTypesPage from "@/pages/base-data/InternshipTypesPage";
import PlanChangeLogsPage from "@/pages/base-data/PlanChangeLogsPage";
import SchoolsPage from "@/pages/base-data/SchoolsPage";
import SubjectCategoriesPage from "@/pages/base-data/SubjectCategoriesPage";
import SubjectsPage from "@/pages/base-data/SubjectsPage";
import TeacherAssignmentPage from "@/pages/base-data/TeacherAssignmentPage";
import TeacherAvailabilityPage from "@/pages/base-data/TeacherAvailabilityPage";
import TeacherDetailPage from "@/pages/base-data/TeacherDetailPage";
import TeachersPage from "@/pages/base-data/TeachersPage";
import TeacherSubjectsPage from "@/pages/base-data/TeacherSubjectsPage";
import ZoneConstraintPage from "@/pages/base-data/ZoneConstraintPage";
import HomePage from "@/pages/home/HomePage";
import TeacherFormPage from "@/pages/public/TeacherFormPage";
import AllocationReportPage from "@/pages/reports/AllocationReportPage";
import RolesPage from "@/pages/roles/RolesPage";
import SettingsPage from "@/pages/settings/SettingsPage";
import TeacherSubmissionsPage from "@/pages/teacher-management/TeacherSubmissionsPage";

import InternshipDemandPerYearPage from "@/pages/internship-demand/InternshipDemandPerYearPage";
import TeacherReportPage from "@/pages/reports/TeacherReportPage";
import SchoolReportPage from "@/pages/reports/SchoolReportPage";
import PlanAnalyticsReportPage from "@/pages/reports/PlanAnalyticsReportPage";

const withSuspense = (node: React.ReactNode) => (
  <Suspense
    fallback={
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2"></div>
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </div>
    }
  >
    {node}
  </Suspense>
);

export const router = createBrowserRouter([
  {
    path: ROUTES.auth.login,
    element: <AuthLayout />,
    children: [
      {
        index: true,
        element: withSuspense(<LoginPage />),
      },
    ],
  },
  {
    path: ROUTES.auth.forgotPassword,
    element: <AuthLayout />,
    children: [
      {
        index: true,
        element: withSuspense(<ForgotPasswordPage />),
      },
    ],
  },
  {
    path: ROUTES.auth.resetPassword,
    element: <AuthLayout />,
    children: [
      {
        index: true,
        element: withSuspense(<ResetPasswordPage />),
      },
    ],
  },
  {
    path: "/form/:token",
    element: withSuspense(<TeacherFormPage />),
  },
  {
    element: <App />,
    children: [
      {
        path: "/",
        element: <Navigate to={ROUTES.auth.login} replace />,
      },
      {
        path: ROUTES.main.home,
        element: <Navigate to={ROUTES.main.dashboard} replace />,
      },
      {
        path: ROUTES.main.dashboard,
        element: withSuspense(<HomePage />),
      },
      {
        path: ROUTES.main.allocationReport,
        element: withSuspense(<AllocationReportPage />),
      },
      {
        path: `${ROUTES.main.allocationReport}/:planId`,
        element: withSuspense(<AllocationReportPage />),
      },
      {
        path: ROUTES.main.settings,
        element: withSuspense(<SettingsPage />),
      },
      // Base Data Management
      {
        path: ROUTES.baseData.academicYears,
        element: withSuspense(<AcademicYearPage />),
      },
      {
        path: ROUTES.baseData.subjectCategories,
        element: withSuspense(<SubjectCategoriesPage />),
      },
      {
        path: ROUTES.baseData.subjects,
        element: withSuspense(<SubjectsPage />),
      },
      {
        path: ROUTES.baseData.schools,
        element: withSuspense(<SchoolsPage />),
      },
      {
        path: ROUTES.baseData.teachers,
        element: withSuspense(<TeachersPage />),
      },
      {
        path: `${ROUTES.baseData.teachers}/:id`,
        element: withSuspense(<TeacherDetailPage />), // or <TeacherDetailPage />
      },
      {
        path: ROUTES.baseData.internshipTypes,
        element: withSuspense(<InternshipTypesPage />),
      },
      {
        path: ROUTES.baseData.zoneConstraints,
        element: withSuspense(<ZoneConstraintPage />),
      },
      // Teacher Management
      {
        path: ROUTES.teacherManagement.teacherSubjects,
        element: withSuspense(<TeacherSubjectsPage />),
      },
      {
        path: ROUTES.teacherManagement.teacherAvailability,
        element: withSuspense(<TeacherAvailabilityPage />),
      },
      {
        path: ROUTES.teacherManagement.teacherSubmissions,
        element: withSuspense(<TeacherSubmissionsPage />),
      },
      // Internship Demand
      {
        path: ROUTES.internshipDemand.demandPerYear,
        element: withSuspense(<InternshipDemandPerYearPage />),
      },
      // Allocation Planning
      {
        path: ROUTES.allocationPlanning.allocationPlans,
        element: withSuspense(<AllocationPlanPage />),
      },
      {
        path: ROUTES.allocationPlanning.teacherAssignments,
        element: withSuspense(<TeacherAssignmentPage />),
      },
      {
        path: ROUTES.allocationPlanning.creditHourTracking,
        element: withSuspense(<CreditHourTrackingPage />),
      },
      {
        path: `${ROUTES.allocationPlanning.creditHourTracking}/:yearId`,
        element: withSuspense(<CreditHourTrackingPage />),
      },
      {
        path: ROUTES.allocationPlanning.planChangeLogs,
        element: withSuspense(<PlanChangeLogsPage />),
      },
      // Reports
      {
        path: ROUTES.reports.allocationReports,
        element: withSuspense(<div>Allocation Reports</div>),
      },
      {
        path: ROUTES.reports.planAnalyticReport,
        element: withSuspense(<PlanAnalyticsReportPage />),
      },
      {
        path: ROUTES.reports.teacherReports,
        element: withSuspense(<TeacherReportPage />),
      },
      {
        path: ROUTES.reports.schoolReports,
        element: withSuspense(<SchoolReportPage />),
      },
      {
        path: ROUTES.reports.auditReports,
        element: withSuspense(<div>Audit Reports</div>),
      },
      // Legacy admin routes
      {
        path: ROUTES.admin.roles,
        element: withSuspense(<RolesPage />),
      },
    ],
  },
  // {
  //   path: "*",
  //   element: <Navigate to={ROUTES.auth.login} replace />,
  // },
]);
