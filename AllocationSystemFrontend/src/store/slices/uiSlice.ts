import type { Theme } from '../../types/theme.types';

export type UISlice = {
  theme: Theme;
  setTheme: (theme: Theme) => void;
};

type SetState<T> = (
  partial: Partial<T> | ((state: T) => Partial<T>),
  replace?: false | undefined
) => void;

export const createUISlice = <T extends UISlice>(set: SetState<T>): UISlice => ({
  theme: 'system',
  setTheme: theme => set({ theme } as Partial<T>),
});