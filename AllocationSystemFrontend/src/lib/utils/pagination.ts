export function getVisiblePages(currentPage: number, totalPages: number, range: number = 2): number[] {
  const safeTotalPages = Math.max(totalPages, 1);
  const safeCurrent = Math.min(Math.max(currentPage, 1), safeTotalPages);
  const start = Math.max(1, safeCurrent - range);
  const end = Math.min(safeTotalPages, safeCurrent + range);
  const pages: number[] = [];

  for (let page = start; page <= end; page += 1) {
    pages.push(page);
  }

  return pages;
}

export function getPaginationSummary(page: number, pageSize: number, totalItems: number): {
  from: number;
  to: number;
} {
  if (totalItems === 0) {
    return { from: 0, to: 0 };
  }

  const from = (Math.max(page, 1) - 1) * pageSize + 1;
  const to = Math.min(page * pageSize, totalItems);
  return { from, to };
}

export function clampPage(page: number, totalPages: number): number {
  const safeTotalPages = Math.max(totalPages, 1);
  return Math.min(Math.max(page, 1), safeTotalPages);
}

