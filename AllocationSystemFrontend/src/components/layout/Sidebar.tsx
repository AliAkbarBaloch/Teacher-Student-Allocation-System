// react
import { Outlet } from "react-router-dom";

// components
import { AppSidebar } from "./AppSidebar"
import { SidebarSkeleton } from "./SidebarSkeleton"
import { SidebarHeader } from "./SidebarHeader";
import {
  SidebarInset,
  SidebarProvider,
} from "@/components/ui/sidebar";

// auth
import { useAuth } from "@/features/auth/hooks/useAuth";

// hooks
import { useBreadcrumbs } from "@/hooks/useBreadcrumbs";

export default function Sidebar() {
  const { isLoading, user } = useAuth();
  const breadcrumbs = useBreadcrumbs();
  const showSidebarSkeleton = isLoading && !user;

  return (
    <SidebarProvider>
      {showSidebarSkeleton ? <SidebarSkeleton /> : <AppSidebar />}
      <SidebarInset>
        <SidebarHeader breadcrumbs={breadcrumbs} />
        <div className="flex flex-1 flex-col overflow-auto p-4 min-w-0">
          <Outlet />
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
}

