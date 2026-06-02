import { userApi } from '@/api/modules/user'
import type { User } from '@shared/types/user'

export const userService = {
  async getMe() {
    const res = await userApi.getMe()
    return res.data
  },

  async getUser(userId: number) {
    const res = await userApi.getUser(userId)
    return res.data
  },

  async updateProfile(data: { email?: string; avatar?: string }): Promise<User> {
    const res = await userApi.updateProfile(data)
    return res.data
  },

  async changePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
    await userApi.changePassword(data)
  }
}
