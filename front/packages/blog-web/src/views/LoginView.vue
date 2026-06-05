<template>
  <div class="container mx-auto px-4 py-16 max-w-sm">
    <div class="bg-white rounded-lg border border-gray-100 p-8">
      <div class="text-center mb-8">
        <h1 class="text-xl font-bold text-gray-900 mb-2">欢迎回来</h1>
        <p class="text-sm text-gray-500">登录你的账号继续探索</p>
      </div>

      <div v-if="error" class="bg-red-50 border border-red-100 rounded-md p-3 mb-6">
        <p class="text-sm text-red-600">{{ error }}</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm text-gray-700 mb-1.5">用户名</label>
          <input v-model="form.username" type="text" required minlength="2" maxlength="50"
            class="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-gray-900 focus:border-gray-900 transition-all"
            placeholder="请输入用户名" />
        </div>
        <div>
          <label class="block text-sm text-gray-700 mb-1.5">密码</label>
          <input v-model="form.password" type="password" required minlength="6" maxlength="100"
            class="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-gray-900 focus:border-gray-900 transition-all"
            placeholder="请输入密码" />
        </div>
        <button type="submit" :disabled="loading"
          class="w-full py-2.5 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-800 disabled:opacity-50 transition-colors">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <p class="text-center text-sm text-gray-500 mt-6">
        还没有账号？<RouterLink to="/register" class="text-gray-900 underline underline-offset-2 font-medium">立即注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true; error.value = ''
  try {
    const user = await authService.login(form.value)
    userStore.setUser(user)
    router.push('/')
  } catch (e: any) { error.value = e?.response?.data?.message || '登录失败' }
  finally { loading.value = false }
}
</script>
