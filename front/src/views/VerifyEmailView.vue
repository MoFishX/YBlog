<template>
  <div class="container mx-auto px-4 py-16 max-w-sm">
    <div class="bg-white rounded-xl border border-zinc-100 p-8 shadow-sm text-center">
      <div v-if="loading" class="flex flex-col items-center gap-4">
        <div class="w-12 h-12 border-2 border-zinc-200 border-t-accent rounded-full animate-spin"></div>
        <p class="text-sm text-zinc-500">正在验证邮箱...</p>
      </div>

      <div v-else-if="error" class="flex flex-col items-center gap-4">
        <div class="w-14 h-14 rounded-full bg-red-50 flex items-center justify-center">
          <svg class="w-7 h-7 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </div>
        <h2 class="text-lg font-bold text-zinc-900 font-serif">验证失败</h2>
        <p class="text-sm text-zinc-500">{{ error }}</p>
        <RouterLink to="/" class="inline-block px-5 py-2.5 bg-zinc-900 text-white text-sm font-medium rounded-lg hover:bg-zinc-800 transition-colors duration-200">
          返回首页
        </RouterLink>
      </div>

      <div v-else class="flex flex-col items-center gap-4">
        <div class="w-14 h-14 rounded-full bg-emerald-50 flex items-center justify-center">
          <svg class="w-7 h-7 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 13l4 4L19 7"/>
          </svg>
        </div>
        <h2 class="text-lg font-bold text-zinc-900 font-serif">邮箱验证成功</h2>
        <p class="text-sm text-zinc-500">你的账号已激活，现在可以正常使用了。</p>
        <RouterLink to="/login" class="inline-block px-5 py-2.5 bg-zinc-900 text-white text-sm font-medium rounded-lg hover:bg-zinc-800 transition-colors duration-200">
          前往登录
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { authService } from '@/services/authService'

const route = useRoute()
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  const token = route.query.token as string
  if (!token) {
    loading.value = false
    error.value = '无效的验证链接'
    return
  }
  try {
    await authService.verifyEmail(token)
  } catch (e: any) {
    error.value = e?.response?.data?.message || '验证失败，链接可能已过期'
  } finally {
    loading.value = false
  }
})
</script>
