import { apiClient } from "@/lib/api-client";
import { BaseApiService, type ApiResponse } from "@/services/api/BaseApiService";
import type {
  SubjectCategory,
  CreateSubjectCategoryRequest,
  UpdateSubjectCategoryRequest,
  PaginatedSubjectCategoriesResponse,
  SubjectCategoriesListParams,
} from "../types/subjectCategory.types";

export class SubjectCategoryServiceClass extends BaseApiService<
  SubjectCategory,
  CreateSubjectCategoryRequest,
  UpdateSubjectCategoryRequest
> {
  constructor() {
    super("subject-categories");
  }

  // Custom method for paginated fetch
  async getPaginated(params: SubjectCategoriesListParams = {}): Promise<PaginatedSubjectCategoriesResponse["data"]> {
    const queryParams = new URLSearchParams();
    
    if (params.page !== undefined) queryParams.append("page", String(params.page));
    if (params.pageSize !== undefined) queryParams.append("pageSize", String(params.pageSize));
    if (params.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params.sortOrder) queryParams.append("sortOrder", params.sortOrder);
    if (params.searchValue) queryParams.append("searchValue", params.searchValue);

    const response = await apiClient.get<ApiResponse<PaginatedSubjectCategoriesResponse["data"]>>(
      `/subject-categories/paginate?${queryParams.toString()}`
    );
    return response.data;
  }
}

export const SubjectCategoryService = new SubjectCategoryServiceClass();