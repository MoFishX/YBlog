import { authApi } from '@/api/modules/auth'
import type { LoginParams, RegisterParams } from '@/api/modules/auth'
import type { User } from '@shared/types/user'

export const authService = {
  async login(params: LoginParams): Promise<User> {
    const res = await authApi.login(params)
    return res.data.user
  },

  async register(params: RegisterParams): Promise<void> {
    await authApi.register(params)
  },

  async logout(): Promise<void> {
    await authApi.logout()
  }
}
