import { commentApi } from '@/api/modules/comment'
import type { Comment, ReplyNotification, CommentFormData } from '@shared/types/comment'
import type { PageResult } from '@shared/types/api'

export const commentService = {
  async getList(articleId: number, params?: { page?: number; pageSize?: number }): Promise<PageResult<Comment>> {
    const res = await commentApi.getList(articleId, params)
    return res.data
  },

  async create(articleId: number, data: CommentFormData): Promise<{ id: number; content: string; createdAt: string }> {
    const res = await commentApi.create(articleId, data)
    return res.data
  },

  async delete(commentId: number): Promise<void> {
    await commentApi.delete(commentId)
  },

  async getReplies(params?: { page?: number; pageSize?: number; unreadOnly?: number }): Promise<PageResult<ReplyNotification>> {
    const res = await commentApi.getReplies(params)
    return res.data
  }
}
