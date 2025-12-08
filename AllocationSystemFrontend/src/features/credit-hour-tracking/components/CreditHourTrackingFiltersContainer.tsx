import { CreditHourTrackingFilters } from "./CreditHourTrackingFilters";
import type { UseCreditHourTrackingFiltersExtendedReturn } from "../hooks/useCreditHourTrackingFiltersExtended";

interface CreditHourTrackingFiltersContainerProps {
  filters: UseCreditHourTrackingFiltersExtendedReturn;
  disabled?: boolean;
}

/**
 * Container component that simplifies prop passing to CreditHourTrackingFilters
 * Encapsulates the mapping between hook return values and component props
 */
export function CreditHourTrackingFiltersContainer({
  filters,
  disabled = false,
}: CreditHourTrackingFiltersContainerProps) {
  return (
    <CreditHourTrackingFilters
      yearId={filters.effectiveYearId}
      onYearIdChange={filters.handleYearChange}
      teacherId={filters.filters.teacherId}
      onTeacherIdChange={filters.handleTeacherChange}
      teacherSearch={filters.teacherSearch}
      onTeacherSearchChange={filters.handleTeacherSearchChange}
      minBalance={filters.filters.minBalance}
      onMinBalanceChange={(value) => filters.handleFilterChange({ minBalance: value })}
      onReset={filters.handleResetFilters}
      disabled={disabled}
    />
  );
}
