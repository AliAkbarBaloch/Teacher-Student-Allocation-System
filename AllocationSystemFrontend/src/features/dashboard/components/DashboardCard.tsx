import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import type { ReactNode } from "react";

interface DashboardCardProps {
  title: string;
  description: string;
  loading?: boolean;
  viewAllLabel?: string;
  onViewAll?: () => void;
  children: ReactNode;
  skeletonCount?: number;
}

/**
 * Reusable dashboard card component with consistent structure
 */
export function DashboardCard({
  title,
  description,
  loading,
  viewAllLabel,
  onViewAll,
  children,
  skeletonCount = 3,
}: DashboardCardProps) {

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[...Array(skeletonCount)].map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>{title}</CardTitle>
            <CardDescription>{description}</CardDescription>
          </div>
          {viewAllLabel && onViewAll && (
            <button
              onClick={onViewAll}
              className="text-sm text-primary hover:underline"
            >
              {viewAllLabel}
            </button>
          )}
        </div>
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  );
}
