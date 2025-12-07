import { CreditHourTrackingDialogs } from "./CreditHourTrackingDialogs";
import type { UseCreditHourTrackingDialogsReturn } from "../hooks/useCreditHourTrackingDialogs";

interface CreditHourTrackingDialogsContainerProps {
  dialogs: UseCreditHourTrackingDialogsReturn;
  isSubmitting: boolean;
}

/**
 * Container component that simplifies prop passing to CreditHourTrackingDialogs
 * Encapsulates the mapping between hook return values and component props
 */
export function CreditHourTrackingDialogsContainer({
  dialogs,
  isSubmitting,
}: CreditHourTrackingDialogsContainerProps) {
  return (
    <CreditHourTrackingDialogs
      dialogs={dialogs.dialogs}
      selectedEntry={dialogs.selectedEntry}
      onUpdateSubmit={dialogs.handleUpdateSubmit}
      onDelete={dialogs.handleDelete}
      onEditClick={dialogs.handleEditClick}
      onSelectedChange={dialogs.setSelectedEntry}
      isSubmitting={isSubmitting}
    />
  );
}
