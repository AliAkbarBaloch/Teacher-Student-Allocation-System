import { Button } from "@/components/ui/button";
import React from "react";

/**
 * Cancel button component Props
 */
interface CancelButtonProps {
  onClick: () => void;
  disabled?: boolean;
  children: React.ReactNode;
}

export const CancelButton: React.FC<CancelButtonProps> = ({
  onClick,
  disabled,
  children,
}) => (
  <Button
    type="button"
    variant="outline"
    onClick={onClick}
    disabled={disabled}
  >
    {children}
  </Button>
);