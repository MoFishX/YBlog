<template>
  <header class="sticky top-0 z-50 bg-white border-b border-gray-200 shadow-sm">
    <div class="container mx-auto px-4 h-14 flex items-center justify-between">
      <RouterLink to="/" class="text-xl font-bold text-blue-600 hover:text-blue-700 transition-colors">
        MyBlog
      </RouterLink>

      <nav class="hidden md:flex items-center gap-6">
        <RouterLink to="/" class="text-sm text-gray-600 hover:text-blue-600 transition-colors">首页</RouterLink>
        <RouterLink to="/search" class="text-sm text-gray-600 hover:text-blue-600 transition-colors">搜索</RouterLink>
      </nav>

      <div class="flex items-center gap-3">
        <template v-if="userStore.isLoggedIn">
          <RouterLink :to="`/user/${userStore.user?.id}`" class="text-sm text-gray-600 hover:text-blue-600">
            {{ userStore.user?.username }}
          </RouterLink>
          <button @click="handleLogout" class="text-sm text-gray-500 hover:text-red-500 transition-colors">
            退出
          </button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="text-sm px-3 py-1.5 rounded bg-blue-600 text-white hover:bg-blue-700 transition-colors">
            登录
          </RouterLink>
          <RouterLink to="/register" class="text-sm text-gray-600 hover:text-blue-600 transition-colors">
            注册
          </RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'

const userStore = useUserStore()

async function handleLogout() {
  try {
    await authService.logout()
  } catch {
    // ignore
  }
  userStore.logout()
}
</script>
