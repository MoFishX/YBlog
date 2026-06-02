<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="text-2xl font-bold text-center mb-6">博客后台管理</h2>

      <div v-if="error" class="mb-4">
        <el-alert :title="error" type="error" show-icon />
      </div>

      <el-form @submit.prevent="handleLogin" label-width="0">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="w-full" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <p class="text-center text-sm text-gray-400 mt-4">仅管理员可登录</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await authService.login(form)
    userStore.setAuth(res.token, res.user)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    error.value = e?.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
