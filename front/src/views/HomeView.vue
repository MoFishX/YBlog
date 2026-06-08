<template>
  <div>
    <section class="bg-white border-b border-zinc-100">
      <div class="container mx-auto px-4 py-20 md:py-28 text-center">
        <h1 class="text-4xl md:text-5xl lg:text-6xl font-bold text-zinc-900 mb-4 tracking-tight font-serif">
          探索技术的无限可能
        </h1>
        <p class="text-lg text-zinc-500 mb-10 max-w-xl mx-auto leading-relaxed">
          在这里发现关于前端、后端、架构与设计的前沿思考与实践
        </p>
        <div class="flex items-center justify-center gap-4">
          <RouterLink to="/search" class="px-6 py-3 text-sm font-semibold text-white bg-zinc-900 rounded-lg hover:bg-zinc-800 transition-colors duration-200 cursor-pointer">
            探索文章
          </RouterLink>
          <RouterLink v-if="!userStore.isLoggedIn" to="/register" class="px-6 py-3 text-sm font-semibold text-zinc-600 border border-zinc-200 rounded-lg hover:bg-zinc-50 hover:border-zinc-300 transition-all duration-200 cursor-pointer">
            加入我们
          </RouterLink>
        </div>
      </div>
    </section>

    <div class="container mx-auto px-4 py-10">
      <div class="lg:grid lg:grid-cols-[1fr_320px] lg:gap-10">
        <div>
          <div class="flex items-center justify-between mb-8">
            <div>
              <h2 class="text-xl font-bold text-zinc-900 font-serif">
                <template v-if="tagFilter">#{{ tagFilter }}</template>
                <template v-else>最新文章</template>
              </h2>
            </div>
            <div class="flex bg-zinc-100 rounded-lg p-1">
              <button
                v-for="opt in orderOptions"
                :key="opt.value"
                @click="orderBy = opt.value; fetchArticles()"
                class="px-4 py-1.5 text-xs font-medium rounded-md transition-all duration-200 cursor-pointer"
                :class="orderBy === opt.value ? 'bg-white text-zinc-900 shadow-sm' : 'text-zinc-500 hover:text-zinc-700'"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>

          <div v-if="error" class="bg-red-50 border border-red-200 rounded-xl p-5 text-center mb-8">
            <p class="text-sm text-red-600 mb-3">{{ error }}</p>
            <button @click="fetchArticles" class="text-sm font-medium text-red-600 underline cursor-pointer">重试</button>
          </div>

          <ArticleList :articles="articles" :loading="loading" />

          <Pagination v-if="total > pageSize" :current-page="page" :total="total" :page-size="pageSize" @change="handlePageChange" />
        </div>

        <aside class="mt-10 lg:mt-0">
          <HotRank :list="hotList" :loading="hotLoading" />
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import ArticleList from '@/components/article/ArticleList.vue'
import HotRank from '@/components/article/HotRank.vue'
import Pagination from '@/components/common/Pagination.vue'
import { articleService } from '@/services/articleService'
import type { ArticleListItem, HotArticle } from '@/types/article'

const userStore = useUserStore()
const route = useRoute()

const articles = ref<ArticleListItem[]>([])
const hotList = ref<HotArticle[]>([])
const loading = ref(true)
const hotLoading = ref(true)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderBy = ref<'latest' | 'hot' | 'oldest'>('latest')
const tagFilter = ref('')

const orderOptions = [
  { value: 'latest' as const, label: '最新' },
  { value: 'hot' as const, label: '最热' },
  { value: 'oldest' as const, label: '最早' }
]

async function fetchArticles() {
  loading.value = true
  error.value = ''
  try {
    const tagName = route.query.tagName as string | undefined
    const res = await articleService.getList({ page: page.value, pageSize: pageSize.value, orderBy: orderBy.value, tagName })
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

function syncTagFilter() {
  tagFilter.value = (route.query.tagName as string) || ''
}

onMounted(() => { syncTagFilter(); fetchArticles(); fetchHot() })

watch(() => route.query.tagName, () => {
  page.value = 1
  syncTagFilter()
  fetchArticles()
})

watch(() => route.path, (to) => {
  if (to === '/') { page.value = 1; syncTagFilter(); fetchArticles(); fetchHot() }
})
</script>
