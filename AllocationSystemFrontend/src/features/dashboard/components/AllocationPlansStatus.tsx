import { FileText, Clock, CheckCircle, Archive } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/config/routes";
import { DashboardCard } from "./DashboardCard";
import { StatusItem } from "./StatusItem";
import { calculatePercentage } from "../utils/calculations";
import type { AllocationPlansByStatus } from "../hooks/useDashboard";

interface AllocationPlansStatusProps {
  plansByStatus: AllocationPlansByStatus;
  loading?: boolean;
}

const statusConfig = [
  {
    key: "draft" as const,
    icon: FileText,
    iconColor: "text-gray-600",
    badgeColor: "bg-gray-100 text-gray-800 border-gray-300",
    bgColor: "bg-gray-50 border-gray-200",
  },
  {
    key: "inReview" as const,
    icon: Clock,
    iconColor: "text-blue-600",
    badgeColor: "bg-blue-100 text-blue-800 border-blue-300",
    bgColor: "bg-blue-50 border-blue-200",
  },
  {
    key: "approved" as const,
    icon: CheckCircle,
    iconColor: "text-green-600",
    badgeColor: "bg-green-100 text-green-800 border-green-300",
    bgColor: "bg-green-50 border-green-200",
  },
  {
    key: "archived" as const,
    icon: Archive,
    iconColor: "text-purple-600",
    badgeColor: "bg-purple-100 text-purple-800 border-purple-300",
    bgColor: "bg-purple-50 border-purple-200",
  },
] as const;

export function AllocationPlansStatus({ plansByStatus, loading }: AllocationPlansStatusProps) {
  const { t } = useTranslation("common");
  const navigate = useNavigate();

  const handleViewPlans = () => {
    navigate(ROUTES.allocationPlanning.allocationPlans);
  };

  const total = plansByStatus.draft + plansByStatus.inReview + plansByStatus.approved + plansByStatus.archived;

  return (
    <DashboardCard
      title={t("dashboard.allocationPlansStatus.title")}
      description={t("dashboard.allocationPlansStatus.description")}
      loading={loading}
      viewAllLabel={t("dashboard.allocationPlansStatus.viewAll")}
      onViewAll={handleViewPlans}
      skeletonCount={4}
    >
      <div className="space-y-3">
        {statusConfig.map((config) => {
          const count = plansByStatus[config.key];
          const percentage = calculatePercentage(count, total);
          const Icon = config.icon;

          return (
            <StatusItem
              key={config.key}
              icon={Icon}
              iconColor={config.iconColor}
              label={t(`dashboard.allocationPlansStatus.${config.key}`)}
              value={count}
              percentage={percentage}
              bgColor={config.bgColor}
              badgeColor={config.badgeColor}
              showPercentage={total > 0}
              percentageLabel={`${percentage}% ${t("dashboard.allocationPlansStatus.ofTotal")}`}
            />
          );
        })}
      </div>
    </DashboardCard>
  );
}
