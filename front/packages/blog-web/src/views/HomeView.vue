<template>
  <div class="container mx-auto px-4 py-8">
    <div class="lg:grid lg:grid-cols-[1fr_300px] lg:gap-8">
      <div>
        <div class="flex items-center gap-3 mb-6">
          <h1 class="text-2xl font-bold text-gray-900">最新文章</h1>
          <select
            v-model="orderBy"
            @change="fetchArticles"
            class="text-sm border border-gray-300 rounded px-2 py-1 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="latest">最新</option>
            <option value="hot">最热</option>
            <option value="oldest">最早</option>
          </select>
        </div>

        <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <p class="text-sm text-red-600">{{ error }}</p>
          <button @click="fetchArticles" class="mt-2 text-sm text-red-600 underline">重试</button>
        </div>

        <ArticleList :articles="articles" :loading="loading" />

        <Pagination
          v-if="total > pageSize"
          :current-page="page"
          :total="total"
          :page-size="pageSize"
          @change="handlePageChange"
        />
      </div>

      <aside class="mt-8 lg:mt-0">
        <HotRank :list="hotList" :loading="hotLoading" />
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ArticleList from '@/components/article/ArticleList.vue'
import HotRank from '@/components/article/HotRank.vue'
import Pagination from '@/components/common/Pagination.vue'
import { articleService } from '@/services/articleService'
import type { ArticleListItem, HotArticle } from '@shared/types/article'

const articles = ref<ArticleListItem[]>([])
const hotList = ref<HotArticle[]>([])
const loading = ref(true)
const hotLoading = ref(true)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderBy = ref<'latest' | 'hot' | 'oldest'>('latest')

async function fetchArticles() {
  loading.value = true
  error.value = ''
  try {
    const res = await articleService.getList({ page: page.value, pageSize: pageSize.value, orderBy: orderBy.value })
    articles.value = res.records
    total.value = res.total
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载文章列表失败'
  } finally {
    loading.value = false
  }
}

async function fetchHot() {
  hotLoading.value = true
  try {
    hotList.value = await articleService.getHot(10)
  } catch {
    // silent fail
  } finally {
    hotLoading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchArticles()
}

onMounted(() => {
  fetchArticles()
  fetchHot()
})
</script>
