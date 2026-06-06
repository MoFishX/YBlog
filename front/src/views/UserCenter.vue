<template>
  <div class="container mx-auto px-4 py-8 max-w-2xl">
    <div v-if="loading" class="animate-pulse space-y-4">
      <div class="bg-white rounded-lg border border-gray-100 p-8">
        <div class="flex gap-4">
          <div class="w-16 h-16 rounded-full bg-gray-100"></div>
          <div class="space-y-2 flex-1">
            <div class="h-6 bg-gray-100 rounded w-28"></div>
            <div class="h-4 bg-gray-100 rounded w-20"></div>
          </div>
        </div>
        <div class="mt-6 h-12 bg-gray-100 rounded w-24"></div>
      </div>
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-100 rounded-lg p-6 text-center">
      <p class="text-red-600 mb-3">{{ error }}</p>
      <button @click="fetchUser" class="px-4 py-2 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-800 transition-colors">重试</button>
    </div>

    <template v-else-if="user">
      <div class="bg-white rounded-lg border border-gray-100 p-8 mb-8">
        <div class="flex items-center gap-5 mb-6">
          <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center text-gray-500 text-2xl font-bold flex-shrink-0">
            {{ user.username.charAt(0).toUpperCase() }}
          </div>
          <div>
            <h1 class="text-xl font-bold text-gray-900 mb-1">{{ user.username }}</h1>
            <p class="text-sm text-gray-500">加入于 {{ formatDate(user.createdAt || '') }}</p>
          </div>
        </div>
        <div class="flex gap-6">
          <div class="bg-gray-50 rounded-lg px-6 py-4 text-center">
            <p class="text-2xl font-bold text-gray-900">{{ formatNumber(user.articleCount || 0) }}</p>
            <p class="text-xs text-gray-500 mt-1">文章</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/format'
import { userService } from '@/services/userService'

const route = useRoute()
const user = ref<any>(null)
const loading = ref(true)
const error = ref('')

async function fetchUser() {
  const id = Number(route.params.id)
  if (!id) { error.value = '用户不存在'; loading.value = false; return }
  loading.value = true; error.value = ''
  try { user.value = await userService.getUser(id) }
  catch (e: any) { error.value = e?.response?.data?.message || '加载失败' }
  finally { loading.value = false }
}

onMounted(() => { fetchUser() })
</script>
