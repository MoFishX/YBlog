import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types/api'

export interface AdminComment {
  id: number
  content: string
  articleId: number
  articleTitle: string
  user: {
    id: number
    username: string
  }
  status: string
  createdAt: string
}

export const commentApi = {
  getList(params: { page?: number; pageSize?: number; keyword?: string; articleId?: number }): Promise<ApiResponse<PageResult<AdminComment>>> {
    return request.get('/admin/comments', { params })
  },

  delete(commentId: number): Promise<ApiResponse<null>> {
    return request.delete(`/admin/comments/${commentId}`)
  }
}
