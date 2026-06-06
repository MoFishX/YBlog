import request from '../request'
import type { ApiResponse } from '@/types/api'
import type { Tag } from '@/types/article'

export const tagApi = {
  getList(): Promise<ApiResponse<Tag[]>> {
    return request.get('/tags')
  }
}
