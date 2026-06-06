import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { authApi } from '@/api/modules/auth'
import type { User } from '@/types/user'

const KEYS = {
  accessToken: 'access_token',
  tokenExpiry: 'token_expiry',
  expiresIn: 'expires_in',
  user: 'user'
}

let refreshPromise: Promise<string> | null = null

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(storage.get(KEYS.user) || null)
  const token = ref<string>(storage.get(KEYS.accessToken) || '')
  const expiresIn = ref<number>(storage.get(KEYS.expiresIn) || 0)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(accessToken: string, exp: number, u: User) {
    token.value = accessToken
    expiresIn.value = exp
    user.value = u
    storage.set(KEYS.accessToken, accessToken)
    storage.set(KEYS.tokenExpiry, Date.now() + exp * 1000)
    storage.set(KEYS.expiresIn, exp)
    storage.set(KEYS.user, u)
  }

  function logout() {
    token.value = ''
    user.value = null
    expiresIn.value = 0
    storage.remove(KEYS.accessToken)
    storage.remove(KEYS.tokenExpiry)
    storage.remove(KEYS.expiresIn)
    storage.remove(KEYS.user)
    refreshPromise = null
  }

  function getExpiry(): number {
    return storage.get<number>(KEYS.tokenExpiry) || 0
  }

  function shouldRefresh(): boolean {
    const expiry = getExpiry()
    const exp = expiresIn.value || storage.get<number>(KEYS.expiresIn) || 0
    if (!expiry || !exp) return false
    const threshold = exp * 1000 * 0.2
    return Date.now() > expiry - threshold
  }

  async function refreshAccessToken(): Promise<string> {
    if (refreshPromise) return refreshPromise

    refreshPromise = (async () => {
      try {
        const res = await authApi.refresh()
        const { accessToken, expiresIn: exp, user: u } = res.data
        token.value = accessToken
        expiresIn.value = exp
        user.value = u
        storage.set(KEYS.accessToken, accessToken)
        storage.set(KEYS.tokenExpiry, Date.now() + exp * 1000)
        storage.set(KEYS.expiresIn, exp)
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
    if (shouldRefresh()) {
      try {
        return await refreshAccessToken()
      } catch {
        logout()
        return null
      }
    }
    return token.value
  }

  async function restore() {
    if (!token.value) return
    if (shouldRefresh() || !user.value) {
      try { await refreshAccessToken() } catch { logout() }
    }
  }

  return {
    user, token, expiresIn, isLoggedIn, isAdmin,
    setAuth, logout, restore, getValidToken, refreshAccessToken
  }
})
