import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { ArticleListItem } from '@/types/article'
import { articleApi as publicArticleApi } from '@/api/modules/article'

export const articleApi = {
  getDetail: publicArticleApi.getDetail,
  create: publicArticleApi.create,
  update: publicArticleApi.update,

  getList(params: { page?: number; pageSize?: number; status?: string; keyword?: string }): Promise<ApiResponse<PageResult<ArticleListItem>>> {
    return request.get('/admin/articles', { params })
  },

  delete(id: number): Promise<ApiResponse<null>> {
    return request.delete(`/admin/articles/${id}`)
  },

  batchDelete(ids: number[]): Promise<ApiResponse<null>> {
    return request.delete('/admin/articles', { params: { ids: ids.join(',') } })
  }
}
