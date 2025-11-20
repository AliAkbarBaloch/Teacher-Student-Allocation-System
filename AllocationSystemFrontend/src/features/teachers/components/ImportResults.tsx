import { useMemo } from "react";
import { CheckCircle2, XCircle, Download, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { BulkImportResponse } from "../types/teacher.types";

interface ImportResultsProps {
  results: BulkImportResponse;
  onClose: () => void;
  onImportMore: () => void;
}

export function ImportResults({ results, onClose, onImportMore }: ImportResultsProps) {
  const failedResults = useMemo(
    () => results.results.filter((r) => !r.success),
    [results.results]
  );

  const downloadErrorReport = () => {
    if (failedResults.length === 0) return;

    const csvContent = [
      ["Row Number", "Error"],
      ...failedResults.map((r) => [String(r.rowNumber), r.error || "Unknown error"]),
    ]
      .map((row) => row.map((cell) => `"${cell}"`).join(","))
      .join("\n");

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", `import-errors-${new Date().toISOString().split("T")[0]}.csv`);
    link.style.visibility = "hidden";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 border rounded-lg bg-muted/30">
          <div className="flex items-center gap-2 mb-2">
            <AlertCircle className="h-5 w-5 text-muted-foreground" />
            <span className="text-sm font-medium text-muted-foreground">Total</span>
          </div>
          <p className="text-2xl font-bold">{results.totalRows}</p>
        </div>
        <div className="p-4 border rounded-lg bg-green-50 dark:bg-green-950/20">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle2 className="h-5 w-5 text-green-600" />
            <span className="text-sm font-medium text-green-700 dark:text-green-400">
              Successful
            </span>
          </div>
          <p className="text-2xl font-bold text-green-700 dark:text-green-400">
            {results.successfulRows}
          </p>
        </div>
        <div className="p-4 border rounded-lg bg-destructive/10">
          <div className="flex items-center gap-2 mb-2">
            <XCircle className="h-5 w-5 text-destructive" />
            <span className="text-sm font-medium text-destructive">Failed</span>
          </div>
          <p className="text-2xl font-bold text-destructive">{results.failedRows}</p>
        </div>
      </div>

      {/* Failed Rows Table */}
      {failedResults.length > 0 && (
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold">Failed Imports</h3>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={downloadErrorReport}
              className="gap-2"
            >
              <Download className="h-4 w-4" />
              Download Error Report
            </Button>
          </div>
          <div className="border rounded-lg overflow-hidden max-h-[300px] overflow-y-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-20">Row</TableHead>
                  <TableHead>Error</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {failedResults.map((result) => (
                  <TableRow key={result.rowNumber}>
                    <TableCell className="font-mono text-xs">{result.rowNumber}</TableCell>
                    <TableCell className="text-sm text-destructive">
                      {result.error || "Unknown error"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>
      )}

      {/* Success Message */}
      {results.successfulRows > 0 && failedResults.length === 0 && (
        <div className="flex items-center gap-2 p-4 bg-green-50 dark:bg-green-950/20 border border-green-200 dark:border-green-800 rounded-lg">
          <CheckCircle2 className="h-5 w-5 text-green-600 shrink-0" />
          <p className="text-sm text-green-700 dark:text-green-400">
            All {results.successfulRows} teachers were imported successfully!
          </p>
        </div>
      )}

      {/* Partial Success */}
      {results.successfulRows > 0 && failedResults.length > 0 && (
        <div className="flex items-center gap-2 p-4 bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800 rounded-lg">
          <AlertCircle className="h-5 w-5 text-amber-600 shrink-0" />
          <p className="text-sm text-amber-700 dark:text-amber-400">
            {results.successfulRows} teachers imported successfully, but {failedResults.length}{" "}
            failed. Please review the errors above.
          </p>
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center justify-end gap-2 pt-4 border-t">
        <Button type="button" variant="outline" onClick={onImportMore}>
          Import More
        </Button>
        <Button type="button" onClick={onClose}>
          Close
        </Button>
      </div>
    </div>
  );
}

