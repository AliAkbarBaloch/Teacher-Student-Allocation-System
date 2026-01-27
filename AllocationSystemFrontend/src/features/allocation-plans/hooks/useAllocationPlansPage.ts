import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";
import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { AllocationPlanService } from "../services/allocationPlanService";
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";

type PaginationResponse = {
  items?: AllocationPlan[];
  page?: number;
  pageSize?: number;
  totalItems?: number;
  totalPages?: number;
};

function getErrorMessage(err: unknown, fallback: string): string {
  if (err instanceof Error) {
    return err.message;
  }
  return fallback;
}

function throwError(message: string): never {
  throw new Error(message);
}

function setAndThrow(
  setError: (value: string | null) => void,
  message: string
): never {
  setError(message);
  throw new Error(message);
}

function useFailureHandler(t: (key: string) => string) {
  const [error, setError] = useState<string | null>(null);

  const handleFailure = useCallback(
    (err: unknown, fallbackKey: string, alsoSetError: boolean) => {
      const fallback = t(fallbackKey);
      const message = getErrorMessage(err, fallback);

      if (alsoSetError) {
        setError(message);
      }

      toast.error(message);
      return message;
    },
    [t]
  );

  return { error, setError, handleFailure };
}

function applyPagination(
  updatePagination: (p: {
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
  }) => void,
  response: PaginationResponse,
  current: { page: number; pageSize: number }
) {
  updatePagination({
    page: response.page ?? current.page,
    pageSize: response.pageSize ?? current.pageSize,
    totalItems: response.totalItems ?? 0,
    totalPages: response.totalPages ?? 0,
  });
}

async function fetchAllocationPlansPage(page: number, pageSize: number) {
  const response: PaginationResponse = await AllocationPlanService.getPaginated({
    page,
    pageSize,
    sortBy: "planName",
    sortOrder: "asc",
  });
  return response;
}

function runEffect(loadFn: () => Promise<void>) {
  loadFn().catch(() => {
    // loadFn already handles toast + error state
  });
}

async function loadAllocationPlansImpl(args: {
  page: number;
  pageSize: number;
  setLoading: (v: boolean) => void;
  setError: (v: string | null) => void;
  setAllocationPlans: (v: AllocationPlan[]) => void;
  updatePagination: (p: {
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
  }) => void;
  handleFailure: (
    err: unknown,
    fallbackKey: string,
    alsoSetError: boolean
  ) => string;
}) {
  args.setLoading(true);
  args.setError(null);

  try {
    const response = await fetchAllocationPlansPage(args.page, args.pageSize);
    args.setAllocationPlans(response.items ?? []);
    applyPagination(args.updatePagination, response, {
      page: args.page,
      pageSize: args.pageSize,
    });
  } catch (err) {
    args.handleFailure(err, "table.emptyMessage", true);
  } finally {
    args.setLoading(false);
  }
}

function useAllocationPlansData(t: (key: string) => string) {
  const [allocationPlans, setAllocationPlans] = useState<AllocationPlan[]>([]);
  const [loading, setLoading] = useState(false);
  const pager = usePagination(DEFAULT_TABLE_PAGE_SIZE);
  const failure = useFailureHandler(t);

  const loadAllocationPlans = useCallback(() => loadAllocationPlansImpl({
    page: pager.pagination.page,
    pageSize: pager.pagination.pageSize,
    setLoading,
    setError: failure.setError,
    setAllocationPlans,
    updatePagination: pager.updatePagination,
    handleFailure: failure.handleFailure,
  }), [failure.handleFailure, failure.setError, pager.pagination.page, pager.pagination.pageSize, pager.updatePagination]);

  useEffect(() => runEffect(loadAllocationPlans), [loadAllocationPlans]);

  return { allocationPlans, loading, error: failure.error, setError: failure.setError, pagination: pager.pagination, handlePageChange: pager.handlePageChange, handlePageSizeChange: pager.handlePageSizeChange, loadAllocationPlans, handleFailure: failure.handleFailure };
}


