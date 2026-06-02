<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">搜索结果</h1>
    <p v-if="keyword" class="text-sm text-gray-500 mb-6">关键词：&quot;{{ keyword }}&quot;</p>

    <div class="mb-6">
      <div class="flex gap-2">
        <input
          v-model="searchText"
          @keyup.enter="search"
          type="text"
          class="flex-1 border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="输入关键词搜索文章..."
        />
        <button
          :disabled="!searchText.trim() || searching"
          @click="search"
          class="px-6 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          搜索
        </button>
      </div>
    </div>

    <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
      <p class="text-sm text-red-600">{{ error }}</p>
      <button @click="search" class="mt-2 text-sm text-red-600 underline">重试</button>
    </div>

    <LoadingSkeleton v-if="searching" :rows="5" />
    <EmptyState v-else-if="searched && results.length === 0" text="未找到相关文章" />

    <div v-else-if="searched" class="space-y-4">
      <p class="text-xs text-gray-400" v-if="took != null">搜索耗时 {{ took }}ms，共 {{ total }} 条结果</p>

      <article
        v-for="item in results"
        :key="item.id"
        class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-shadow cursor-pointer"
        @click="goDetail(item.id)"
      >
        <h3 class="text-lg font-semibold text-gray-900 mb-2">
          <span v-if="item.titleHighlight" v-html="item.titleHighlight"></span>
          <span v-else>{{ item.title }}</span>
        </h3>
        <p class="text-sm text-gray-600 mb-3 line-clamp-2">{{ item.summary }}</p>
        <div class="flex items-center justify-between text-xs text-gray-400">
          <div class="flex items-center gap-3">
            <span>{{ item.author.username }}</span>
            <span>&#128065; {{ formatNumber(item.viewCount) }}</span>
            <span>&#10084; {{ formatNumber(item.likeCount) }}</span>
          </div>
          <span>{{ formatDate(item.createdAt) }}</span>
        </div>
      </article>

      <Pagination
        v-if="total > pageSize"
        :current-page="page"
        :total="total"
        :page-size="pageSize"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/storage'
import { articleService } from '@/services/articleService'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { SearchArticle } from '@shared/types/article'

const route = useRoute()
const router = useRouter()

const keyword = ref((route.query.q as string) || '')
const searchText = ref(keyword.value)
const results = ref<SearchArticle[]>([])
const searching = ref(false)
const searched = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const took = ref<number | null>(null)

async function doSearch() {
  searching.value = true
  searched.value = true
  error.value = ''
  try {
    const res = await articleService.search(keyword.value, page.value, pageSize.value)
    results.value = res.records
    total.value = res.total
    took.value = res.took
  } catch (e: any) {
    error.value = e?.response?.data?.message || '搜索失败'
  } finally {
    searching.value = false
  }
}

function search() {
  keyword.value = searchText.value.trim()
  if (!keyword.value) return
  page.value = 1
  router.replace({ query: { q: keyword.value } })
  doSearch()
}

function handlePageChange(p: number) {
  page.value = p
  doSearch()
}

function goDetail(id: number) {
  router.push({ name: 'ArticleDetail', params: { id } })
}

onMounted(() => {
  if (keyword.value) {
    doSearch()
  }
})
</script>
