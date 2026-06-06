const PREFIX = 'blog_'

export const storage = {
  get<T = string>(key: string): T | null {
    try {
      const raw = localStorage.getItem(PREFIX + key)
      if (raw === null) return null
      return JSON.parse(raw) as T
    } catch {
      return localStorage.getItem(PREFIX + key) as unknown as T
    }
  },

  set<T = string>(key: string, value: T): void {
    try {
      localStorage.setItem(PREFIX + key, JSON.stringify(value))
    } catch {
      localStorage.setItem(PREFIX + key, String(value))
    }
  },

  remove(key: string): void {
    localStorage.removeItem(PREFIX + key)
  }
}
