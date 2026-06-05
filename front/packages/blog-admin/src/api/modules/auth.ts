import request from '../request'
import type { ApiResponse } from '@shared/types/api'
import type { User } from '@shared/types/user'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  expiresIn: number
  user: User
}

export interface RefreshResult {
  token: string
  expiresIn: number
}

export const authApi = {
  login(data: LoginParams): Promise<ApiResponse<LoginResult>> {
    return request.post('/auth/login', data)
  },

  refresh(): Promise<ApiResponse<RefreshResult>> {
    return request.post('/auth/refresh')
  },

  logout(): Promise<ApiResponse<null>> {
    return request.post('/auth/logout')
  }
}
