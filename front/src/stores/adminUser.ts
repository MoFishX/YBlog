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

  function restore() {
    if (token.value && !user.value) {
      refreshAccessToken().catch(() => logout())
    }
  }

  return {
    user, token, isLoggedIn, isAdmin,
    setAuth, logout, restore, refreshAccessToken
  }
})
