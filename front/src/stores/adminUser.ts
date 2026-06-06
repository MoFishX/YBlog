import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { authApi } from '@/api/admin/modules/auth'
import type { User } from '@/types/user'

const KEYS = {
  accessToken: 'admin_access_token',
  tokenExpiry: 'admin_token_expiry',
  user: 'admin_user'
}

let refreshPromise: Promise<string> | null = null

export const useUserStore = defineStore('admin_user', () => {
  const user = ref<User | null>(storage.get(KEYS.user) || null)
  const token = ref<string>(storage.get(KEYS.accessToken) || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(accessToken: string, expiresIn: number, u: User) {
    token.value = accessToken
    user.value = u
    storage.set(KEYS.accessToken, accessToken)
    storage.set(KEYS.tokenExpiry, Date.now() + expiresIn * 1000)
    storage.set(KEYS.user, u)
  }

  function logout() {
    token.value = ''
    user.value = null
    storage.remove(KEYS.accessToken)
    storage.remove(KEYS.tokenExpiry)
    storage.remove(KEYS.user)
    refreshPromise = null
  }

  function getExpiry(): number {
    return storage.get<number>(KEYS.tokenExpiry) || 0
  }

  function isTokenExpired(): boolean {
    const expiry = getExpiry()
    if (!expiry) return false
    return Date.now() > expiry - 60 * 1000
  }

  async function refreshAccessToken(): Promise<string> {
    if (refreshPromise) return refreshPromise

    refreshPromise = (async () => {
      try {
        const res = await authApi.refresh()
        const { accessToken, expiresIn, user: u } = res.data
        token.value = accessToken
        user.value = u as User
        storage.set(KEYS.accessToken, accessToken)
        storage.set(KEYS.tokenExpiry, Date.now() + expiresIn * 1000)
        storage.set(KEYS.user, u)
        return accessToken
      } finally {
        refreshPromise = null
      }
    })()

    return refreshPromise
  }

  async function getValidToken(): Promise<string | null> {
    if (!token.value) return null
    if (isTokenExpired()) {
      try {
        return await refreshAccessToken()
      } catch {
        logout()
        return null
      }
    }
    return token.value
  }

  function restore() {
    if (!token.value) {
      logout()
    }
  }

  return {
    user, token, isLoggedIn, isAdmin,
    setAuth, logout, restore, getValidToken,
    refreshAccessToken
  }
})
