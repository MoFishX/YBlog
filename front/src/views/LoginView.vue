<template>
  <div class="container mx-auto px-4 py-16 max-w-sm">
    <div class="bg-white rounded-xl border border-zinc-100 p-8 shadow-sm">
      <div class="text-center mb-8">
        <h1 class="text-2xl font-bold text-zinc-900 mb-2 font-serif">欢迎回来</h1>
        <p class="text-sm text-zinc-500">登录你的账号继续探索</p>
      </div>

      <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-3 mb-6">
        <p class="text-sm text-red-600">{{ error }}</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-5">
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-2">用户名</label>
          <input v-model="form.username" type="text" required minlength="2" maxlength="50"
            class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
            placeholder="请输入用户名" />
        </div>
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-2">密码</label>
          <input v-model="form.password" type="password" required minlength="6" maxlength="100"
            class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
            placeholder="请输入密码" />
        </div>
        <button type="submit" :disabled="loading"
          class="w-full py-3 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <p class="text-center text-sm text-zinc-500 mt-6">
        还没有账号？<RouterLink to="/register" class="text-accent underline underline-offset-2 font-semibold">立即注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true; error.value = ''
  try {
    const res = await authService.login(form.value)
    userStore.setAuth(res.accessToken, res.expiresIn, res.user)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) { error.value = e?.response?.data?.message || '登录失败' }
  finally { loading.value = false }
}
</script>
