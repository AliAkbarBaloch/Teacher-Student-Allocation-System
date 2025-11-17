import { Skeleton } from "@/components/ui/skeleton";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";

export function SidebarSkeleton() {
  return (
    <Sidebar
      variant="inset"
      className="pointer-events-none select-none border-transparent"
      data-testid="sidebar-skeleton"
    >
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg">
              <Skeleton className="h-8 w-8 rounded-lg" />
              <div className="flex-1 space-y-1.5">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-3 w-20" />
              </div>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>

      <SidebarContent>
        <div className="space-y-6 px-3 py-4">
          {Array.from({ length: 4 }).map((_, groupIndex) => (
            <div key={groupIndex} className="space-y-3">
              <Skeleton className="h-3 w-28" />
              <div className="space-y-2">
                {Array.from({ length: 3 }).map((_, itemIndex) => (
                  <Skeleton
                    key={itemIndex}
                    className="h-9 w-full rounded-md"
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      </SidebarContent>

      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg">
              <Skeleton className="h-8 w-8 rounded-lg" />
              <div className="flex-1 space-y-1.5">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-3 w-40" />
              </div>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  );
}

