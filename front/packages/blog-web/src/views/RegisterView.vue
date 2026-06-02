<template>
  <div class="container mx-auto px-4 py-16 max-w-md">
    <h1 class="text-2xl font-bold text-gray-900 text-center mb-8">注册</h1>

    <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
      <p class="text-sm text-red-600">{{ error }}</p>
    </div>
    <div v-if="success" class="bg-green-50 border border-green-200 rounded-lg p-4 mb-4 text-center">
      <p class="text-sm text-green-600 mb-3">注册成功！</p>
      <RouterLink to="/login" class="text-blue-600 hover:underline text-sm">前往登录</RouterLink>
    </div>

    <form v-if="!success" @submit.prevent="handleRegister" class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
        <input
          v-model="form.username"
          type="text"
          required
          minlength="2"
          maxlength="50"
          pattern="[a-zA-Z0-9_]+"
          class="w-full border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="字母、数字、下划线"
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">邮箱 <span class="text-gray-400">(选填)</span></label>
        <input
          v-model="form.email"
          type="email"
          class="w-full border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="example@mail.com"
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
          placeholder="至少6位"
        />
      </div>
      <button
        type="submit"
        :disabled="loading"
        class="w-full py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
      >
        {{ loading ? '注册中...' : '注册' }}
      </button>
    </form>

    <p v-if="!success" class="text-center text-sm text-gray-500 mt-6">
      已有账号？
      <RouterLink to="/login" class="text-blue-600 hover:underline">立即登录</RouterLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import { authService } from '@/services/authService'

const form = reactive({
  username: '',
  password: '',
  email: ''
})
const loading = ref(false)
const error = ref('')
const success = ref(false)

async function handleRegister() {
  loading.value = true
  error.value = ''
  try {
    await authService.register({
      username: form.username,
      password: form.password,
      email: form.email || undefined
    })
    success.value = true
  } catch (e: any) {
    error.value = e?.response?.data?.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>
