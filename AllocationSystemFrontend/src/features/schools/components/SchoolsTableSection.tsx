// components
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { SchoolStatusBadge } from "@/features/schools/components/SchoolStatusBadge";
// types
import type { School } from "@/features/schools/types/school.types";
import type { TFunction } from "i18next";

// icons
import { Eye, Loader2, Pencil, Power } from "lucide-react";


const TABLE_COLUMN_COUNT = 7;

interface SchoolsTableSectionProps {
  schools: School[];
  loading: boolean;
  pageSize: number;
  isAdmin: boolean;
  t: TFunction<"schools">;
  onViewSchool: (school: School) => void;
  onEditSchool: (school: School) => void;
  onToggleStatus: (school: School) => void;
}

export function SchoolsTableSection({
  schools,
  loading,
  pageSize,
  isAdmin,
  t,
  onViewSchool,
  onEditSchool,
  onToggleStatus,
}: SchoolsTableSectionProps) {
  return (
    <div className="border rounded-lg overflow-hidden">
      <div className="relative overflow-x-auto">
        {loading && schools.length > 0 && (
          <div className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-background/70 backdrop-blur-sm">
            <Loader2 className="h-6 w-6 animate-spin text-primary" aria-hidden="true" />
            <p className="text-sm text-muted-foreground">{t("table.loading")}</p>
          </div>
        )}
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="min-w-[220px]">{t("table.columns.name")}</TableHead>
              <TableHead className="min-w-[140px]">{t("table.columns.type")}</TableHead>
              <TableHead className="min-w-[100px]">{t("table.columns.zone")}</TableHead>
              <TableHead className="min-w-[180px]">{t("table.columns.contactEmail")}</TableHead>
              <TableHead className="min-w-[150px]">{t("table.columns.contactPhone")}</TableHead>
              <TableHead className="min-w-[120px]">{t("table.columns.status")}</TableHead>
              <TableHead className="min-w-[200px]">{t("table.columns.actions")}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading && schools.length === 0 ? (
              Array.from({ length: pageSize }).map((_, index) => (
                <TableRow key={`skeleton-${index}`}>
                  {Array.from({ length: TABLE_COLUMN_COUNT }).map((__, cellIndex) => (
                    <TableCell key={cellIndex}>
                      <Skeleton className="h-4 w-full max-w-[160px]" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : schools.length === 0 ? (
              <TableRow>
                <TableCell colSpan={TABLE_COLUMN_COUNT} className="h-24 text-center">
                  <p className="text-muted-foreground">{t("table.empty")}</p>
                </TableCell>
              </TableRow>
            ) : (
              schools.map((school) => (
                <TableRow key={school.id}>
                  <TableCell>
                    <div>
                      <p className="font-medium">{school.schoolName}</p>
                      {school.address && (
                        <p className="text-xs text-muted-foreground line-clamp-1">{school.address}</p>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="uppercase text-xs">
                      {t(`typeLabels.${school.schoolType}`)}
                    </Badge>
                  </TableCell>
                  <TableCell className="font-medium">{school.zoneNumber}</TableCell>
                  <TableCell>
                    {school.contactEmail ? (
                      <a
                        href={`mailto:${school.contactEmail}`}
                        className="text-primary underline-offset-2 hover:underline break-word"
                      >
                        {school.contactEmail}
                      </a>
                    ) : (
                      <span className="text-muted-foreground">—</span>
                    )}
                  </TableCell>
                  <TableCell>
                    {school.contactPhone ? (
                      <a
                        href={`tel:${school.contactPhone}`}
                        className="text-primary underline-offset-2 hover:underline"
                      >
                        {school.contactPhone}
                      </a>
                    ) : (
                      <span className="text-muted-foreground">—</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <SchoolStatusBadge isActive={school.isActive} />
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        className="gap-2"
                        onClick={() => onViewSchool(school)}
                      >
                        <Eye className="h-3.5 w-3.5" />
                        {t("actions.view")}
                      </Button>
                      {isAdmin && (
                        <>
                          <Button
                            variant="secondary"
                            size="sm"
                            className="gap-2"
                            onClick={() => onEditSchool(school)}
                          >
                            <Pencil className="h-3.5 w-3.5" />
                            {t("actions.edit")}
                          </Button>
                          <Button
                            variant={school.isActive ? "destructive" : "default"}
                            size="sm"
                            className="gap-2"
                            onClick={() => onToggleStatus(school)}
                          >
                            <Power className="h-3.5 w-3.5" />
                            {school.isActive ? t("actions.deactivate") : t("actions.activate")}
                          </Button>
                        </>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}

