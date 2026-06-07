import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type {
  ArticleListItem,
  Article,
  HotArticle,
  ArticleQuery,
  ArticleFormData
} from '@/types/article'

export const articleApi = {
  getList(params: ArticleQuery): Promise<ApiResponse<PageResult<ArticleListItem>>> {
    return request.get('/articles', { params })
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
    return request.delete(`/articles/${id}`)
  },

  like(id: number): Promise<ApiResponse<{ isLiked: boolean; likeCount: number }>> {
    return request.post(`/articles/${id}/like`)
  },

  getHot(limit?: number): Promise<ApiResponse<HotArticle[]>> {
    return request.get('/articles/hot', { params: { limit } })
  },

  search(keyword: string, page?: number, pageSize?: number): Promise<ApiResponse<PageResult<ArticleListItem>>> {
    return request.get('/articles/search', { params: { keyword, page, pageSize } })
  },

  getMine(params: { page?: number; pageSize?: number; status?: string }): Promise<ApiResponse<PageResult<ArticleListItem>>> {
    return request.get('/articles/mine', { params })
  },

  getAiSummary(id: number): Promise<ApiResponse<{ status: number; summary: string }>> {
    return request.get('/articles/ai', { params: { articleId: id } })
  }
}
