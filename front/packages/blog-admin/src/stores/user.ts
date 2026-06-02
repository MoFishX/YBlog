import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import type { User } from '@shared/types/user'

export const useUserStore = defineStore('admin_user', () => {
  const user = ref<User | null>(null)
  const token = ref<string>(storage.get('admin_token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(t: string, u: User) {
    token.value = t
    user.value = u
    storage.set('admin_token', t)
  }

  function logout() {
    token.value = ''
    user.value = null
    storage.remove('admin_token')
  }

  return { user, token, isLoggedIn, isAdmin, setAuth, logout }
})
