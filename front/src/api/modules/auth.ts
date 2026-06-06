import request from '../request'
import type { ApiResponse } from '@/types/api'
import type { User } from '@/types/user'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  email?: string
}

export interface LoginResult {
  token: string
  expiresIn: number
  user: User
}

export const authApi = {
  login(data: LoginParams): Promise<ApiResponse<LoginResult>> {
    return request.post('/auth/login', data)
  },

  register(data: RegisterParams): Promise<ApiResponse<null>> {
    return request.post('/auth/register', data)
  },

  logout(): Promise<ApiResponse<null>> {
    return request.post('/auth/logout')
  }
}
