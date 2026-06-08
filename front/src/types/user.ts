export interface User {
  id: number
  username: string
  email?: string | null
  avatar?: string | null
  role: 'USER' | 'ADMIN'
  articleCount?: number
  status?: 'INACTIVE' | 'ACTIVE' | 'BANNED'
  createdAt?: string
}
