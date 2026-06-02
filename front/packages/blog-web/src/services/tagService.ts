import { tagApi } from '@/api/modules/tag'
import type { Tag } from '@shared/types/article'

export const tagService = {
  async getList(): Promise<Tag[]> {
    const res = await tagApi.getList()
    return res.data
  }
}
