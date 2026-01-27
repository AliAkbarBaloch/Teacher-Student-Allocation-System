import type { TFunction } from "i18next";
import type { ColumnConfig } from "@/types/datatable.types";
import type {
    AcademicYear,
    CreateAcademicYearRequest,
    UpdateAcademicYearRequest,
} from "@/features/academic-years/types/academicYear.types";

import { AcademicYearDialogs } from "@/features/academic-years";
import { AcademicYearPageHeader } from "./AcademicYearPageHeader";
import { AcademicYearPageTable } from "./AcademicYearPageTable";

type AcademicYearUpsert = CreateAcademicYearRequest | UpdateAcademicYearRequest;

type DialogApi = {
    isOpen: boolean;
    setIsOpen: (v: boolean) => void;
};

type DialogState = {
    create: DialogApi;
    edit: DialogApi;
    view: DialogApi;
    delete: DialogApi;
};

export type AcademicYearPageActions = {
    handleCreate: (data: AcademicYearUpsert) => Promise<void>;
    handleUpdate: (data: AcademicYearUpsert) => Promise<void>;
    handleDelete: () => Promise<void>;
    handleViewClick: (ay: AcademicYear) => void;
    handleEditClick: (ay: AcademicYear) => void;
    handleDeleteClick: (ay: AcademicYear) => void;
};

type AcademicYearPageContentProps = {
    t: TFunction<"academicYears">;
    dialogs: DialogState;
    columnConfig: ColumnConfig[];
    academicYears: AcademicYear[];
    loading: boolean;
    error: string | null;
    selectedAcademicYear: AcademicYear | null;
    isSubmitting: boolean;
    actions: AcademicYearPageActions;
};

function renderHeader(t: TFunction<"academicYears">, dialogs: DialogState) {
    return (
        <AcademicYearPageHeader
            title={t("title")}
            subtitle={t("subtitle")}
            createLabel={t("actions.create")}
            onCreateClick={() => dialogs.create.setIsOpen(true)}
        />
    );
}

function renderTable(props: {
    t: TFunction<"academicYears">;
    columnConfig: ColumnConfig[];
    academicYears: AcademicYear[];
    loading: boolean;
    error: string | null;
    actions: AcademicYearPageActions;
}) {
    const { t, columnConfig, academicYears, loading, error, actions } = props;

    return (
        <AcademicYearPageTable
            columnConfig={columnConfig}
            data={academicYears}
            loading={loading}
            error={error}
            searchPlaceholder={t("table.searchPlaceholder")}
            emptyMessage={t("table.emptyMessage")}
            labels={{
                view: t("actions.view"),
                edit: t("actions.edit"),
                delete: t("actions.delete"),
            }}
            onView={actions.handleViewClick}
            onEdit={actions.handleEditClick}
            onDelete={actions.handleDeleteClick}
        />
    );
}

function renderDialogs(props: {
    t: TFunction<"academicYears">;
    dialogs: DialogState;
    selectedAcademicYear: AcademicYear | null;
    isSubmitting: boolean;
    actions: AcademicYearPageActions;
}) {
    const { t, dialogs, selectedAcademicYear, isSubmitting, actions } = props;

    return (
        <AcademicYearDialogs
            isCreateDialogOpen={dialogs.create.isOpen}
            setIsCreateDialogOpen={dialogs.create.setIsOpen}
            isEditDialogOpen={dialogs.edit.isOpen}
            setIsEditDialogOpen={dialogs.edit.setIsOpen}
            isViewDialogOpen={dialogs.view.isOpen}
            setIsViewDialogOpen={dialogs.view.setIsOpen}
            isDeleteDialogOpen={dialogs.delete.isOpen}
            setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
            selectedAcademicYear={selectedAcademicYear}
            onCreateSubmit={actions.handleCreate}
            onUpdateSubmit={actions.handleUpdate}
            onDelete={actions.handleDelete}
            onEditClick={actions.handleEditClick}
            isSubmitting={isSubmitting}
            t={t}
        />
    );
}

export function AcademicYearPageContent(props: AcademicYearPageContentProps) {
    const {
        t,
        dialogs,
        columnConfig,
        academicYears,
        loading,
        error,
        selectedAcademicYear,
        isSubmitting,
        actions,
    } = props;

    return (
        <div className="space-y-6">
            {renderHeader(t, dialogs)}
            {renderTable({ t, columnConfig, academicYears, loading, error, actions })}
            {renderDialogs({ t, dialogs, selectedAcademicYear, isSubmitting, actions })}
        </div>
    );
}
