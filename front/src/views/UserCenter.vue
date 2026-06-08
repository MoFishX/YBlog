<template>
  <div class="container mx-auto px-4 py-10 max-w-2xl">
    <div v-if="loading" class="animate-pulse space-y-4">
      <div class="bg-white rounded-xl border border-zinc-100 p-8">
        <div class="flex gap-4">
          <div class="w-16 h-16 rounded-full bg-zinc-100"></div>
          <div class="space-y-2 flex-1">
            <div class="h-6 bg-zinc-100 rounded w-28"></div>
            <div class="h-4 bg-zinc-100 rounded w-20"></div>
          </div>
        </div>
        <div class="mt-6 h-12 bg-zinc-100 rounded w-24"></div>
      </div>
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
      <p class="text-red-600 mb-3">{{ error }}</p>
      <button @click="fetchUser" class="px-5 py-2.5 bg-zinc-900 text-white text-sm font-medium rounded-lg hover:bg-zinc-800 transition-colors duration-200 cursor-pointer">重试</button>
    </div>

    <template v-else-if="user">

      <div v-if="isOwnProfile && user.status === 'INACTIVE'" class="bg-amber-50 border border-amber-200 rounded-xl p-5 mb-6">
        <div class="flex items-start justify-between gap-3">
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
            <div>
              <p class="text-sm font-semibold text-amber-800 mb-1">账号未激活</p>
              <p class="text-sm text-amber-600">激活后可发布文章和参与评论。</p>
            </div>
          </div>
          <RouterLink to="/settings" class="px-4 py-2 bg-amber-600 text-white text-sm font-medium rounded-lg hover:bg-amber-700 transition-colors duration-200 flex-shrink-0">
            去激活
          </RouterLink>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-zinc-100 p-8 mb-10 shadow-sm">
        <div class="flex items-center gap-5 mb-6">
          <div class="w-16 h-16 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-500 text-2xl font-bold flex-shrink-0 overflow-hidden">
            <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover" />
            <template v-else>{{ user.username.charAt(0).toUpperCase() }}</template>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2.5 mb-1">
              <h1 class="text-xl font-bold text-zinc-900 font-serif">{{ user.username }}</h1>
              <span
                class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                :class="statusClass"
              >
                <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="statusDotClass"></span>
                {{ statusLabel }}
              </span>
              <span v-if="user.role === 'ADMIN'" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-accent/10 text-accent">
                管理员
              </span>
            </div>
            <p class="text-sm text-zinc-500">加入于 {{ formatDate(user.createdAt || '') }}</p>
          </div>
          <div class="bg-zinc-50 rounded-xl px-5 py-3 text-center flex-shrink-0">
            <p class="text-xl font-bold text-zinc-900">{{ formatNumber(user.articleCount || 0) }}</p>
            <p class="text-xs text-zinc-500 font-medium">文章</p>
          </div>
        </div>

        <div v-if="isOwnProfile" class="flex gap-3 pt-4 border-t border-zinc-100">
          <RouterLink to="/dashboard" class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium text-zinc-600 bg-zinc-50 rounded-xl hover:bg-zinc-100 hover:text-zinc-900 transition-colors duration-200">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
            创作中心
          </RouterLink>
          <RouterLink to="/settings" class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium text-zinc-600 bg-zinc-50 rounded-xl hover:bg-zinc-100 hover:text-zinc-900 transition-colors duration-200">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
            账号设置
          </RouterLink>
        </div>
      </div>

      <h2 class="text-lg font-bold text-zinc-900 mb-5 font-serif">{{ isOwnProfile ? '我的文章' : 'Ta 的文章' }}</h2>
      <ArticleList :articles="articles" :loading="articlesLoading" />
      <Pagination v-if="total > pageSize" :current-page="page" :total="total" :page-size="pageSize" @change="handlePageChange" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { userService } from '@/services/userService'
import { articleService } from '@/services/articleService'
import ArticleList from '@/components/article/ArticleList.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { User } from '@/types/user'
import type { ArticleListItem } from '@/types/article'

const route = useRoute()
const userStore = useUserStore()
const user = ref<User | null>(null)
const loading = ref(true)
const error = ref('')
const articles = ref<ArticleListItem[]>([])
const articlesLoading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const isOwnProfile = computed(() => {
  const uid = Number(route.params.id)
  return uid > 0 && Number(userStore.user?.id) === uid
})

const statusLabel = computed(() => {
  const map: Record<string, string> = { ACTIVE: '正常', INACTIVE: '未激活', BANNED: '已封禁' }
  return map[user.value?.status || ''] || '未知'
})
const statusClass = computed(() => {
  const map: Record<string, string> = {
    ACTIVE: 'bg-emerald-50 text-emerald-700',
    INACTIVE: 'bg-amber-50 text-amber-700',
    BANNED: 'bg-red-50 text-red-600'
  }
  return map[user.value?.status || ''] || 'bg-zinc-50 text-zinc-500'
})
const statusDotClass = computed(() => {
  const map: Record<string, string> = {
    ACTIVE: 'bg-emerald-500',
    INACTIVE: 'bg-amber-500',
    BANNED: 'bg-red-500'
  }
  return map[user.value?.status || ''] || 'bg-zinc-400'
})

async function fetchUser() {
  const id = Number(route.params.id)
  if (!id) { error.value = '用户不存在'; loading.value = false; return }
  loading.value = true; error.value = ''
  try { user.value = await userService.getUser(id) }
  catch (e: any) { error.value = e?.response?.data?.message || '加载失败' }
  finally { loading.value = false }
}

async function fetchArticles() {
  const id = Number(route.params.id)
  if (!id) return
  articlesLoading.value = true
  try {
    const res = await articleService.getList({ authorId: id, page: page.value, pageSize: pageSize.value })
    articles.value = res.records; total.value = res.total
  } catch { /* silent */ }
  finally { articlesLoading.value = false }
}

function handlePageChange(p: number) { page.value = p; fetchArticles() }

onMounted(() => { fetchUser(); fetchArticles() })
</script>
