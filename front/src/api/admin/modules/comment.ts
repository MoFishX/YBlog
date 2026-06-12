import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { AdminComment } from '@/types/comment'

export const commentApi = {
  getList(params: { page?: number; pageSize?: number; keyword?: string; articleId?: number }): Promise<ApiResponse<PageResult<AdminComment>>> {
    return request.get('/admin/comments', { params })
  },

  hide(commentId: number): Promise<ApiResponse<null>> {
    return request.put(`/admin/comments/${commentId}/hide`)
  },

  delete(commentId: number): Promise<ApiResponse<null>> {
    return request.delete(`/admin/comments/${commentId}`)
  }
}