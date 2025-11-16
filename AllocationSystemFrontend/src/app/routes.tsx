import { Suspense } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import App from "./App";
import AuthLayout from "@/components/layout/AuthLayout";
import { ROUTES } from "../config/routes";

// Pages
import HomePage from "@/pages/home/HomePage";
import LoginPage from "@/pages/auth/LoginPage";
import ForgotPasswordPage from "@/pages/auth/ForgotPasswordPage";
import ChangePasswordPage from "@/pages/auth/ChangePasswordPage";
import SettingsPage from "@/pages/settings/SettingsPage";

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
        element: withSuspense(<ChangePasswordPage />),
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
        element: withSuspense(<HomePage />),
      },
      {
        path: ROUTES.main.schools,
        element: withSuspense(<div>Schools</div>),
      },
      {
        path: ROUTES.main.teachers_pools,
        element: withSuspense(<div>Teachers Pools</div>),
      },
      {
        path: ROUTES.main.teacher_management,
        element: withSuspense(<div>Teacher Management</div>),
      },
      {
        path: ROUTES.main.settings,
        element: withSuspense(<SettingsPage />),
      },
      {
        path: ROUTES.configuration.budget,
        element: withSuspense(<div>Budget</div>),
      },
      {
        path: ROUTES.configuration.subjects,
        element: withSuspense(<div>Subjects</div>),
      },
      {
        path: ROUTES.configuration.internships,
        element: withSuspense(<div>Internships</div>),
      },
      {
        path: ROUTES.operations.allocation,
        element: withSuspense(<div>Allocation</div>),
      },
      {
        path: ROUTES.operations.reports,
        element: withSuspense(<div>Reports</div>),
      },
      {
        path: ROUTES.operations.archives,
        element: withSuspense(<div>Archives</div>),
      },
    ],
  },
  {
    path: "*",
    element: <Navigate to={ROUTES.auth.login} replace />,
  },
]);
