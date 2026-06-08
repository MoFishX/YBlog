<template>
  <div class="container mx-auto px-4 py-16 max-w-sm">
    <div class="bg-white rounded-xl border border-zinc-100 p-8 shadow-sm">
      <div class="text-center mb-8">
        <h1 class="text-2xl font-bold text-zinc-900 mb-2 font-serif">创建账号</h1>
        <p class="text-sm text-zinc-500">加入 yvmouX Blog，分享你的思考</p>
      </div>

      <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-3 mb-6">
        <p class="text-sm text-red-600">{{ error }}</p>
      </div>
      <div v-if="success" class="bg-green-50 border border-green-200 rounded-xl p-6 mb-6 text-center">
        <p class="text-sm text-green-600 font-semibold mb-3">注册成功！</p>
        <RouterLink to="/login" class="inline-block px-5 py-2.5 bg-zinc-900 text-white text-sm font-medium rounded-lg hover:bg-zinc-800 transition-colors duration-200">前往登录</RouterLink>
      </div>

      <form v-if="!success" @submit.prevent="handleRegister" class="space-y-5">
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-2">用户名</label>
          <input v-model="form.username" type="text" required minlength="2" maxlength="50" pattern="[a-zA-Z0-9_]+"
            class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
            placeholder="字母、数字、下划线" />
        </div>
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-2">邮箱 <span class="text-zinc-400">(选填)</span></label>
          <input v-model="form.email" type="email"
            class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
            placeholder="example@mail.com" />
        </div>
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-2">密码</label>
          <input v-model="form.password" type="password" required minlength="6" maxlength="100"
            class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
            placeholder="至少6位" />
        </div>
        <button type="submit" :disabled="loading"
          class="w-full py-3 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <p v-if="!success" class="text-center text-sm text-zinc-500 mt-6">
        已有账号？<RouterLink to="/login" class="text-accent underline underline-offset-2 font-semibold">立即登录</RouterLink>
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
