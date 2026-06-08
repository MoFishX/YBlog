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
      <div class="bg-white rounded-xl border border-zinc-100 p-8 mb-10 shadow-sm">
        <div class="flex items-center gap-5 mb-6">
          <div class="w-16 h-16 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-500 text-2xl font-bold flex-shrink-0">
            {{ user.username.charAt(0).toUpperCase() }}
          </div>
          <div>
            <h1 class="text-xl font-bold text-zinc-900 mb-1 font-serif">{{ user.username }}</h1>
            <p class="text-sm text-zinc-500">加入于 {{ formatDate(user.createdAt || '') }}</p>
          </div>
        </div>
        <div class="flex gap-6">
          <div class="bg-zinc-50 rounded-xl px-6 py-4 text-center">
            <p class="text-2xl font-bold text-zinc-900">{{ formatNumber(user.articleCount || 0) }}</p>
            <p class="text-xs text-zinc-500 mt-1 font-medium">文章</p>
          </div>
        </div>
      </div>

      <h2 class="text-lg font-bold text-zinc-900 mb-5 font-serif">Ta 的文章</h2>
      <ArticleList :articles="articles" :loading="articlesLoading" />
      <Pagination v-if="total > pageSize" :current-page="page" :total="total" :page-size="pageSize" @change="handlePageChange" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/format'
import { userService } from '@/services/userService'
import { articleService } from '@/services/articleService'
import ArticleList from '@/components/article/ArticleList.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { ArticleListItem } from '@/types/article'

const route = useRoute()
const user = ref<any>(null)
const loading = ref(true)
const error = ref('')
const articles = ref<ArticleListItem[]>([])
const articlesLoading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

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
