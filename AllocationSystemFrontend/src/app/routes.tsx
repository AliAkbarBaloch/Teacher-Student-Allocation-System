import { Suspense } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import App from "./App";
import AuthLayout from "@/components/layout/AuthLayout";
import { ROUTES } from "../config/routes";

// Pages
import HomePage from "@/pages/home/HomePage";
import LoginPage from "@/pages/auth/LoginPage";
import ForgotPasswordPage from "@/pages/auth/ForgotPasswordPage";
import ResetPasswordPage from "@/pages/auth/ResetPasswordPage";
import SettingsPage from "@/pages/settings/SettingsPage";
import RolesPage from "@/pages/roles/RolesPage";

const withSuspense = (node: React.ReactNode) => (
  <Suspense fallback={
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2"></div>
        <p className="text-muted-foreground">Loading...</p>
      </div>
    </div>
  }>
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
        path: ROUTES.main.settings,
        element: withSuspense(<SettingsPage />),
      },
      // User & Access Management
      {
        path: ROUTES.userAccess.users,
        element: withSuspense(<div>Users</div>),
      },
      {
        path: ROUTES.userAccess.roles,
        element: withSuspense(<RolesPage />),
      },
      {
        path: ROUTES.userAccess.permissions,
        element: withSuspense(<div>Permissions</div>),
      },
      {
        path: ROUTES.userAccess.auditLogs,
        element: withSuspense(<div>Audit Logs</div>),
      },
      // Base Data Management
      {
        path: ROUTES.baseData.academicYears,
        element: withSuspense(<div>Academic Years</div>),
      },
      {
        path: ROUTES.baseData.subjectCategories,
        element: withSuspense(<div>Subject Categories</div>),
      },
      {
        path: ROUTES.baseData.subjects,
        element: withSuspense(<div>Subjects</div>),
      },
      {
        path: ROUTES.baseData.schools,
        element: withSuspense(<div>Schools</div>),
      },
      {
        path: ROUTES.baseData.teachers,
        element: withSuspense(<div>Teachers</div>),
      },
      {
        path: ROUTES.baseData.internshipTypes,
        element: withSuspense(<div>Internship Types</div>),
      },
      // Teacher Management
      {
        path: ROUTES.teacherManagement.teacherSubjects,
        element: withSuspense(<div>Teacher Subjects</div>),
      },
      {
        path: ROUTES.teacherManagement.teacherAvailability,
        element: withSuspense(<div>Teacher Availability</div>),
      },
      {
        path: ROUTES.teacherManagement.teacherFormSubmissions,
        element: withSuspense(<div>Teacher Form Submissions</div>),
      },
      // Internship Demand
      {
        path: ROUTES.internshipDemand.demandPerYear,
        element: withSuspense(<div>Internship Demand per Year</div>),
      },
      // Allocation Planning
      {
        path: ROUTES.allocationPlanning.allocationPlans,
        element: withSuspense(<div>Allocation Plans</div>),
      },
      {
        path: ROUTES.allocationPlanning.teacherAssignments,
        element: withSuspense(<div>Teacher Assignments</div>),
      },
      {
        path: ROUTES.allocationPlanning.creditHourTracking,
        element: withSuspense(<div>Credit Hour Tracking</div>),
      },
      {
        path: ROUTES.allocationPlanning.zoneConstraints,
        element: withSuspense(<div>Zone Constraints</div>),
      },
      {
        path: ROUTES.allocationPlanning.planChangeLogs,
        element: withSuspense(<div>Plan Change Logs</div>),
      },
      // Reports
      {
        path: ROUTES.reports.allocationReports,
        element: withSuspense(<div>Allocation Reports</div>),
      },
      {
        path: ROUTES.reports.teacherReports,
        element: withSuspense(<div>Teacher Reports</div>),
      },
      {
        path: ROUTES.reports.schoolReports,
        element: withSuspense(<div>School Reports</div>),
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
  {
    path: "*",
    element: <Navigate to={ROUTES.auth.login} replace />,
  },
]);

