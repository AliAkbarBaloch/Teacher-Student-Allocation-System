import { useCallback, useState } from "react";
import type {
    AcademicYear,
    CreateAcademicYearRequest,
    UpdateAcademicYearRequest,
} from "@/features/academic-years/types/academicYear.types";

/* ===================== Types ===================== */

type DialogApi = {
    isOpen: boolean;
    setIsOpen: (open: boolean) => void;
};

type DialogState = {
    create: DialogApi;
    edit: DialogApi;
    view: DialogApi;
    delete: DialogApi;
};

type AcademicYearUpsert =
    | CreateAcademicYearRequest
    | UpdateAcademicYearRequest;

export type AcademicYearPageActions = {
    handleCreate: (data: AcademicYearUpsert) => Promise<void>;
    handleUpdate: (data: AcademicYearUpsert) => Promise<void>;
    handleDelete: () => Promise<void>;
    handleEditClick: (ay: AcademicYear) => void;
    handleViewClick: (ay: AcademicYear) => void;
    handleDeleteClick: (ay: AcademicYear) => void;
};

type Params = {
    dialogs: DialogState;
    selectedAcademicYear: AcademicYear | null;
    setSelectedAcademicYear: (ay: AcademicYear | null) => void;
    handleCreateInternal: (data: AcademicYearUpsert) => Promise<void>;
    handleUpdateInternal: (data: AcademicYearUpsert, id: number) => Promise<void>;
    handleDeleteInternal: (id: number) => Promise<void>;
};

/* ===================== Small action hooks ===================== */

function useCreateAction(
    dialogs: DialogState,
    handleCreateInternal: (data: AcademicYearUpsert) => Promise<void>
) {
    return useCallback(
        async (data: AcademicYearUpsert) => {
            try {
                await handleCreateInternal(data);
                dialogs.create.setIsOpen(false);
            } catch {
                /* handled in service */
            }
        },
        [handleCreateInternal, dialogs.create]
    );
}

function useUpdateAction(
    dialogs: DialogState,
    selectedAcademicYear: AcademicYear | null,
    setSelectedAcademicYear: (ay: AcademicYear | null) => void,
    handleUpdateInternal: (data: AcademicYearUpsert, id: number) => Promise<void>
) {
    return useCallback(
        async (data: AcademicYearUpsert) => {
            if (!selectedAcademicYear) {
                return;
            }

            try {
                await handleUpdateInternal(data, selectedAcademicYear.id);
                dialogs.edit.setIsOpen(false);
                setSelectedAcademicYear(null);
            } catch {
                /* handled in service */
            }
        },
        [
            handleUpdateInternal,
            selectedAcademicYear,
            dialogs.edit,
            setSelectedAcademicYear,
        ]
    );
}

function useDeleteAction(
    dialogs: DialogState,
    academicYearToDelete: AcademicYear | null,
    setAcademicYearToDelete: (ay: AcademicYear | null) => void,
    handleDeleteInternal: (id: number) => Promise<void>
) {
    return useCallback(
        async () => {
            if (!academicYearToDelete) {
                return;
            }

            try {
                await handleDeleteInternal(academicYearToDelete.id);
                dialogs.delete.setIsOpen(false);
                setAcademicYearToDelete(null);
            } catch {
                /* handled in service */
            }
        },
        [
            academicYearToDelete,
            handleDeleteInternal,
            dialogs.delete,
            setAcademicYearToDelete,
        ]
    );
}

function useEditClick(
    dialogs: DialogState,
    setSelectedAcademicYear: (ay: AcademicYear | null) => void
) {
    return useCallback(
        (ay: AcademicYear) => {
            setSelectedAcademicYear(ay);
            dialogs.edit.setIsOpen(true);
        },
        [setSelectedAcademicYear, dialogs.edit]
    );
}

function useViewClick(
    dialogs: DialogState,
    setSelectedAcademicYear: (ay: AcademicYear | null) => void
) {
    return useCallback(
        (ay: AcademicYear) => {
            setSelectedAcademicYear(ay);
            dialogs.view.setIsOpen(true);
        },
        [setSelectedAcademicYear, dialogs.view]
    );
}

function useDeleteClick(
    dialogs: DialogState,
    setAcademicYearToDelete: (ay: AcademicYear | null) => void
) {
    return useCallback(
        (ay: AcademicYear) => {
            setAcademicYearToDelete(ay);
            dialogs.delete.setIsOpen(true);
        },
        [dialogs.delete, setAcademicYearToDelete]
    );
}

/* ===================== Exported hook (UNDER 30 LINES) ===================== */

export function useAcademicYearPageActions(
    params: Params
): AcademicYearPageActions {
    const {
        dialogs,
        selectedAcademicYear,
        setSelectedAcademicYear,
        handleCreateInternal,
        handleUpdateInternal,
        handleDeleteInternal,
    } = params;

    const [academicYearToDelete, setAcademicYearToDelete] =
        useState<AcademicYear | null>(null);

    return {
        handleCreate: useCreateAction(dialogs, handleCreateInternal),
        handleUpdate: useUpdateAction(
            dialogs,
            selectedAcademicYear,
            setSelectedAcademicYear,
            handleUpdateInternal
        ),
        handleDelete: useDeleteAction(
            dialogs,
            academicYearToDelete,
            setAcademicYearToDelete,
            handleDeleteInternal
        ),
        handleEditClick: useEditClick(dialogs, setSelectedAcademicYear),
        handleViewClick: useViewClick(dialogs, setSelectedAcademicYear),
        handleDeleteClick: useDeleteClick(dialogs, setAcademicYearToDelete),
    };
}
