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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { SchoolStatusBadge } from "@/features/schools/components/SchoolStatusBadge";
// types
import type { School } from "@/features/schools/types/school.types";
import type { TFunction } from "i18next";

// icons
import { Eye, Loader2, MoreHorizontal, Pencil, Power, Trash2, School2 } from "lucide-react";


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
  onDeleteSchool: (school: School) => void;
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
  onDeleteSchool,
}: SchoolsTableSectionProps) {
  // Ensure schools is always an array
  const safeSchools = schools || [];
  
  return (
    <div className="border rounded-lg overflow-hidden w-full">
      <div className="relative overflow-x-auto w-full">
        {loading && safeSchools.length > 0 && (
          <div className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-background/70 backdrop-blur-sm">
            <Loader2 className="h-6 w-6 animate-spin text-primary" aria-hidden="true" />
            <p className="text-sm text-muted-foreground">{t("table.loading")}</p>
          </div>
        )}
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[200px] max-w-[200px]">{t("table.columns.name")}</TableHead>
              <TableHead className="w-[120px]">{t("table.columns.type")}</TableHead>
              <TableHead className="w-[80px]">{t("table.columns.zone")}</TableHead>
              <TableHead className="w-[180px] max-w-[180px]">{t("table.columns.contactEmail")}</TableHead>
              <TableHead className="w-[140px]">{t("table.columns.contactPhone")}</TableHead>
              <TableHead className="w-[100px]">{t("table.columns.status")}</TableHead>
              <TableHead className="w-[80px]">{t("table.columns.actions")}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading && safeSchools.length === 0 ? (
              Array.from({ length: pageSize }).map((_, index) => (
                <TableRow key={`skeleton-${index}`}>
                  {Array.from({ length: TABLE_COLUMN_COUNT }).map((__, cellIndex) => (
                    <TableCell key={cellIndex}>
                      <Skeleton className="h-4 w-full max-w-[160px]" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : safeSchools.length === 0 ? (
              <TableRow>
                <TableCell colSpan={TABLE_COLUMN_COUNT} className="h-24 text-center">
                  <div className="flex flex-col items-center justify-center gap-3 py-8">
                    <div className="rounded-full bg-muted p-4">
                      <School2 className="h-8 w-8 text-muted-foreground" />
                    </div>
                    <div className="space-y-1">
                      <p className="text-sm font-medium text-foreground">{t("table.empty")}</p>
                      <p className="text-xs text-muted-foreground">
                        {t("table.emptyDescription", { defaultValue: "No schools found matching your filters." })}
                      </p>
                    </div>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              safeSchools.map((school) => (
                <TableRow key={school.id}>
                  <TableCell className="w-[200px] max-w-[200px]">
                    <div className="min-w-0">
                      <p className="font-medium truncate" title={school.schoolName}>
                        {school.schoolName}
                      </p>
                      {school.address && (
                        <p className="text-xs text-muted-foreground truncate" title={school.address}>
                          {school.address}
                        </p>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="uppercase text-xs">
                      {t(`typeLabels.${school.schoolType}`)}
                    </Badge>
                  </TableCell>
                  <TableCell className="font-medium">{school.zoneNumber}</TableCell>
                  <TableCell className="w-[180px] max-w-[180px]">
                    {school.contactEmail ? (
                      <a
                        href={`mailto:${school.contactEmail}`}
                        className="text-primary underline-offset-2 hover:underline truncate block"
                        title={school.contactEmail}
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
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="h-8 w-8 p-0">
                          <span className="sr-only">{t("actions.openMenu")}</span>
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => onViewSchool(school)}>
                          <Eye className="mr-2 h-4 w-4" />
                          {t("actions.view")}
                        </DropdownMenuItem>
                        {isAdmin && (
                          <>
                            <DropdownMenuItem onClick={() => onEditSchool(school)}>
                              <Pencil className="mr-2 h-4 w-4" />
                              {t("actions.edit")}
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => onToggleStatus(school)}>
                              <Power className="mr-2 h-4 w-4" />
                              {school.isActive ? t("actions.deactivate") : t("actions.activate")}
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => onDeleteSchool(school)}
                              className="text-destructive focus:text-destructive"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              {t("actions.delete")}
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
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

