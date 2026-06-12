import { tagApi } from '@/api/admin/modules/tag'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { PageResult } from '@/types/api'
import type { Tag } from '@/types/article'

export const tagService = {
  async getList(page: number, pageSize: number): Promise<PageResult<Tag>> {
    const res = await tagApi.getList({ page, pageSize })
    return res.data
  },

  async create(name: string): Promise<{ id: number; name: string }> {
    const res = await tagApi.create({ name })
    ElMessage.success('标签创建成功')
    return res.data
  },

  async update(id: number, name: string): Promise<void> {
    await tagApi.update(id, { name })
    ElMessage.success('标签更新成功')
  },

  async delete(id: number): Promise<void> {
    await ElMessageBox.confirm('确认删除该标签？', '警告', { type: 'warning' })
    await tagApi.delete(id)
    ElMessage.success('标签已删除')
  }
}
