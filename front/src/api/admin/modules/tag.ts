import request from '@/api/request'
import type { ApiResponse } from '@/types/api'
import { tagApi as publicTagApi } from '@/api/modules/tag'

export const tagApi = {
  getList: publicTagApi.getList,

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
