import { commentApi } from '@/api/admin/modules/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AdminComment } from '@/types/comment'
import type { PageResult } from '@/types/api'

export const commentService = {
  async getList(params: { page?: number; pageSize?: number; keyword?: string; articleId?: number }): Promise<PageResult<AdminComment>> {
    const res = await commentApi.getList(params)
    return res.data
  },

  async hide(commentId: number): Promise<void> {
    await commentApi.hide(commentId)
    ElMessage.success('操作成功')
  },

  async delete(commentId: number): Promise<void> {
    await ElMessageBox.confirm('确认永久删除该评论？此操作不可撤销。', '警告', { type: 'warning' })
    await commentApi.delete(commentId)
    ElMessage.success('评论已删除')
  }
}