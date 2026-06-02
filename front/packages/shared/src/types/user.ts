export interface User {
  id: number
  username: string
  email?: string | null
  avatar?: string | null
  role: 'USER' | 'ADMIN'
  articleCount?: number
  status?: 'ACTIVE' | 'BANNED'
  createdAt?: string
}
