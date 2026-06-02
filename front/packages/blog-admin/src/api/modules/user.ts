import request from '../request'
import type { ApiResponse, PageResult } from '@shared/types/api'
import type { User } from '@shared/types/user'

export const userApi = {
  getList(params: { page?: number; pageSize?: number; keyword?: string }): Promise<ApiResponse<PageResult<User>>> {
    return request.get('/admin/users', { params })
  },

  updateStatus(id: number, status: 'ACTIVE' | 'BANNED'): Promise<ApiResponse<{ id: number; username: string; status: string }>> {
    return request.put(`/admin/users/${id}/status`, { status })
  },

  changeRole(id: number, role: 'USER' | 'ADMIN'): Promise<ApiResponse<{ id: number; username: string; role: string }>> {
    return request.put(`/admin/users/${id}/role`, { role })
  }
}
