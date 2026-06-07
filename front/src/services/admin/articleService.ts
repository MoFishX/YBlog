import { articleApi } from '@/api/admin/modules/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ArticleListItem, ArticleFormData } from '@/types/article'
import type { PageResult } from '@/types/api'
import { articleService as publicArticleService } from '@/services/articleService'

export const articleService = {
  getDetail: publicArticleService.getDetail,

  async getList(params: { page?: number; pageSize?: number; status?: string; keyword?: string }): Promise<PageResult<ArticleListItem>> {
    const res = await articleApi.getList(params)
    return res.data
  },

  async create(data: ArticleFormData): Promise<{ id: number; title: string; status: string; createdAt: string }> {
    const res = await articleApi.create(data)
    ElMessage.success('文章发布成功')
    return res.data
  },

  async update(id: number, data: Partial<ArticleFormData>): Promise<{ id: number; title: string; status: string; updatedAt: string }> {
    const res = await articleApi.update(id, data)
    ElMessage.success('文章更新成功')
    return res.data
  },

  async delete(ids: number[]): Promise<void> {
    if (ids.length === 0) return
    const msg = ids.length === 1 ? '确认删除该文章？此操作不可撤销。' : `确认删除选中的 ${ids.length} 篇文章？此操作不可撤销。`
    await ElMessageBox.confirm(msg, '警告', { type: 'warning' })
    await articleApi.delete(ids)
    ElMessage.success(ids.length === 1 ? '文章已删除' : `已删除 ${ids.length} 篇文章`)
  }
}
