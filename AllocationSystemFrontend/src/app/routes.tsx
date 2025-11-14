import { Suspense } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import App from "./App";
import { ROUTES } from "../config/routes";

// Pages
import HomePage from "@/pages/home/HomePage";

const withSuspense = (node: React.ReactNode) => (
  <Suspense fallback={<div>Loading...</div>}>{node}</Suspense>
);

export const router = createBrowserRouter([
  {
    path: ROUTES.main.home, 
    element: <App />,
    children: [
      { index: true, element: withSuspense(<HomePage />) },
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
    element: <Navigate to={ROUTES.main.home} replace />,
  },
]);
