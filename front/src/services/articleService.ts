import { articleApi } from '@/api/modules/article'
import type {
  ArticleListItem,
  Article,
  HotArticle,
  ArticleQuery,
  ArticleFormData
} from '@/types/article'
import type { PageResult } from '@/types/api'

export const articleService = {
  async getList(params: ArticleQuery): Promise<PageResult<ArticleListItem>> {
    const res = await articleApi.getList(params)
    return res.data
  },

  async getDetail(id: number): Promise<Article> {
    const res = await articleApi.getDetail(id)
    return res.data
  },

  async create(data: ArticleFormData): Promise<{ id: number; title: string; status: string; createdAt: string }> {
    const res = await articleApi.create(data)
    return res.data
  },

  async update(id: number, data: Partial<ArticleFormData>): Promise<{ id: number; title: string; status: string; updatedAt: string }> {
    const res = await articleApi.update(id, data)
    return res.data
  },

  async delete(id: number): Promise<void> {
    await articleApi.delete(id)
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

  async genSummaryLong(id: number): Promise<void> {
    await articleApi.genSummaryLong(id)
  }
}
