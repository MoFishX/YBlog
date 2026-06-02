<template>
  <div class="container mx-auto px-4 py-16 max-w-md">
    <h1 class="text-2xl font-bold text-gray-900 text-center mb-8">登录</h1>

    <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
      <p class="text-sm text-red-600">{{ error }}</p>
    </div>

    <form @submit.prevent="handleLogin" class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
        <input
          v-model="form.username"
          type="text"
          required
          minlength="2"
          maxlength="50"
          class="w-full border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="请输入用户名"
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
        <input
          v-model="form.password"
          type="password"
          required
          minlength="6"
          maxlength="100"
          class="w-full border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="请输入密码"
        />
      </div>
      <button
        type="submit"
        :disabled="loading"
        class="w-full py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>

    <p class="text-center text-sm text-gray-500 mt-6">
      还没有账号？
      <RouterLink to="/register" class="text-blue-600 hover:underline">立即注册</RouterLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    const res = await authService.login(form)
    userStore.setAuth(res.token, res.user)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    error.value = e?.response?.data?.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>
