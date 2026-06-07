<template>
  <div class="login-page">
    <div class="login-card">
      <div class="text-center mb-8">
        <div class="w-12 h-12 bg-gray-900 rounded-xl flex items-center justify-center mx-auto mb-4">
          <span class="text-white text-lg font-bold">R</span>
        </div>
        <h2 class="text-xl font-semibold text-gray-900">Reasonix 管理后台</h2>
        <p class="text-sm text-gray-400 mt-1">仅管理员可登录</p>
      </div>

      <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mb-6" />

      <el-form @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password :prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="w-full" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'
import { ElMessage } from 'element-plus'

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
    if (res.user.role !== 'ADMIN') {
      ElMessage.error('仅管理员可登录后台')
      return
    }
    userStore.setAuth(res.accessToken, res.expiresIn, res.user)
    const redirect = (route.query.redirect as string) || '/admin'
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
  background: #f5f6fa;
}
.login-card {
  width: 400px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}
</style>
