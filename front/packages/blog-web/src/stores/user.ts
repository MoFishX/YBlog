import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import type { User } from '@shared/types/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(storage.get('user') || null)
  const token = ref<string>(storage.get('token') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(t: string, u: User) {
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

  return { user, token, isLoggedIn, isAdmin, setAuth, logout }
})
