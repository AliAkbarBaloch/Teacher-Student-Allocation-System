import { useTranslation } from "react-i18next";
import { useDialogState } from "@/hooks/useDialogState";
import { useAcademicYearsColumnConfig, useAcademicYearsPage } from "@/features/academic-years";
import { useAcademicYearPageActions } from "./useAcademicYearPageActions";

export function useAcademicYearPageVm() {
    const { t } = useTranslation("academicYears");
    const dialogs = useDialogState();

    const page = useAcademicYearsPage();
    const columnConfig = useAcademicYearsColumnConfig();

    const actions = useAcademicYearPageActions({
        dialogs,
        selectedAcademicYear: page.selectedAcademicYear,
        setSelectedAcademicYear: page.setSelectedAcademicYear,
        handleCreateInternal: page.handleCreate,
        handleUpdateInternal: page.handleUpdate,
        handleDeleteInternal: page.handleDelete,
    });

    return { t, dialogs, page, columnConfig, actions };
}
