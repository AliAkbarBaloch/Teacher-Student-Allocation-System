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
    const queryParams = this.buildQueryParams(params);

    const response = await apiClient.get<ApiResponse<PaginatedSubjectCategoriesResponse["data"]>>(
      `/subject-categories/paginate?${queryParams.toString()}`
    );
    return response.data;
  }
}

export const SubjectCategoryService = new SubjectCategoryServiceClass();