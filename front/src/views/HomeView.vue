<template>
  <div>
    <section class="bg-white border-b border-gray-100">
      <div class="container mx-auto px-4 py-16 md:py-20 text-center">
        <h1 class="text-3xl md:text-4xl font-bold text-gray-900 mb-3 tracking-tight">
          探索技术的无限可能
        </h1>
        <p class="text-base text-gray-500 mb-8 max-w-xl mx-auto">
          在这里发现关于前端、后端、架构与设计的前沿思考与实践
        </p>
        <div class="flex items-center justify-center gap-3">
          <RouterLink to="/search" class="px-5 py-2.5 text-sm font-medium text-white bg-gray-900 rounded-md hover:bg-gray-800 transition-colors">
            探索文章
          </RouterLink>
          <RouterLink v-if="!userStore.isLoggedIn" to="/register" class="px-5 py-2.5 text-sm font-medium text-gray-600 border border-gray-200 rounded-md hover:bg-gray-50 transition-colors">
            加入我们
          </RouterLink>
        </div>
      </div>
    </section>

    <div class="container mx-auto px-4 py-8">
      <div class="lg:grid lg:grid-cols-[1fr_300px] lg:gap-8">
        <div>
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-gray-900">最新文章</h2>
            <div class="flex bg-gray-100 rounded-md p-0.5">
              <button
                v-for="opt in orderOptions"
                :key="opt.value"
                @click="orderBy = opt.value; fetchArticles()"
                class="px-3 py-1 text-xs rounded transition-colors"
                :class="orderBy === opt.value ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>

          <div v-if="error" class="bg-red-50 border border-red-100 rounded-lg p-4 text-center mb-6">
            <p class="text-sm text-red-600 mb-2">{{ error }}</p>
            <button @click="fetchArticles" class="text-sm text-red-600 underline">重试</button>
          </div>

          <ArticleList :articles="articles" :loading="loading" />

          <Pagination v-if="total > pageSize" :current-page="page" :total="total" :page-size="pageSize" @change="handlePageChange" />
        </div>

        <aside class="mt-8 lg:mt-0">
          <HotRank :list="hotList" :loading="hotLoading" />
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import ArticleList from '@/components/article/ArticleList.vue'
import HotRank from '@/components/article/HotRank.vue'
import Pagination from '@/components/common/Pagination.vue'
import { articleService } from '@/services/articleService'
import type { ArticleListItem, HotArticle } from '@/types/article'

const userStore = useUserStore()

const articles = ref<ArticleListItem[]>([])
const hotList = ref<HotArticle[]>([])
const loading = ref(true)
const hotLoading = ref(true)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderBy = ref<'latest' | 'hot' | 'oldest'>('latest')

const orderOptions = [
  { value: 'latest' as const, label: '最新' },
  { value: 'hot' as const, label: '最热' },
  { value: 'oldest' as const, label: '最早' }
]

async function fetchArticles() {
  loading.value = true
  error.value = ''
  try {
    const res = await articleService.getList({ page: page.value, pageSize: pageSize.value, orderBy: orderBy.value })
    articles.value = res.records
    total.value = res.total
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function fetchHot() {
  hotLoading.value = true
  try {
    hotList.value = await articleService.getHot(10)
  } catch {
    /* silent */
  } finally {
    hotLoading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchArticles()
  window.scrollTo({ top: 0 })
}

onMounted(() => { fetchArticles(); fetchHot() })
</script>
