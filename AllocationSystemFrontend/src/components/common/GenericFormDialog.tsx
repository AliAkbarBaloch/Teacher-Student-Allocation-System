import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from "@/lib/utils";
import type { GenericFormDialogProps } from "./types/form.types";

const maxWidthClasses = {
  sm: "max-w-sm",
  md: "max-w-md",
  lg: "max-w-lg",
  xl: "max-w-xl",
  "2xl": "max-w-2xl",
  "3xl": "max-w-3xl",
  "4xl": "max-w-4xl",
  "5xl": "max-w-5xl",
  full: "max-w-full",
};

/**
 * GenericFormDialog - A reusable dialog wrapper for Create/Edit forms
 * 
 * This component provides a consistent dialog structure (header, body, footer)
 * for form dialogs. The actual form content is passed as children.
 * 
 * @example
 * ```tsx
 * <GenericFormDialog
 *   open={isOpen}
 *   onOpenChange={setIsOpen}
 *   title="Create Academic Year"
 *   description="Add a new academic year"
 * >
 *   <GenericForm fields={fieldConfig} onSubmit={handleSubmit} />
 * </GenericFormDialog>
 * ```
 */
export function GenericFormDialog({
  open,
  onOpenChange,
  title,
  description,
  maxWidth = "2xl",
  children,
  className,
}: GenericFormDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className={cn(maxWidthClasses[maxWidth], className)}>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          {description && <DialogDescription>{description}</DialogDescription>}
        </DialogHeader>
        <DialogBody>{children}</DialogBody>
      </DialogContent>
    </Dialog>
  );
}
