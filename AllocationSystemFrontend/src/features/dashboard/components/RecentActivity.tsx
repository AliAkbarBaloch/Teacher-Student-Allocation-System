import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { AuditLog } from "../../audit-logs/types/auditLog.types";
import { AuditAction } from "../../audit-logs/types/auditLog.types";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/config/routes";

const formatTimeAgo = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);
  
  if (diffInSeconds < 60) return "just now";
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
  if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)} days ago`;
  
  return date.toLocaleDateString();
};

interface RecentActivityProps {
  auditLogs: AuditLog[];
  loading?: boolean;
}

const getActionColor = (action: AuditAction): string => {
  if (action === AuditAction.CREATE) return "bg-green-100 text-green-800";
  if (action === AuditAction.UPDATE) return "bg-blue-100 text-blue-800";
  if (action === AuditAction.DELETE) return "bg-red-100 text-red-800";
  if (action === AuditAction.LOGIN || action === AuditAction.LOGOUT) return "bg-purple-100 text-purple-800";
  return "bg-gray-100 text-gray-800";
};

export function RecentActivity({ auditLogs, loading }: RecentActivityProps) {
  const { t } = useTranslation("common");
  const navigate = useNavigate();

  const handleViewAll = () => {
    navigate(ROUTES.reports.auditReports);
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{t("dashboard.recentActivity.title")}</CardTitle>
          <CardDescription>{t("dashboard.recentActivity.description")}</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[...Array(5)].map((_, i) => (
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

  if (auditLogs.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{t("dashboard.recentActivity.title")}</CardTitle>
          <CardDescription>{t("dashboard.recentActivity.description")}</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground text-center py-4">
            {t("dashboard.recentActivity.noActivity")}
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>{t("dashboard.recentActivity.title")}</CardTitle>
            <CardDescription>{t("dashboard.recentActivity.description")}</CardDescription>
          </div>
          <button
            onClick={handleViewAll}
            className="text-sm text-primary hover:underline"
          >
            {t("dashboard.recentActivity.viewAll")}
          </button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {auditLogs.map((log) => (
            <div key={log.id} className="flex items-start justify-between gap-4 pb-4 border-b last:border-0">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <Badge className={getActionColor(log.action)} variant="secondary">
                    {log.action}
                  </Badge>
                  <span className="text-sm font-medium truncate">{log.targetEntity}</span>
                </div>
                {log.description && (
                  <p className="text-sm text-muted-foreground truncate">{log.description}</p>
                )}
                <div className="flex items-center gap-2 mt-1 text-xs text-muted-foreground">
                  <span>{log.userIdentifier}</span>
                  <span>•</span>
                  <span>
                    {formatTimeAgo(log.eventTimestamp)}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
