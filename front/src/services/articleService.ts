import { articleApi } from '@/api/modules/article'
import type {
  ArticleListItem,
  Article,
  HotArticle,
  ArticleQuery,
  ArticleFormData
} from '@/types/article'
import type { PageResult } from '@/types/api'

const CACHE_TTL = 3 * 60 * 1000
const listCache = new Map<string, { data: PageResult<ArticleListItem>; timestamp: number }>()

export const articleService = {
  clearListCache() {
    listCache.clear()
  },

  async getList(params: ArticleQuery): Promise<PageResult<ArticleListItem>> {
    const cacheKey = JSON.stringify(params)
    const cached = listCache.get(cacheKey)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      return cached.data
    }
    const res = await articleApi.getList(params)
    listCache.set(cacheKey, { data: res.data, timestamp: Date.now() })
    return res.data
  },

  async getDetail(id: number): Promise<Article> {
    const res = await articleApi.getDetail(id)
    return res.data
  },

  async create(data: ArticleFormData): Promise<{ id: number; title: string; status: string; createdAt: string }> {
    const res = await articleApi.create(data)
    this.clearListCache()
    return res.data
  },

  async update(id: number, data: Partial<ArticleFormData>): Promise<{ id: number; title: string; status: string; updatedAt: string }> {
    const res = await articleApi.update(id, data)
    this.clearListCache()
    return res.data
  },

  async delete(id: number): Promise<void> {
    await articleApi.delete(id)
    this.clearListCache()
  },

  async like(id: number): Promise<{ isLiked: boolean; likeCount: number }> {
    const res = await articleApi.like(id)
    return res.data
  },

  async getHot(limit?: number): Promise<HotArticle[]> {
    const res = await articleApi.getHot(limit)
    return res.data
  },

  async search(keyword: string, page?: number, pageSize?: number): Promise<PageResult<ArticleListItem>> {
    const res = await articleApi.search(keyword, page, pageSize)
    return res.data
  },

  async getMine(params: { page?: number; pageSize?: number; status?: string }): Promise<PageResult<ArticleListItem>> {
    const res = await articleApi.getMine(params)
    return res.data
  },

  async getAiSummary(id: number): Promise<{ status: number; summary: string }> {
    const res = await articleApi.getAiSummary(id)
    return res.data
  },

  async genAiSummary(id: number): Promise<void> {
    await articleApi.genAiSummary(id)
  }
}
