// components
import { Badge } from "@/components/ui/badge";

// translations
import { useTranslation } from "react-i18next";

interface SchoolStatusBadgeProps {
  isActive: boolean;
}

export function SchoolStatusBadge({ isActive }: SchoolStatusBadgeProps) {
  const { t } = useTranslation("schools");

  if (isActive) {
    return (
      <Badge variant="success">
        {t("status.active")}
      </Badge>
    );
  }

  return (
    <Badge variant="destructive" className="bg-destructive/15 text-destructive border-transparent">
      {t("status.inactive")}
    </Badge>
  );
}

