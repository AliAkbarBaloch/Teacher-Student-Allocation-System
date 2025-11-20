import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { createUISlice, type UISlice } from './slices/uiSlice';
import { createSchoolsSlice, type SchoolsSlice } from './slices/schoolsSlice';

type RootStore = UISlice & SchoolsSlice;

export const useAppStore = create<RootStore>()(
  devtools(
    persist(
      (set) => ({
        ...createUISlice<RootStore>(set),
        ...createSchoolsSlice<RootStore>(set),
      }),
      {
        name: 'app-store',
        partialize: (state) => ({
          theme: state.theme,
        }),
      }
    ),
    { name: 'AppStore' }
  )
);

// Initialize schools on store creation
useAppStore.getState().loadSchools();


