import { Loader2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { BulkImportFileUpload } from "./BulkImportFileUpload";
import { ImportPreview } from "./ImportPreview";
import { ImportProgress } from "./ImportProgress";
import { ImportResults } from "./ImportResults";
import { useBulkImport } from "../hooks/useBulkImport";

interface BulkImportDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onImportComplete?: () => void;
}

export function BulkImportDialog({
  open,
  onOpenChange,
  onImportComplete,
}: BulkImportDialogProps) {
  const {
    step,
    parsedData,
    validationResult,
    importProgress,
    importResults,
    error,
    isLoading,
    handleFileSelect,
    handleImport,
    reset,
    startNewImport,
  } = useBulkImport();

  const handleClose = (open: boolean) => {
    // When open is false, it means the dialog should be closed
    if (!open) {
      reset();
      onOpenChange(false);
      if (onImportComplete && step === "results") {
        onImportComplete();
      }
    }
  };

  const handleCloseDialog = () => {
    reset();
    onOpenChange(false);
    if (onImportComplete && step === "results") {
      onImportComplete();
    }
  };

  const handleImportMore = () => {
    startNewImport();
  };

  const renderContent = () => {
    switch (step) {
      case "upload":
      case "parsing":
      case "validating":
        return (
          <BulkImportFileUpload
            onFileSelect={handleFileSelect}
            disabled={isLoading}
            error={error}
          />
        );

      case "preview":
        return validationResult ? (
          <ImportPreview
            data={parsedData}
            errors={validationResult.errors}
            onConfirm={handleImport}
            onCancel={handleCloseDialog}
            isLoading={isLoading}
          />
        ) : null;

      case "importing":
        return (
          <ImportProgress
            progress={importProgress.percentage}
            current={importProgress.current}
            total={importProgress.total}
            status="Importing teachers... Please wait"
          />
        );

      case "results":
        return importResults ? (
          <ImportResults
            results={importResults}
            onClose={handleCloseDialog}
            onImportMore={handleImportMore}
          />
        ) : null;

      default:
        return null;
    }
  };

  const getDialogTitle = () => {
    switch (step) {
      case "upload":
        return "Bulk Import Teachers";
      case "parsing":
        return "Parsing Excel File...";
      case "validating":
        return "Validating Data...";
      case "preview":
        return "Review Import Data";
      case "importing":
        return "Importing Teachers...";
      case "results":
        return "Import Complete";
      default:
        return "Bulk Import Teachers";
    }
  };

  const getDialogDescription = () => {
    switch (step) {
      case "upload":
        return "Upload an Excel file containing teacher data to import multiple teachers at once.";
      case "preview":
        return "Review the data below and fix any errors before importing.";
      case "importing":
        return "Please wait while we import the teachers.";
      case "results":
        return "Import process completed. Review the results below.";
      default:
        return "";
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            {isLoading && step !== "results" && <Loader2 className="h-5 w-5 animate-spin" />}
            {getDialogTitle()}
          </DialogTitle>
          {getDialogDescription() && (
            <DialogDescription>{getDialogDescription()}</DialogDescription>
          )}
        </DialogHeader>

        <div className="py-4">{renderContent()}</div>
      </DialogContent>
    </Dialog>
  );
}

