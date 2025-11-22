import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Copy, ExternalLink, CheckCircle2 } from "lucide-react";
import { toast } from "sonner";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";
import type { TeacherFormSubmission } from "../types/teacherFormSubmission.types";

export function useTeacherFormSubmissionColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("teacherFormSubmissions");

  return [
    {
      field: "teacher",
      title: t("table.teacher"),
      enableSorting: false,
      fieldType: "text",
      format: (_value: unknown, row: unknown): ReactNode => {
        const submission = row as TeacherFormSubmission;
        const name = submission.teacherFirstName && submission.teacherLastName
          ? `${submission.teacherFirstName} ${submission.teacherLastName}`
          : submission.teacherEmail || "-";
        return <span className="font-medium">{name}</span>;
      },
    },
    {
      field: "yearName",
      title: t("table.academicYear"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "submittedAt",
      title: t("table.submittedAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) => {
        if (!value) return "-";
        try {
          const date = new Date(value as string);
          return date.toLocaleString();
        } catch {
          return String(value);
        }
      },
    },
    {
      field: "isProcessed",
      title: t("table.processed"),
      enableSorting: true,
      format: (value: unknown): ReactNode => {
        const isProcessed = Boolean(value);
        return isProcessed ? (
          <Badge variant="success">{t("table.yes")}</Badge>
        ) : (
          <Badge variant="secondary">{t("table.no")}</Badge>
        );
      },
    },
    {
      field: "invited",
      title: t("table.invited"),
      enableSorting: false,
      format: (_value: unknown, row: unknown): ReactNode => {
        const submission = row as TeacherFormSubmission;
        
        // If form has been submitted (submittedAt exists), show "accepted"
        if (submission.submittedAt) {
          return <Badge variant="success">{t("table.invitedStatus.accepted")}</Badge>;
        }
        
        // If formToken exists but no submittedAt yet, show "invited" (link generated but not submitted)
        if (submission.formToken) {
          return <Badge variant="default">{t("table.invitedStatus.invited")}</Badge>;
        }
        
        // Otherwise, show "-" (no link generated)
        return <span className="text-muted-foreground">{t("table.invitedStatus.notInvited")}</span>;
      },
    },
    {
      field: "formToken",
      title: t("table.formLink"),
      enableSorting: false,
      fieldType: "text",
      format: (_value: unknown, row: unknown): ReactNode => {
        const submission = row as TeacherFormSubmission;
        if (!submission.formToken) return "-";
        
        const formUrl = `${window.location.origin}/form/${submission.formToken}`;
        
        const handleCopy = (e: React.MouseEvent) => {
          e.stopPropagation();
          navigator.clipboard.writeText(formUrl);
          toast.success(t("table.linkCopied"));
        };
        
        const handleOpen = (e: React.MouseEvent) => {
          e.stopPropagation();
          window.open(formUrl, "_blank");
        };
        
        return (
          <div className="flex items-center gap-2">
            <Badge variant="success" className="flex items-center gap-1">
              <CheckCircle2 className="h-3 w-3" />
              {t("table.linkGenerated")}
            </Badge>
            <div className="flex items-center gap-1">
              <Button
                variant="ghost"
                size="sm"
                className="h-6 w-6 p-0"
                onClick={handleCopy}
                title={t("table.copyLink")}
              >
                <Copy className="h-3 w-3" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="h-6 w-6 p-0"
                onClick={handleOpen}
                title={t("table.openLink")}
              >
                <ExternalLink className="h-3 w-3" />
              </Button>
            </div>
          </div>
        );
      },
    },
  ];
}

