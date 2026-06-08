<template>
  <div class="container mx-auto px-4 py-10 max-w-2xl">
    <h1 class="text-2xl font-bold text-zinc-900 mb-2 font-serif">搜索文章</h1>
    <p v-if="keyword" class="text-sm text-zinc-500 mb-6">关键词："{{ keyword }}"</p>

    <div class="mb-10">
      <div class="flex gap-3">
        <div class="flex-1 relative">
          <svg class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
          <input
            v-model="searchText" @keyup.enter="search" type="text"
            class="w-full border border-zinc-200 rounded-lg pl-10 pr-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200"
            placeholder="输入关键词搜索文章..."
          />
        </div>
        <button
          :disabled="!searchText.trim() || searching" @click="search"
          class="px-6 py-3 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
        >
          {{ searching ? '搜索中...' : '搜索' }}
        </button>
      </div>
    </div>

    <div v-if="error" class="bg-red-50 border border-red-200 rounded-xl p-5 text-center mb-6">
      <p class="text-sm text-red-600 mb-3">{{ error }}</p>
      <button @click="search" class="text-sm font-medium text-red-600 underline cursor-pointer">重试</button>
    </div>

    <LoadingSkeleton v-if="searching" :rows="5" />
    <EmptyState v-else-if="searched && results.length === 0" text="未找到相关文章" />

    <div v-else-if="searched" class="space-y-4">
      <p class="text-xs text-zinc-400 pl-1 font-medium">共 {{ total }} 条结果</p>

      <article
        v-for="item in results" :key="item.id"
        class="bg-white border border-zinc-100 rounded-xl p-6 hover:border-accent/30 hover:shadow-sm transition-all duration-200 cursor-pointer"
        @click="goDetail(item.id)"
      >
        <h3 class="text-lg font-bold text-zinc-900 mb-2 font-serif group-hover:text-accent transition-colors duration-200">{{ item.title }}</h3>
        <p class="text-sm text-zinc-500 mb-4 line-clamp-2 leading-relaxed">{{ item.summary }}</p>
        <div class="flex items-center justify-between text-xs text-zinc-400">
          <div class="flex items-center gap-3">
            <span class="font-medium">{{ item.author.username }}</span>
            <span>{{ formatNumber(item.viewCount) }} 阅读</span>
          </div>
          <span>{{ formatDate(item.createdAt) }}</span>
        </div>
      </article>

      <Pagination v-if="total > pageSize" :current-page="page" :total="total" :page-size="pageSize" @change="handlePageChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/format'
import { articleService } from '@/services/articleService'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { ArticleListItem } from '@/types/article'

const props = defineProps<{ keyword: string }>()
const router = useRouter()

const searchText = ref(props.keyword || '')
const results = ref<ArticleListItem[]>([])
const searching = ref(false)
const searched = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function doSearch() {
  const kw = searchText.value.trim()
  if (!kw) return
  searching.value = true; searched.value = true; error.value = ''
  try {
    const res = await articleService.search(kw, page.value, pageSize.value)
    results.value = res.records; total.value = res.total
  } catch (e: any) { error.value = e?.response?.data?.message || '搜索失败' }
  finally { searching.value = false }
}

function search() { page.value = 1; doSearch() }
function handlePageChange(p: number) { page.value = p; doSearch(); window.scrollTo({ top: 0 }) }
function goDetail(id: number) { router.push(`/article/${id}`) }

onMounted(() => { if (props.keyword) doSearch() })
</script>
