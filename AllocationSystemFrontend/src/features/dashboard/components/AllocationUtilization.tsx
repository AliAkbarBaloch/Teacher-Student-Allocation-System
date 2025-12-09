import { AlertTriangle, TrendingDown, CheckCircle2 } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/config/routes";
import { DashboardCard } from "./DashboardCard";
import { StatusItem } from "./StatusItem";
import { calculatePercentage } from "../utils/calculations";
import type { AllocationUtilization } from "../hooks/useDashboard";

interface AllocationUtilizationProps {
  utilization: AllocationUtilization;
  loading?: boolean;
}

const utilizationConfig = [
  {
    key: "overUtilized" as const,
    icon: AlertTriangle,
    iconColor: "text-red-600",
    bgColor: "bg-red-50 border-red-200",
    badgeColor: "bg-red-600 text-white",
    translationKey: "overUtilized",
  },
  {
    key: "balanced" as const,
    icon: CheckCircle2,
    iconColor: "text-green-600",
    bgColor: "bg-green-50 border-green-200",
    badgeColor: "bg-green-600 text-white",
    translationKey: "balanced",
  },
  {
    key: "underUtilized" as const,
    icon: TrendingDown,
    iconColor: "text-yellow-600",
    bgColor: "bg-yellow-50 border-yellow-200",
    badgeColor: "border-yellow-600 text-yellow-900",
    translationKey: "underUtilized",
  },
] as const;

export function AllocationUtilization({ utilization, loading }: AllocationUtilizationProps) {
  const { t } = useTranslation("common");
  const navigate = useNavigate();

  const handleViewDetails = () => {
    navigate(ROUTES.allocationPlanning.creditHourTracking);
  };

  const total = utilization.overUtilized + utilization.underUtilized + utilization.balanced;

  return (
    <DashboardCard
      title={t("dashboard.allocationUtilization.title")}
      description={t("dashboard.allocationUtilization.description")}
      loading={loading}
      viewAllLabel={t("dashboard.allocationUtilization.viewDetails")}
      onViewAll={handleViewDetails}
      skeletonCount={3}
    >
      <div className="space-y-4">
        {utilizationConfig.map((config) => {
          const value = utilization[config.key];
          const percentage = calculatePercentage(value, total);
          const Icon = config.icon;

          return (
            <StatusItem
              key={config.key}
              icon={Icon}
              iconColor={config.iconColor}
              label={t(`dashboard.allocationUtilization.${config.translationKey}`)}
              value={value}
              percentage={percentage}
              bgColor={config.bgColor}
              badgeColor={config.badgeColor}
              showPercentage={true}
              percentageLabel={`${value} ${t("dashboard.allocationUtilization.teachers")} (${percentage}%)`}
            />
          );
        })}
      </div>
    </DashboardCard>
  );
}
