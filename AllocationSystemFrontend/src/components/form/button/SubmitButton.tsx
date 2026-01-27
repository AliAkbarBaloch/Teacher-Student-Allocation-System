import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import React from "react";

/**
 * Submit button component Props
 */
interface SubmitButtonProps {
  isLoading?: boolean;
  isEdit?: boolean;
  createText: React.ReactNode;
  updateText: React.ReactNode;
  savingText: React.ReactNode;
  disabled?: boolean;
}

export const SubmitButton: React.FC<SubmitButtonProps> = ({
  isLoading,
  isEdit,
  createText,
  updateText,
  savingText,
  disabled,
}) => (
  <Button type="submit" disabled={disabled}>
    {isLoading ? (
      <>
        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
        {savingText}
      </>
    ) : isEdit ? updateText : createText}
  </Button>
);