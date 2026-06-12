import type { User } from './user'

export interface Comment {
  id: number
  content: string
  user: {
    id: number
    username: string
    avatar?: string | null
  }
  replyTo?: {
    id: number
    username: string
  } | null
  createdAt: string
}

export interface AdminComment {
  id: number
  content: string
  articleId: number
  articleTitle: string
  user: {
    id: number
    username: string
  }
  status: string
  createdAt: string
  deletedAt: string | null
}

export interface ReplyNotification {
  id: number
  content: string
  articleId: number
  articleTitle: string
  user: {
    id: number
    username: string
  }
  isRead: boolean
  createdAt: string
}

export interface CommentFormData {
  articleId: number
  content: string
  parentId?: number | null
}
