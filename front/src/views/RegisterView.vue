<template>
  <div class="container mx-auto px-4 py-16 max-w-sm">
    <div class="bg-white rounded-lg border border-gray-100 p-8">
      <div class="text-center mb-8">
        <h1 class="text-xl font-bold text-gray-900 mb-2">创建账号</h1>
        <p class="text-sm text-gray-500">加入 Reasonix，分享你的思考</p>
      </div>

      <div v-if="error" class="bg-red-50 border border-red-100 rounded-md p-3 mb-6">
        <p class="text-sm text-red-600">{{ error }}</p>
      </div>
      <div v-if="success" class="bg-green-50 border border-green-100 rounded-lg p-5 mb-6 text-center">
        <p class="text-sm text-green-600 font-medium mb-3">注册成功！</p>
        <RouterLink to="/login" class="inline-block px-4 py-2 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-800 transition-colors">前往登录</RouterLink>
      </div>

      <form v-if="!success" @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label class="block text-sm text-gray-700 mb-1.5">用户名</label>
          <input v-model="form.username" type="text" required minlength="2" maxlength="50" pattern="[a-zA-Z0-9_]+"
            class="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-gray-900 focus:border-gray-900 transition-all"
            placeholder="字母、数字、下划线" />
        </div>
        <div>
          <label class="block text-sm text-gray-700 mb-1.5">邮箱 <span class="text-gray-400">(选填)</span></label>
          <input v-model="form.email" type="email"
            class="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-gray-900 focus:border-gray-900 transition-all"
            placeholder="example@mail.com" />
        </div>
        <div>
          <label class="block text-sm text-gray-700 mb-1.5">密码</label>
          <input v-model="form.password" type="password" required minlength="6" maxlength="100"
            class="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-gray-900 focus:border-gray-900 transition-all"
            placeholder="至少6位" />
        </div>
        <button type="submit" :disabled="loading"
          class="w-full py-2.5 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-800 disabled:opacity-50 transition-colors">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <p v-if="!success" class="text-center text-sm text-gray-500 mt-6">
        已有账号？<RouterLink to="/login" class="text-gray-900 underline underline-offset-2 font-medium">立即登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { authService } from '@/services/authService'

const form = ref({ username: '', password: '', email: '' })
const loading = ref(false)
const error = ref('')
const success = ref(false)

async function handleRegister() {
  loading.value = true; error.value = ''
  try { await authService.register(form.value); success.value = true }
  catch (e: any) { error.value = e?.response?.data?.message || '注册失败' }
  finally { loading.value = false }
}
</script>
