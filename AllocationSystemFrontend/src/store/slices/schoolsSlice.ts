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

export const createSchoolsSlice = <T extends SchoolsSlice>(set: SetState<T>): SchoolsSlice => ({
  schools: [],
  schoolsLoading: false,
  schoolsError: null,
  loadSchools: async () => {
    set({ schoolsLoading: true, schoolsError: null } as Partial<T>);
    try {
      const response = await SchoolService.getPaginated({
        isActive: true,
        page: 1,
        pageSize: SCHOOLS_DROPDOWN_PAGE_SIZE,
        sortBy: 'schoolName',
        sortOrder: 'asc',
      });
      set({ schools: response.items || [], schoolsLoading: false } as Partial<T>);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load schools';
      set({ schoolsError: message, schools: [], schoolsLoading: false } as unknown as Partial<T>);
    }
  },
  refreshSchools: async () => {
    set({ schoolsLoading: true, schoolsError: null } as Partial<T>);
    try {
      const response = await SchoolService.getPaginated({
        isActive: true,
        page: 1,
        pageSize: SCHOOLS_DROPDOWN_PAGE_SIZE,
        sortBy: 'schoolName',
        sortOrder: 'asc',
      });
      set({ schools: response.items || [], schoolsLoading: false } as Partial<T>);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load schools';
      set({ schoolsError: message, schools: [], schoolsLoading: false } as unknown as Partial<T>);
    }
  },
});

