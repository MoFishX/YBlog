import { authApi } from '@/api/admin/modules/auth'
import type { LoginParams, LoginResult } from '@/api/admin/modules/auth'
import { ElMessage } from 'element-plus'

export const authService = {
  async login(params: LoginParams): Promise<LoginResult> {
    const res = await authApi.login(params)
    if (res.data.user.role !== 'ADMIN') {
      ElMessage.error('仅管理员可登录后台')
      throw new Error('仅管理员可登录后台')
    }
    return res.data
  },

  async logout(): Promise<void> {
    try {
      await authApi.logout()
    } catch {
      // ignore
    }
  }
}
