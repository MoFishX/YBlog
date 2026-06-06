import request from '../request'
import type { ApiResponse } from '@/types/api'
import type { Tag } from '@/types/article'

export const tagApi = {
  getList(): Promise<ApiResponse<Tag[]>> {
    return request.get('/tags')
  },

  create(data: { name: string }): Promise<ApiResponse<{ id: number; name: string }>> {
    return request.post('/tags', data)
  },

  update(id: number, data: { name: string }): Promise<ApiResponse<{ id: number; name: string }>> {
    return request.put(`/tags/${id}`, data)
  },

  delete(id: number): Promise<ApiResponse<null>> {
    return request.delete(`/tags/${id}`)
  }
}
