import type { User } from './user'

export interface Tag {
  id: number
  name: string
  articleCount?: number
  createdBy?: number
}

export interface ArticleListItem {
  id: number
  title: string
  summary: string
  aiSummary?: string
  aiSummaryStatus?: number
  aiSummaryLong?: string
  aiSummaryLongStatus?: number
  coverImage?: string | null
  author: {
    id: number
    username: string
    avatar?: string | null
  }
  tags: Tag[]
  viewCount: number
  likeCount: number
  commentCount: number
  createdAt: string
  updatedAt?: string
  status?: string
}

export interface Article {
  id: number
  title: string
  content: string
  summary: string
  coverImage?: string | null
  status: 'PUBLISHED' | 'DRAFT' | 'REVIEWING' | 'REJECTED' | 'APPROVED'
  author: {
    id: number
    username: string
    avatar?: string | null
  }
  tags: Tag[]
  viewCount: number
  likeCount: number
  isLiked?: boolean
  commentCount?: number
  aiSummary?: string
  aiSummaryStatus?: number
  aiSummaryLong?: string
  aiSummaryLongStatus?: number
  createdAt: string
  updatedAt?: string
}

export interface HotArticle {
  rank: number
  id: number
  title: string
  summary: string
  viewCount: number
  likeCount: number
  author: {
    id: number
    username: string
    avatar?: string | null
  }
  createdAt: string
}

export interface ArticleQuery {
  page?: number
  pageSize?: number
  tagId?: number
  tagName?: string
  orderBy?: 'latest' | 'hot' | 'oldest'
  authorId?: number
}

export interface ArticleFormData {
  title: string
  content: string
  summary?: string
  coverImage?: string
  tagIds?: number[]
  tagNames?: string[]
  status?: 'PUBLISHED' | 'DRAFT'
  genAiSummary?: 0 | 1
  genAiSummaryLong?: 0 | 1
}
