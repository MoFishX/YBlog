import { commentApi } from '@/api/modules/comment'
import type { Comment, ReplyNotification, CommentFormData } from '@/types/comment'
import type { PageResult } from '@/types/api'

export const commentService = {
  async getList(params: { articleId: number; page?: number; pageSize?: number }): Promise<PageResult<Comment>> {
    const res = await commentApi.getList(params)
    return res.data
  },

  async create(data: CommentFormData): Promise<{ id: number; content: string; createdAt: string }> {
    const res = await commentApi.create(data)
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