async function runCreateAllocationPlan(
  t: (key: string) => string,
  data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest
) {
  const createData = data as CreateAllocationPlanRequest;
  const { AllocationService } = await import("@/services/allocationService");

  const allocationResult = await AllocationService.runAllocation(
    createData.yearId,
    createData.isCurrent,
    createData.planVersion
  );

  toast.success(`${t("create.success")} - Plan ID: ${allocationResult.planId}`);
}

async function runUpdateAllocationPlan(
  t: (key: string) => string,
  id: number,
  data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest
) {
  await AllocationPlanService.update(id, data as UpdateAllocationPlanRequest);
  toast.success(t("update.success"));
}

async function runDeleteAllocationPlan(t: (key: string) => string, id: number) {
  await AllocationPlanService.delete(id);
  toast.success(t("delete.success"));
}

async function withSubmitting<T>(
  setIsSubmitting: (v: boolean) => void,
  work: () => Promise<T>
): Promise<T> {
  setIsSubmitting(true);
  try {
    return await work();
  } finally {
    setIsSubmitting(false);
  }
}

async function runMutation(
  work: () => Promise<void>,
  onError: (err: unknown) => never
) {
  try {
    await work();
  } catch (err) {
    onError(err);
  }
}
async function createWork(
  t: (key: string) => string,
  data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest,
  loadAllocationPlans: () => Promise<void>,
  setIsSubmitting: (v: boolean) => void
) {
  await withSubmitting(setIsSubmitting, async () => {
    await runCreateAllocationPlan(t, data);
    await loadAllocationPlans();
  });
}

async function updateWork(
  t: (key: string) => string,
  id: number,
  data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest,
  loadAllocationPlans: () => Promise<void>,
  setIsSubmitting: (v: boolean) => void
) {
  await withSubmitting(setIsSubmitting, async () => {
    await runUpdateAllocationPlan(t, id, data);
    await loadAllocationPlans();
  });
}

async function deleteWork(
  t: (key: string) => string,
  id: number,
  loadAllocationPlans: () => Promise<void>
) {
  await runDeleteAllocationPlan(t, id);
  await loadAllocationPlans();
}

function useAllocationPlansMutations(
  t: (key: string) => string,
  loadAllocationPlans: () => Promise<void>,
  handleFailure: (err: unknown, fallbackKey: string, alsoSetError: boolean) => string,
  setError: (value: string | null) => void
) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleCreate = useCallback(
    (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) =>
      runMutation(
        () => createWork(t, data, loadAllocationPlans, setIsSubmitting),
        (err) => throwError(handleFailure(err, "create.error", false))
      ),
    [handleFailure, loadAllocationPlans, t]
  );

  const handleUpdate = useCallback(
    (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest, id: number) =>
      runMutation(
        () => updateWork(t, id, data, loadAllocationPlans, setIsSubmitting),
        (err) => throwError(handleFailure(err, "update.error", false))
      ),
    [handleFailure, loadAllocationPlans, t]
  );

  const handleDelete = useCallback(
    (id: number) =>
      runMutation(
        () => deleteWork(t, id, loadAllocationPlans),
        (err) => setAndThrow(setError, handleFailure(err, "delete.error", true))
      ),
    [handleFailure, loadAllocationPlans, setError, t]
  );

  return { isSubmitting, handleCreate, handleUpdate, handleDelete };
}

export function useAllocationPlansPage() {
  const { t } = useTranslation("allocationPlans");
  const [selectedAllocationPlan, setSelectedAllocationPlan] =
    useState<AllocationPlan | null>(null);

  const data = useAllocationPlansData(t);
  const mutations = useAllocationPlansMutations(
    t,
    data.loadAllocationPlans,
    data.handleFailure,
    data.setError
  );

  return {
    allocationPlans: data.allocationPlans,
    loading: data.loading,
    error: data.error,
    selectedAllocationPlan,
    setSelectedAllocationPlan,
    isSubmitting: mutations.isSubmitting,
    pagination: data.pagination,
    handlePageChange: data.handlePageChange,
    handlePageSizeChange: data.handlePageSizeChange,
    loadAllocationPlans: data.loadAllocationPlans,
    handleCreate: mutations.handleCreate,
    handleUpdate: mutations.handleUpdate,
    handleDelete: mutations.handleDelete,
  };
}