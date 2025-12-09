import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { type ComponentType } from "react";

interface StatCardProps {
  title: string;
  value: string | number;
  description?: string;
  icon: ComponentType<{ className?: string }>;
  iconColor?: string;
  trend?: {
    value: number;
    label: string;
    isPositive: boolean;
  };
  onClick?: () => void;
  className?: string;
}

export function StatCard({
  title,
  value,
  description,
  icon: Icon,
  iconColor = "text-blue-600",
  trend,
  onClick,
  className,
}: StatCardProps) {
  return (
    <Card
      className={cn(
        "transition-all hover:shadow-md",
        onClick && "cursor-pointer hover:border-primary",
        className
      )}
      onClick={onClick}
    >
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <Icon className={cn("h-4 w-4", iconColor)} />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value.toLocaleString()}</div>
        {description && (
          <p className="text-xs text-muted-foreground mt-1">{description}</p>
        )}
        {trend && (
          <div className="flex items-center mt-2 text-xs">
            <span
              className={cn(
                "font-medium",
                trend.isPositive ? "text-green-600" : "text-red-600"
              )}
            >
              {trend.isPositive ? "+" : ""}
              {trend.value}%
            </span>
            <span className="text-muted-foreground ml-1">{trend.label}</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
