import { userApi } from '@/api/modules/user'
import type { User } from '@/types/user'

export const userService = {
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
