import request from '../request'
import type { ApiResponse } from '@shared/types/api'
import type { Tag } from '@shared/types/article'

export const tagApi = {
  getList(): Promise<ApiResponse<Tag[]>> {
    return request.get('/tags')
  }
}
