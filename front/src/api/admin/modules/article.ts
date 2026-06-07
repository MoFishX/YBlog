import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { ArticleListItem, Article, ArticleFormData } from '@/types/article'

export const articleApi = {
  getList(params: { page?: number; pageSize?: number; status?: string; keyword?: string }): Promise<ApiResponse<PageResult<ArticleListItem>>> {
    return request.get('/admin/articles', { params })
  },

  getDetail(id: number): Promise<ApiResponse<Article>> {
    return request.get(`/articles/${id}`)
  },

  create(data: ArticleFormData): Promise<ApiResponse<{ id: number; title: string; status: string; createdAt: string }>> {
    return request.post('/articles', data)
  },

  update(id: number, data: Partial<ArticleFormData>): Promise<ApiResponse<{ id: number; title: string; status: string; updatedAt: string }>> {
    return request.put(`/articles/${id}`, data)
  },

  delete(id: number): Promise<ApiResponse<null>> {
    return request.delete(`/admin/articles/${id}`)
  },

  batchDelete(ids: number[]): Promise<ApiResponse<{ deletedCount: number }>> {
    return request.post('/admin/articles/batch-delete', { ids })
  },

  review(id: number, data: { status: 'APPROVED' | 'REJECTED'; reason?: string }): Promise<ApiResponse<{ id: number; title: string; status: string }>> {
    return request.put(`/admin/articles/${id}/review`, data)
  }
}
