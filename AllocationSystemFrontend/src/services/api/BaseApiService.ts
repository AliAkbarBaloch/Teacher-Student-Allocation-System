import { apiClient } from "@/lib/api-client";

export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

export class BaseApiService<T, CreateDto, UpdateDto> {
  protected resourceUrl: string;

  constructor(resourceUrl: string) {
    this.resourceUrl = resourceUrl;
  }

  async getAll(): Promise<T[]> {
    const response = await apiClient.get<ApiResponse<T[]>>(`/${this.resourceUrl}`);
    return response.data;
  }

  async getById(id: number): Promise<T> {
    const response = await apiClient.get<ApiResponse<T>>(`/${this.resourceUrl}/${id}`);
    return response.data;
  }

  async create(data: CreateDto): Promise<T> {
    const response = await apiClient.post<ApiResponse<T>>(`/${this.resourceUrl}`, data);
    return response.data;
  }

  async update(id: number, data: UpdateDto): Promise<T> {
    const response = await apiClient.put<ApiResponse<T>>(`/${this.resourceUrl}/${id}`, data);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/${this.resourceUrl}/${id}`);
  }
}