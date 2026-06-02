<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <div v-if="loading" class="space-y-4 animate-pulse">
      <div class="h-8 bg-gray-200 rounded w-48"></div>
      <div class="h-4 bg-gray-200 rounded w-32"></div>
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-center">
      <p class="text-red-600 mb-4">{{ error }}</p>
      <button @click="fetchUser" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">重试</button>
    </div>

    <template v-else-if="user">
      <div class="bg-white rounded-lg border border-gray-200 shadow-sm p-8 mb-8">
        <div class="flex items-center gap-4 mb-6">
          <div class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-2xl font-bold">
            {{ user.username.charAt(0).toUpperCase() }}
          </div>
          <div>
            <h1 class="text-xl font-bold text-gray-900">{{ user.username }}</h1>
            <p class="text-sm text-gray-500">
              加入于 {{ formatDate(user.createdAt || '') }}
            </p>
          </div>
        </div>
        <div class="flex gap-8 text-center">
          <div>
            <p class="text-2xl font-bold text-gray-900">{{ formatNumber(user.articleCount || 0) }}</p>
            <p class="text-sm text-gray-500">文章</p>
          </div>
        </div>
      </div>

      <h2 class="text-lg font-semibold text-gray-900 mb-4">Ta 的文章</h2>
      <ArticleList :articles="articles" :loading="articlesLoading" />
      <Pagination
        v-if="total > pageSize"
        :current-page="page"
        :total="total"
        :page-size="pageSize"
        @change="handlePageChange"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/storage'
import { userApi } from '@/api/modules/user'
import { articleService } from '@/services/articleService'
import ArticleList from '@/components/article/ArticleList.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { ArticleListItem } from '@shared/types/article'

const route = useRoute()

const user = ref<any>(null)
const loading = ref(true)
const error = ref('')
const articles = ref<ArticleListItem[]>([])
const articlesLoading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchUser() {
  loading.value = true
  error.value = ''
  try {
    const userId = Number(route.params.id)
    const res = await userApi.getUser(userId)
    user.value = res.data
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载用户信息失败'
  } finally {
    loading.value = false
  }
}

async function fetchArticles() {
  articlesLoading.value = true
  try {
    const res = await articleService.getList({ page: page.value, pageSize: pageSize.value })
    articles.value = res.records
    total.value = res.total
  } catch {
    // silent
  } finally {
    articlesLoading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchArticles()
}

onMounted(() => {
  fetchUser()
})
</script>
