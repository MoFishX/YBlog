<template>
  <header class="sticky top-0 z-50 bg-white border-b border-gray-100">
    <div class="container mx-auto px-4 h-14 flex items-center justify-between">
      <RouterLink to="/" class="text-xl font-semibold text-gray-900 hover:text-gray-600 transition-colors">
        Reasonix
      </RouterLink>

      <nav class="hidden md:flex items-center gap-1">
        <RouterLink to="/" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-md transition-colors">
          首页
        </RouterLink>
        <RouterLink to="/search" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-md transition-colors">
          搜索
        </RouterLink>
      </nav>

      <div class="hidden md:flex items-center gap-3">
        <template v-if="userStore.isLoggedIn && userStore.user">
          <RouterLink :to="`/user/${userStore.user.id}`" class="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 transition-colors">
            <span class="w-7 h-7 rounded-full bg-gray-100 flex items-center justify-center text-gray-500 text-xs font-medium">
              {{ userStore.user.username.charAt(0).toUpperCase() }}
            </span>
            {{ userStore.user.username }}
          </RouterLink>
          <RouterLink
            v-if="userStore.isAdmin"
            to="/admin"
            class="text-sm text-gray-500 hover:text-gray-900 transition-colors"
          >
            管理后台
          </RouterLink>
          <button @click="handleLogout" class="text-sm text-gray-400 hover:text-gray-700 transition-colors">
             退出</button>
        </template>
        <template v-else>
          <RouterLink to="/register" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-md transition-colors">
            注册
          </RouterLink>
          <RouterLink to="/login" class="px-4 py-2 text-sm text-white bg-gray-900 rounded-md hover:bg-gray-800 transition-colors">
            登录
          </RouterLink>
        </template>
      </div>

      <button @click="mobileOpen = !mobileOpen" class="md:hidden p-2 text-gray-600 rounded-md hover:bg-gray-50 transition-colors">
        <svg v-if="!mobileOpen" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
        <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <Transition name="slide-down">
      <div v-if="mobileOpen" class="md:hidden border-t border-gray-100 bg-white">
        <div class="container mx-auto px-4 py-4 space-y-1">
          <RouterLink to="/" @click="mobileOpen = false" class="block px-4 py-3 text-sm text-gray-700 rounded-md hover:bg-gray-50 transition-colors">
            首页
          </RouterLink>
          <RouterLink to="/search" @click="mobileOpen = false" class="block px-4 py-3 text-sm text-gray-700 rounded-md hover:bg-gray-50 transition-colors">
            搜索
          </RouterLink>
          <div class="border-t border-gray-50 pt-2 mt-2">
            <template v-if="userStore.isLoggedIn && userStore.user">
              <RouterLink :to="`/user/${userStore.user.id}`" @click="mobileOpen = false" class="block px-4 py-3 text-sm text-gray-700 rounded-md hover:bg-gray-50 transition-colors">
                个人中心
              </RouterLink>
              <RouterLink
                v-if="userStore.isAdmin"
                to="/admin"
                @click="mobileOpen = false"
                class="block px-4 py-3 text-sm text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
              >
                管理后台
              </RouterLink>
              <button @click="handleLogout" class="block w-full text-left px-4 py-3 text-sm text-gray-500 rounded-md hover:bg-gray-50 transition-colors">
                 退出登录</button>
            </template>
            <template v-else>
              <RouterLink to="/login" @click="mobileOpen = false" class="block px-4 py-3 text-sm text-white bg-gray-900 rounded-md text-center">
                登录
              </RouterLink>
              <RouterLink to="/register" @click="mobileOpen = false" class="block px-4 py-3 text-sm text-gray-600 rounded-md hover:bg-gray-50 text-center transition-colors mt-2">
                注册
              </RouterLink>
            </template>
          </div>
        </div>
      </div>
    </Transition>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'

const userStore = useUserStore()
const router = useRouter()
const mobileOpen = ref(false)

async function handleLogout() {
  mobileOpen.value = false
  try { await authService.logout() } catch { /* ignore */ }
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.2s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
