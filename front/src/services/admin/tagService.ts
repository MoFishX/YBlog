import { tagApi } from '@/api/admin/modules/tag'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tagService as publicTagService } from '@/services/tagService'

export const tagService = {
  getList: publicTagService.getList,

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
