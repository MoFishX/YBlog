<template>
  <div class="container mx-auto px-4 py-8 max-w-lg">
    <div class="flex items-center gap-4 mb-8">
      <RouterLink :to="`/user/${userStore.user?.id}`" class="p-2 text-zinc-500 hover:text-zinc-900 hover:bg-zinc-100 rounded-lg transition-colors duration-200">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </RouterLink>
      <h1 class="text-2xl font-bold text-zinc-900 font-serif">账号设置</h1>
    </div>

    <div v-if="user" class="space-y-8">

      <div v-if="user.status === 'INACTIVE'" class="bg-amber-50 border border-amber-200 rounded-xl p-5">
        <div class="flex items-start gap-3">
          <svg class="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
          </svg>
          <div>
            <p class="text-sm font-semibold text-amber-800 mb-1">账号未激活</p>
            <p class="text-sm text-amber-600 mb-3">激活后即可使用完整功能，包括发布文章和评论。</p>
            <button
              :disabled="activating"
              @click="handleActivate"
              class="px-4 py-2 bg-amber-600 text-white text-sm font-medium rounded-lg hover:bg-amber-700 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
            >
              {{ activating ? '激活中...' : '立即激活' }}
            </button>
            <p v-if="activateMsg" class="text-sm mt-2" :class="activateError ? 'text-red-500' : 'text-emerald-600'">{{ activateMsg }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-zinc-100 p-6">
        <h2 class="text-base font-bold text-zinc-900 mb-5 font-serif">个人信息</h2>
        <form @submit.prevent="handleSaveProfile" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-2">用户名</label>
            <input
              :value="user.username"
              disabled
              class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm text-zinc-400 bg-zinc-50"
            />
            <p class="text-xs text-zinc-400 mt-1">用户名不可修改</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-2">邮箱</label>
            <input
              v-model="profileForm.email"
              type="email"
              class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
              placeholder="输入邮箱地址"
            />
          </div>
          <div v-if="profileError" class="text-sm text-red-500">{{ profileError }}</div>
          <div v-if="profileSuccess" class="text-sm text-emerald-600">{{ profileSuccess }}</div>
          <button
            type="submit"
            :disabled="savingProfile"
            class="px-5 py-2.5 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
          >
            {{ savingProfile ? '保存中...' : '保存' }}
          </button>
        </form>
      </div>

      <div class="bg-white rounded-xl border border-zinc-100 p-6">
        <h2 class="text-base font-bold text-zinc-900 mb-5 font-serif">修改密码</h2>
        <form @submit.prevent="handleChangePassword" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-2">当前密码</label>
            <input
              v-model="pwdForm.oldPassword"
              type="password"
              required
              class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-2">新密码</label>
            <input
              v-model="pwdForm.newPassword"
              type="password"
              required
              minlength="6"
              class="w-full border border-zinc-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
              placeholder="至少6位"
            />
          </div>
          <div v-if="pwdError" class="text-sm text-red-500">{{ pwdError }}</div>
          <div v-if="pwdSuccess" class="text-sm text-emerald-600">{{ pwdSuccess }}</div>
          <button
            type="submit"
            :disabled="changingPwd"
            class="px-5 py-2.5 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
          >
            {{ changingPwd ? '修改中...' : '修改密码' }}
          </button>
        </form>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userService } from '@/services/userService'
import { authService } from '@/services/authService'
import type { User } from '@/types/user'

const userStore = useUserStore()
const user = ref<User | null>(null)

const profileForm = reactive({ email: '' })
const savingProfile = ref(false)
const profileError = ref('')
const profileSuccess = ref('')

const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const changingPwd = ref(false)
const pwdError = ref('')
const pwdSuccess = ref('')

const activating = ref(false)
const activateMsg = ref('')
const activateError = ref(false)

onMounted(() => {
  if (userStore.user) {
    user.value = { ...userStore.user }
    profileForm.email = userStore.user.email || ''
  }
})

async function handleActivate() {
  activating.value = true
  activateMsg.value = ''
  activateError.value = false
  try {
    await authService.activate()
    activateMsg.value = '激活成功！'
    if (user.value) user.value.status = 'ACTIVE'
    if (userStore.user) userStore.user.status = 'ACTIVE'
  } catch (e: any) {
    activateError.value = true
    activateMsg.value = e?.response?.data?.message || '激活失败'
  } finally {
    activating.value = false
  }
}

async function handleSaveProfile() {
  savingProfile.value = true
  profileError.value = ''
  profileSuccess.value = ''
  try {
    const updated = await userService.updateProfile({ email: profileForm.email || undefined })
    if (user.value) {
      user.value.email = updated.email
      user.value.avatar = updated.avatar
    }
    if (userStore.user) {
      userStore.user.email = updated.email
      userStore.user.avatar = updated.avatar
    }
    profileSuccess.value = '保存成功'
  } catch (e: any) {
    profileError.value = e?.response?.data?.message || '保存失败'
  } finally {
    savingProfile.value = false
  }
}

async function handleChangePassword() {
  if (pwdForm.newPassword.length < 6) {
    pwdError.value = '新密码至少6位'
    return
  }
  changingPwd.value = true
  pwdError.value = ''
  pwdSuccess.value = ''
  try {
    await userService.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdSuccess.value = '密码修改成功'
  } catch (e: any) {
    pwdError.value = e?.response?.data?.message || '修改失败'
  } finally {
    changingPwd.value = false
  }
}
</script>
