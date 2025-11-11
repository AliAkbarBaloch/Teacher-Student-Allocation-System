import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { createUISlice, type UISlice } from './slices/uiSlice';

type RootStore = UISlice;

export const useAppStore = create<RootStore>()(
  devtools(
    persist(
      (set) => ({
        ...createUISlice<RootStore>(set),
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


