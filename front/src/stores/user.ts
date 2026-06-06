import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { authApi } from '@/api/modules/auth'
import type { User } from '@/types/user'

const KEYS = {
  accessToken: 'access_token',
  tokenIssuedAt: 'token_issued_at',
  expiresIn: 'expires_in',
  user: 'user'
}

let refreshPromise: Promise<string> | null = null
let refreshTimer: ReturnType<typeof setTimeout> | null = null

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(storage.get(KEYS.user) || null)
  const token = ref<string>(storage.get(KEYS.accessToken) || '')
  const expiresIn = ref<number>(storage.get(KEYS.expiresIn) || 0)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function scheduleNextRefresh() {
    stopRefreshTimer()
    const exp = expiresIn.value
    if (!exp) return
    // 在 token 80% 生命周期时刷新（最小 1 秒，最大 30 分钟）
    const delay = Math.max(1000, Math.min(exp * 1000 * 0.8, 30 * 60 * 1000))
    refreshTimer = setTimeout(async () => {
      try {
        await refreshAccessToken()
        // 刷新成功后，用新的 expiresIn 重新排期
        scheduleNextRefresh()
      } catch { /* 失败不重试 */ }
    }, delay)
  }

  function stopRefreshTimer() {
    if (refreshTimer != null) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
  }

  function setAuth(accessToken: string, exp: number, u: User) {
    const now = Date.now()
    token.value = accessToken
    expiresIn.value = exp
    user.value = u
    storage.set(KEYS.accessToken, accessToken)
    storage.set(KEYS.tokenIssuedAt, now)
    storage.set(KEYS.expiresIn, exp)
    storage.set(KEYS.user, u)
    scheduleNextRefresh()
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
    stopRefreshTimer()
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
        const { accessToken, expiresIn: exp, user: u } = res.data
        token.value = accessToken
        expiresIn.value = exp
        storage.set(KEYS.accessToken, accessToken)
        storage.set(KEYS.tokenIssuedAt, Date.now())
        storage.set(KEYS.expiresIn, exp)
        if (u) {
          user.value = u
          storage.set(KEYS.user, u)
        }
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
    // 清理明显损坏的 user 数据（如被旧 refresh 写入的 undefined）
    if (user.value && !user.value.username) {
      user.value = null
      storage.remove(KEYS.user)
    }
    if (!token.value) {
      // 主动试探：refreshToken cookie 可能仍然有效
      try { await refreshAccessToken() } catch { /* 静默忽略 */ }
    } else if (shouldRefresh() || !user.value) {
      try { await refreshAccessToken() } catch { logout() }
    }
    if (isLoggedIn.value) scheduleNextRefresh()
  }

  return {
    user, token, expiresIn, isLoggedIn, isAdmin,
    setAuth, logout, restore, getValidToken, refreshAccessToken, stopRefreshTimer
  }
})
