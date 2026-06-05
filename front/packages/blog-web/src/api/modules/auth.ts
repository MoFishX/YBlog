import request from '../request'
import type { ApiResponse } from '@shared/types/api'
import type { User } from '@shared/types/user'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  email?: string
}

export const authApi = {
  login(data: LoginParams): Promise<ApiResponse<{ user: User }>> {
    return request.post('/auth/login', data)
  },

  register(data: RegisterParams): Promise<ApiResponse<null>> {
    return request.post('/auth/register', data)
  },

  logout(): Promise<ApiResponse<null>> {
    return request.post('/auth/logout')
  }
}
