import request from '../request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { User } from '@/types/user'

export const userApi = {
  getMe(): Promise<ApiResponse<User & { articleCount: number; createdAt: string }>> {
    return request.get('/user/me')
  },

  getUser(userId: number): Promise<ApiResponse<Pick<User, 'id' | 'username' | 'avatar'> & { articleCount: number; createdAt: string }>> {
    return request.get(`/user/${userId}`)
  },

  updateProfile(data: { email?: string; avatar?: string }): Promise<ApiResponse<User>> {
    return request.put('/user/me', data)
  },

  changePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<null>> {
    return request.put('/user/me/password', data)
  }
}
