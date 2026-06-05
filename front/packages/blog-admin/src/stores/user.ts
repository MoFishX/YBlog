import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { authService } from '@/services/authService'
import type { User } from '@shared/types/user'

const REFRESH_WINDOW = 30 * 60 * 1000

export const useUserStore = defineStore('admin_user', () => {
  const user = ref<User | null>(storage.get('admin_user') || null)
  const token = ref<string>(storage.get('admin_token') || '')
  const expiresAt = ref<number>(storage.get('admin_expiresAt') || 0)
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
    storage.set('admin_token', t)
    storage.set('admin_expiresAt', Date.now() + exp * 1000)
    storage.set('admin_user', u)
    scheduleRefresh(exp * 1000)
  }

  function logout() {
    clearRefreshTimer()
    token.value = ''
    expiresAt.value = 0
    user.value = null
    storage.remove('admin_token')
    storage.remove('admin_expiresAt')
    storage.remove('admin_user')
  }

  function restore() {
    if (token.value) {
      const remaining = expiresAt.value - Date.now()
      if (remaining <= 0) {
        logout()
      } else {
        scheduleRefresh(remaining / 1000)
      }
    }
  }

  return { user, token, isLoggedIn, isAdmin, setAuth, logout, restore }
})
