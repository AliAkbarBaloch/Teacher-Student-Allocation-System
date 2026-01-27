import { SchoolService } from '@/features/schools/services/schoolService';
import type { School } from '@/features/schools/types/school.types';
import { SCHOOLS_DROPDOWN_PAGE_SIZE } from '@/lib/constants/app';

export type SchoolsSlice = {
  schools: School[];
  schoolsLoading: boolean;
  schoolsError: string | null;
  loadSchools: () => Promise<void>;
  refreshSchools: () => Promise<void>;
};

type SetState<T> = (
  partial: Partial<T> | ((state: T) => Partial<T>),
  replace?: false | undefined
) => void;

/**
 * Creates the parameters for fetching schools
 */
const createFetchSchoolsParams = () => ({
  isActive: true,
  page: 1,
  pageSize: SCHOOLS_DROPDOWN_PAGE_SIZE,
  sortBy: 'schoolName',
  sortOrder: 'asc',
});

/**
 * Sets loading state for schools
 */
const setLoadingState = <T extends SchoolsSlice>(set: SetState<T>) => {
  set({ schoolsLoading: true, schoolsError: null } as Partial<T>);
};

/**
 * Sets success state with schools data
 */
const setSuccessState = <T extends SchoolsSlice>(set: SetState<T>, schools: School[]) => {
  set({ schools, schoolsLoading: false } as Partial<T>);
};

/**
 * Sets error state with error message
 */
const setErrorState = <T extends SchoolsSlice>(set: SetState<T>, err: unknown) => {
  const message = err instanceof Error ? err.message : 'Failed to load schools';
  set({ schoolsError: message, schools: [], schoolsLoading: false } as unknown as Partial<T>);
};

/**
 * Common function to fetch schools data
 */
const fetchSchoolsData = async <T extends SchoolsSlice>(set: SetState<T>) => {
  setLoadingState(set);
  try {
    const response = await SchoolService.getPaginated(createFetchSchoolsParams());
    setSuccessState(set, response.items || []);
  } catch (err) {
    setErrorState(set, err);
  }
};

export const createSchoolsSlice = <T extends SchoolsSlice>(set: SetState<T>): SchoolsSlice => ({
  schools: [],
  schoolsLoading: false,
  schoolsError: null,
  loadSchools: async () => {
    await fetchSchoolsData(set);
  },
  refreshSchools: async () => {
    await fetchSchoolsData(set);
  },
});