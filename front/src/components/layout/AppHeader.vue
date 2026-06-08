<template>
  <header class="sticky top-0 z-50 bg-white/95 backdrop-blur-sm border-b border-zinc-100">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-14 flex items-center justify-between">
      <RouterLink to="/" class="text-xl font-semibold text-zinc-900 hover:text-accent transition-colors duration-200 font-serif">
        yvmouX Blog
      </RouterLink>

      <nav class="hidden md:flex items-center gap-1">
        <RouterLink to="/" class="px-4 py-2 text-sm font-medium text-zinc-600 hover:text-zinc-900 hover:bg-zinc-50 rounded-lg transition-colors duration-200">
          首页
        </RouterLink>
        <RouterLink to="/search" class="px-4 py-2 text-sm font-medium text-zinc-600 hover:text-zinc-900 hover:bg-zinc-50 rounded-lg transition-colors duration-200">
          搜索
        </RouterLink>
        <RouterLink to="/tags" class="px-4 py-2 text-sm font-medium text-zinc-600 hover:text-zinc-900 hover:bg-zinc-50 rounded-lg transition-colors duration-200">
          标签
        </RouterLink>
      </nav>

      <div class="hidden md:flex items-center gap-3">
        <template v-if="userStore.isLoggedIn && userStore.user">
          <RouterLink :to="`/user/${userStore.user.id}`" class="flex items-center gap-2 text-sm font-medium text-zinc-600 hover:text-zinc-900 transition-colors duration-200">
            <span class="w-7 h-7 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-500 text-xs font-semibold">
              {{ userStore.user.username.charAt(0).toUpperCase() }}
            </span>
            {{ userStore.user.username }}
          </RouterLink>
          <RouterLink
            to="/dashboard"
            class="text-sm font-medium text-zinc-500 hover:text-accent transition-colors duration-200"
          >
            创作中心
          </RouterLink>
          <RouterLink
            v-if="userStore.isAdmin"
            to="/admin"
            class="text-sm font-medium text-zinc-500 hover:text-accent transition-colors duration-200"
          >
            管理后台
          </RouterLink>
          <button @click="handleLogout" class="text-sm font-medium text-zinc-400 hover:text-zinc-700 transition-colors duration-200 cursor-pointer">
            退出</button>
        </template>
        <template v-else>
          <RouterLink to="/register" class="px-4 py-2 text-sm font-medium text-zinc-600 hover:text-zinc-900 hover:bg-zinc-50 rounded-lg transition-colors duration-200">
            注册
          </RouterLink>
          <RouterLink to="/login" class="px-4 py-2 text-sm font-medium text-white bg-zinc-900 rounded-lg hover:bg-zinc-800 transition-colors duration-200 cursor-pointer">
            登录
          </RouterLink>
        </template>
      </div>

      <button @click="mobileOpen = !mobileOpen" class="md:hidden p-2 text-zinc-600 rounded-lg hover:bg-zinc-50 transition-colors duration-200 cursor-pointer">
        <svg v-if="!mobileOpen" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
        <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <Transition name="slide-down">
      <div v-if="mobileOpen" class="md:hidden border-t border-zinc-100 bg-white">
        <div class="mx-auto max-w-7xl px-4 sm:px-6 py-4 space-y-1">
          <RouterLink to="/" @click="mobileOpen = false" class="block px-4 py-3 text-sm font-medium text-zinc-700 rounded-lg hover:bg-zinc-50 transition-colors duration-200">
            首页
          </RouterLink>
          <RouterLink to="/search" @click="mobileOpen = false" class="block px-4 py-3 text-sm font-medium text-zinc-700 rounded-lg hover:bg-zinc-50 transition-colors duration-200">
            搜索
          </RouterLink>
          <RouterLink to="/tags" @click="mobileOpen = false" class="block px-4 py-3 text-sm font-medium text-zinc-700 rounded-lg hover:bg-zinc-50 transition-colors duration-200">
            标签
          </RouterLink>
          <div class="border-t border-zinc-100 pt-2 mt-2">
            <template v-if="userStore.isLoggedIn && userStore.user">
              <RouterLink :to="`/user/${userStore.user.id}`" @click="mobileOpen = false" class="block px-4 py-3 text-sm font-medium text-zinc-700 rounded-lg hover:bg-zinc-50 transition-colors duration-200">
                个人中心
              </RouterLink>
              <RouterLink
                to="/dashboard"
                @click="mobileOpen = false"
                class="block px-4 py-3 text-sm font-medium text-zinc-700 rounded-lg hover:bg-zinc-50 transition-colors duration-200"
              >
                创作中心
              </RouterLink>
              <RouterLink
                v-if="userStore.isAdmin"
                to="/admin"
                @click="mobileOpen = false"
                class="block px-4 py-3 text-sm font-medium text-zinc-700 rounded-lg hover:bg-zinc-50 transition-colors duration-200"
              >
                管理后台
              </RouterLink>
              <button @click="handleLogout" class="block w-full text-left px-4 py-3 text-sm font-medium text-zinc-500 rounded-lg hover:bg-zinc-50 transition-colors duration-200 cursor-pointer">
                退出登录</button>
            </template>
            <template v-else>
              <RouterLink to="/login" @click="mobileOpen = false" class="block px-4 py-3 text-sm font-medium text-white bg-zinc-900 rounded-lg text-center">
                登录
              </RouterLink>
              <RouterLink to="/register" @click="mobileOpen = false" class="block px-4 py-3 text-sm font-medium text-zinc-600 rounded-lg hover:bg-zinc-50 text-center transition-colors duration-200 mt-2">
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
