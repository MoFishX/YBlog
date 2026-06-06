import { authApi } from '@/api/modules/auth'
import type { LoginParams, RegisterParams, LoginResult } from '@/api/modules/auth'

export const authService = {
  async login(params: LoginParams): Promise<LoginResult> {
    const res = await authApi.login(params)
    return res.data
  },

  async register(params: RegisterParams): Promise<void> {
    await authApi.register(params)
  },

  async logout(): Promise<void> {
    await authApi.logout()
  }
}
