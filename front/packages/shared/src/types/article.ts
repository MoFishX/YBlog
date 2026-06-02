import type { User } from './user'

export interface Tag {
  id: number
  name: string
  articleCount?: number
}

export interface ArticleListItem {
  id: number
  title: string
  summary: string
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

export interface SearchArticle {
  id: number
  title: string
  titleHighlight?: string
  summary: string
  contentHighlight?: string
  author: {
    id: number
    username: string
  }
  tags: Tag[]
  viewCount: number
  likeCount: number
  createdAt: string
}

export interface SearchResult {
  records: SearchArticle[]
  total: number
  page: number
  pageSize: number
  took: number
}

export interface ArticleQuery {
  page?: number
  pageSize?: number
  tagId?: number
  orderBy?: 'latest' | 'hot' | 'oldest'
}

export interface ArticleFormData {
  title: string
  content: string
  summary?: string
  coverImage?: string
  tagIds?: number[]
  status?: 'PUBLISHED' | 'DRAFT'
}
