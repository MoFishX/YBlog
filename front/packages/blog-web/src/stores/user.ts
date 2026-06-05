import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { userService } from '@/services/userService'
import { authService } from '@/services/authService'
import type { User } from '@shared/types/user'

const REFRESH_WINDOW = 30 * 60 * 1000

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(storage.get('user') || null)
  const token = ref<string>(storage.get('token') || '')
  const expiresAt = ref<number>(storage.get('expiresAt') || 0)
  const ready = ref(false)
  let refreshTimer: ReturnType<typeof setTimeout> | null = null

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function clearRefreshTimer() {
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
  }

  function scheduleRefresh(expiresInSec: number) {
    clearRefreshTimer()
    const delayMs = expiresInSec * 1000
    const refreshDelay = Math.max(delayMs - REFRESH_WINDOW, 1000)
    refreshTimer = setTimeout(async () => {
      try {
        const res = await authService.refresh()
        setAuth(res.token, res.expiresIn, user.value!)
      } catch {
        logout()
      }
    }, refreshDelay)
  }

  function setAuth(t: string, exp: number, u: User) {
    token.value = t
    expiresAt.value = Date.now() + exp * 1000
    user.value = u
    storage.set('token', t)
    storage.set('expiresAt', Date.now() + exp * 1000)
    storage.set('user', u)
    scheduleRefresh(exp * 1000)
  }

  function logout() {
    clearRefreshTimer()
    token.value = ''
    expiresAt.value = 0
    user.value = null
    storage.remove('token')
    storage.remove('expiresAt')
    storage.remove('user')
  }

  async function restore() {
    try {
      if (token.value) {
        const remaining = expiresAt.value - Date.now()
        if (remaining <= 0) {
          logout()
        } else {
          scheduleRefresh(remaining / 1000)
          const u = await userService.getMe()
          user.value = u as User
          storage.set('user', u)
        }
      }
    } catch {
      logout()
    } finally {
      ready.value = true
    }
  }

  return { user, token, ready, isLoggedIn, isAdmin, setAuth, logout, restore }
})
