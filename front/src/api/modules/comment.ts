import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Comment, ReplyNotification, CommentFormData } from '@/types/comment'

export const commentApi = {
  getList(params: { articleId: number; page?: number; pageSize?: number }): Promise<ApiResponse<PageResult<Comment>>> {
    return request.get('/comments', { params })
  },

  create(data: CommentFormData): Promise<ApiResponse<{ id: number; content: string; createdAt: string }>> {
    return request.post('/comments', data)
  },

  delete(commentId: number): Promise<ApiResponse<null>> {
    return request.delete(`/comments/${commentId}`)
  },

  getReplies(params?: { page?: number; pageSize?: number; unreadOnly?: number }): Promise<ApiResponse<PageResult<ReplyNotification>>> {
    return request.get('/comments/replies', { params })
  }
}
