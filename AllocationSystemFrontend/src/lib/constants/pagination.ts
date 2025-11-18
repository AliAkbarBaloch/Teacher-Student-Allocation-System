export const TABLE_PAGE_SIZE_OPTIONS = [10, 20, 50, 100] as const;

export type TablePageSizeOption = (typeof TABLE_PAGE_SIZE_OPTIONS)[number];

export const DEFAULT_TABLE_PAGE_SIZE: TablePageSizeOption = 20;

export const TABLE_PAGINATION_PRESET = {
  pageSizeOptions: TABLE_PAGE_SIZE_OPTIONS,
  defaultPageSize: DEFAULT_TABLE_PAGE_SIZE,
} as const;

