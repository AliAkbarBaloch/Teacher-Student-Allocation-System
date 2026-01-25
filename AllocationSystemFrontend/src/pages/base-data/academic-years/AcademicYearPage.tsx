import { AcademicYearPageContent } from "./AcademicYearPageContent";
import { useAcademicYearPageVm } from "./useAcademicYearPageVm";

export default function AcademicYearPage() {
  const { t, dialogs, page, columnConfig, actions } = useAcademicYearPageVm();

  return (
    <AcademicYearPageContent
      t={t}
      dialogs={dialogs}
      columnConfig={columnConfig}
      academicYears={page.academicYears}
      loading={page.loading}
      error={page.error}
      selectedAcademicYear={page.selectedAcademicYear}
      isSubmitting={page.isSubmitting}
      actions={actions}
    />
  );
}

