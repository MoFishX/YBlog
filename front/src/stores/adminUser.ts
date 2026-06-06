import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { authApi } from '@/api/admin/modules/auth'
import type { User } from '@/types/user'

const KEYS = {
  accessToken: 'admin_access_token',
  tokenIssuedAt: 'admin_token_issued_at',
  expiresIn: 'admin_expires_in',
  user: 'admin_user'
}

let refreshPromise: Promise<string> | null = null

export const useUserStore = defineStore('admin_user', () => {
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
    storage.set(KEYS.tokenIssuedAt, Date.now())
    storage.set(KEYS.expiresIn, exp)
    storage.set(KEYS.user, u)
  }

  function logout() {
    token.value = ''
    user.value = null
    expiresIn.value = 0
    storage.remove(KEYS.accessToken)
    storage.remove(KEYS.tokenIssuedAt)
    storage.remove(KEYS.expiresIn)
    storage.remove(KEYS.user)
    refreshPromise = null
  }

  function shouldRefresh(): boolean {
    const issuedAt = storage.get<number>(KEYS.tokenIssuedAt) || 0
    const exp = expiresIn.value || storage.get<number>(KEYS.expiresIn) || 0
    if (!issuedAt || !exp) return false
    const elapsed = Date.now() - issuedAt
    const threshold = exp * 1000 * 0.8
    return elapsed > threshold
  }

  async function refreshAccessToken(): Promise<string> {
    if (refreshPromise) return refreshPromise

    refreshPromise = (async () => {
      try {
        const res = await authApi.refresh()
        const { accessToken, expiresIn: exp } = res.data
        token.value = accessToken
        expiresIn.value = exp
        storage.set(KEYS.accessToken, accessToken)
        storage.set(KEYS.tokenIssuedAt, Date.now())
        storage.set(KEYS.expiresIn, exp)
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

  function restore() {
    if (token.value && !user.value) {
      refreshAccessToken().catch(() => logout())
    }
  }

  return {
    user, token, expiresIn, isLoggedIn, isAdmin,
    setAuth, logout, restore, getValidToken, refreshAccessToken
  }
})
