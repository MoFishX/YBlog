import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { userService } from '@/services/userService'
import type { User } from '@shared/types/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(storage.get('user') || null)
  const ready = ref(false)

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setUser(u: User) {
    user.value = u
    storage.set('user', u)
  }

  function logout() {
    user.value = null
    storage.remove('user')
  }

  async function restore() {
    try {
      const u = await userService.getMe()
      setUser(u as User)
    } catch {
      logout()
    } finally {
      ready.value = true
    }
  }

  return { user, ready, isLoggedIn, isAdmin, setUser, logout, restore }
})
