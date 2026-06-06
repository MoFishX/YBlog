import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import type { User } from '@/types/user'

export const useUserStore = defineStore('admin_user', () => {
  const user = ref<User | null>(storage.get('admin_user') || null)
  const token = ref<string>(storage.get('admin_token') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(t: string, _exp: number, u: User) {
    token.value = t
    user.value = u
    storage.set('admin_token', t)
    storage.set('admin_user', u)
  }

  function logout() {
    token.value = ''
    user.value = null
    storage.remove('admin_token')
    storage.remove('admin_user')
  }

  function restore() {
    if (!token.value) {
      logout()
    }
  }

  return { user, token, isLoggedIn, isAdmin, setAuth, logout, restore }
})
