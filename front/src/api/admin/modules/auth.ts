import request from '../request'
import type { ApiResponse } from '@/types/api'
import type { User } from '@/types/user'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  expiresIn: number
  user: User
}

export const authApi = {
  login(data: LoginParams): Promise<ApiResponse<LoginResult>> {
    return request.post('/auth/login', data)
  },

  refresh(): Promise<ApiResponse<LoginResult>> {
    return request.post('/auth/refresh')
  },

  logout(): Promise<ApiResponse<null>> {
    return request.post('/auth/logout')
  }
}
