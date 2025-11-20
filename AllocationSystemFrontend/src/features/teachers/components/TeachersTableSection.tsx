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
// types
import type { Teacher } from "@/features/teachers/types/teacher.types";
import type { TFunction } from "i18next";

// icons
import { Eye, Loader2, MoreHorizontal, Pencil, Power, Trash2 } from "lucide-react";

const TABLE_COLUMN_COUNT = 6;

interface TeachersTableSectionProps {
  teachers: Teacher[];
  loading: boolean;
  pageSize: number;
  isAdmin: boolean;
  t: TFunction<"teachers">;
  onViewTeacher: (teacher: Teacher) => void;
  onEditTeacher: (teacher: Teacher) => void;
  onToggleStatus: (teacher: Teacher) => void;
  onDeleteTeacher: (teacher: Teacher) => void;
}

export function TeachersTableSection({
  teachers = [],
  loading,
  pageSize,
  isAdmin,
  t,
  onViewTeacher,
  onEditTeacher,
  onToggleStatus,
  onDeleteTeacher,
}: TeachersTableSectionProps) {
  return (
    <div className="border rounded-lg overflow-hidden">
      <div className="relative overflow-x-auto">
        {loading && teachers.length > 0 && (
          <div className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-background/70 backdrop-blur-sm">
            <Loader2 className="h-6 w-6 animate-spin text-primary" aria-hidden="true" />
            <p className="text-sm text-muted-foreground">{t("table.loading")}</p>
          </div>
        )}
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="min-w-[200px]">{t("table.columns.name")}</TableHead>
              <TableHead className="min-w-[200px]">{t("table.columns.email")}</TableHead>
              <TableHead className="w-[200px] max-w-[200px]">{t("table.columns.schoolName")}</TableHead>
              <TableHead className="min-w-[140px]">{t("table.columns.employmentStatus")}</TableHead>
              <TableHead className="min-w-[100px]">{t("table.columns.isPartTime")}</TableHead>
              <TableHead className="min-w-[120px]">{t("table.columns.status")}</TableHead>
              <TableHead className="min-w-[200px]">{t("table.columns.actions")}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading && teachers.length === 0 ? (
              Array.from({ length: pageSize }).map((_, index) => (
                <TableRow key={`skeleton-${index}`}>
                  {Array.from({ length: TABLE_COLUMN_COUNT + 1 }).map((__, cellIndex) => (
                    <TableCell key={cellIndex}>
                      <Skeleton className="h-4 w-full max-w-[160px]" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : teachers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={TABLE_COLUMN_COUNT + 1} className="h-24 text-center">
                  <p className="text-muted-foreground">{t("table.empty")}</p>
                </TableCell>
              </TableRow>
            ) : (
              teachers.map((teacher) => (
                <TableRow key={teacher.id}>
                  <TableCell>
                    <div>
                      <p className="font-medium">{`${teacher.firstName} ${teacher.lastName}`}</p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <a
                      href={`mailto:${teacher.email}`}
                      className="text-primary underline-offset-2 hover:underline break-word"
                    >
                      {teacher.email}
                    </a>
                  </TableCell>
                  <TableCell className="w-[200px] max-w-[200px]">
                    <div className="min-w-0">
                      <p className="truncate" title={teacher.schoolName || "-"}>
                        {teacher.schoolName || "-"}
                      </p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="uppercase text-xs">
                      {t(`form.employmentStatus.${teacher.employmentStatus}`, {
                        defaultValue: teacher.employmentStatus,
                      })}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {teacher.isPartTime ? (
                      <Badge variant="secondary" className="text-xs">
                        {t("table.yes")}
                      </Badge>
                    ) : (
                      <Badge variant="outline" className="text-xs">
                        {t("table.no")}
                      </Badge>
                    )}
                  </TableCell>
                  <TableCell>
                    {teacher.isActive ? (
                      <Badge variant="default" className="bg-green-500 hover:bg-green-600">
                        {t("status.active")}
                      </Badge>
                    ) : (
                      <Badge variant="secondary">{t("status.inactive")}</Badge>
                    )}
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
                        <DropdownMenuItem onClick={() => onViewTeacher(teacher)}>
                          <Eye className="mr-2 h-4 w-4" />
                          {t("actions.view")}
                        </DropdownMenuItem>
                        {isAdmin && (
                          <>
                            <DropdownMenuItem onClick={() => onEditTeacher(teacher)}>
                              <Pencil className="mr-2 h-4 w-4" />
                              {t("actions.edit")}
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => onToggleStatus(teacher)}>
                              <Power className="mr-2 h-4 w-4" />
                              {teacher.isActive ? t("actions.deactivate") : t("actions.activate")}
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => onDeleteTeacher(teacher)}
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

