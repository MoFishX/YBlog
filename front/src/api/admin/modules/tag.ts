import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Tag } from '@/types/article'

export const tagApi = {
  getList(params: { page: number; pageSize: number }): Promise<ApiResponse<PageResult<Tag>>> {
    return request.get('/admin/tags', { params })
  },

  create(data: { name: string }): Promise<ApiResponse<{ id: number; name: string }>> {
    return request.post('/admin/tags', data)
  },

  update(id: number, data: { name: string }): Promise<ApiResponse<{ id: number; name: string }>> {
    return request.put(`/admin/tags/${id}`, data)
  },

  delete(id: number): Promise<ApiResponse<null>> {
    return request.delete(`/admin/tags/${id}`)
  }
}
