import { userApi } from '@/api/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { User } from '@shared/types/user'
import type { PageResult } from '@shared/types/api'

export const userService = {
  async getList(params: { page?: number; pageSize?: number; keyword?: string }): Promise<PageResult<User>> {
    const res = await userApi.getList(params)
    return res.data
  },

  async toggleStatus(id: number, currentStatus: string): Promise<void> {
    const target = currentStatus === 'ACTIVE' ? 'BANNED' : 'ACTIVE'
    const label = target === 'BANNED' ? '封禁' : '解封'
    await ElMessageBox.confirm(`确认${label}该用户？`, '警告', { type: 'warning' })
    const res = await userApi.updateStatus(id, target)
    ElMessage.success(`用户已${label}`)
  },

  async changeRole(id: number, role: 'USER' | 'ADMIN'): Promise<void> {
    await ElMessageBox.confirm(`确认修改该用户角色为 ${role}？`, '提示', { type: 'warning' })
    const res = await userApi.changeRole(id, role)
    ElMessage.success('角色已更新')
  }
}
