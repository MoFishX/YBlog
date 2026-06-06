import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { userService } from '@/services/userService'
import type { User } from '@shared/types/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(storage.get('user') || null)
  const token = ref<string>(storage.get('token') || '')
  const ready = ref(false)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(t: string, _exp: number, u: User) {
    token.value = t
    user.value = u
    storage.set('token', t)
    storage.set('user', u)
  }

  function logout() {
    token.value = ''
    user.value = null
    storage.remove('token')
    storage.remove('user')
  }

  async function restore() {
    try {
      if (token.value) {
        const u = await userService.getMe()
        user.value = u as User
        storage.set('user', u)
      }
    } catch {
      logout()
    } finally {
      ready.value = true
    }
  }

  return { user, token, ready, isLoggedIn, isAdmin, setAuth, logout, restore }
})
