<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-zinc-900 font-serif">创作中心</h1>
        <p class="text-sm text-zinc-500 mt-1">管理你的文章</p>
      </div>
      <RouterLink to="/dashboard/write" class="inline-flex items-center gap-1.5 px-5 py-2.5 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 transition-colors duration-200">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
        写文章
      </RouterLink>
    </div>

    <div class="flex bg-zinc-100 rounded-lg p-1 mb-6 w-fit">
      <button
        v-for="opt in statusTabs"
        :key="opt.value"
        @click="statusFilter = opt.value; page = 1; fetchArticles()"
        class="px-4 py-1.5 text-xs font-medium rounded-md transition-all duration-200 cursor-pointer"
        :class="statusFilter === opt.value ? 'bg-white text-zinc-900 shadow-sm' : 'text-zinc-500 hover:text-zinc-700'"
      >
        {{ opt.label }}
      </button>
    </div>

    <div v-if="error" class="bg-red-50 border border-red-200 rounded-xl p-5 text-center mb-6">
      <p class="text-sm text-red-600 mb-3">{{ error }}</p>
      <button @click="fetchArticles" class="text-sm font-medium text-red-600 underline cursor-pointer">重试</button>
    </div>

    <LoadingSkeleton v-if="loading" :rows="4" />
    <EmptyState v-else-if="articles.length === 0" :text="statusFilter === 'DRAFT' ? '暂无草稿' : '暂无文章，快去写一篇吧'" />

    <div v-else class="space-y-3">
      <article
        v-for="item in articles"
        :key="item.id"
        class="group bg-white rounded-xl border border-zinc-100 hover:border-zinc-200 transition-all duration-200 overflow-hidden"
      >
        <div class="p-5">
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2 mb-1.5">
                <h3 class="text-base font-bold text-zinc-900 group-hover:text-accent transition-colors duration-200 truncate">
                  {{ item.title }}
                </h3>
                <span
                  class="inline-flex items-center px-1.5 py-0.5 rounded text-[11px] font-semibold flex-shrink-0"
                  :class="item.status === 'PUBLISHED' ? 'bg-emerald-50 text-emerald-700' : 'bg-zinc-100 text-zinc-500'"
                >
                  {{ item.status === 'PUBLISHED' ? '已发布' : '草稿' }}
                </span>
              </div>
              <p class="text-sm text-zinc-400 line-clamp-1 mb-2">{{ item.summary || '暂无简介' }}</p>
              <div class="flex items-center gap-4 text-xs text-zinc-400">
                <span class="flex items-center gap-1">{{ formatDate(item.createdAt) }}</span>
                <span class="flex items-center gap-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  {{ formatNumber(item.viewCount) }}
                </span>
                <span class="flex items-center gap-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
                  {{ formatNumber(item.likeCount) }}
                </span>
              </div>
            </div>
            <div class="flex items-center gap-1 flex-shrink-0">
              <RouterLink
                :to="`/dashboard/write/${item.id}`"
                class="px-3 py-1.5 text-xs font-medium text-zinc-500 hover:text-zinc-900 hover:bg-zinc-100 rounded-lg transition-colors duration-200"
              >
                编辑
              </RouterLink>
              <button
                @click="confirmDelete(item)"
                class="px-3 py-1.5 text-xs font-medium text-zinc-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors duration-200 cursor-pointer"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </article>

      <Pagination v-if="total > pageSize" :current-page="page" :total="total" :page-size="pageSize" @change="handlePageChange" />
    </div>

    <Teleport to="body">
      <div
        v-if="deleteTarget"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 backdrop-blur-sm"
        @click.self="deleteTarget = null"
      >
        <div class="bg-white rounded-2xl p-6 max-w-sm w-full mx-4 shadow-xl">
          <h3 class="text-lg font-bold text-zinc-900 mb-2 font-serif">确认删除</h3>
          <p class="text-sm text-zinc-500 mb-6">确定要删除「{{ deleteTarget.title }}」吗？此操作不可撤销。</p>
          <div class="flex gap-3 justify-end">
            <button
              @click="deleteTarget = null"
              class="px-4 py-2 text-sm font-medium text-zinc-600 border border-zinc-200 rounded-lg hover:bg-zinc-50 transition-colors duration-200 cursor-pointer"
            >
              取消
            </button>
            <button
              :disabled="deleting"
              @click="handleDelete"
              class="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
            >
              {{ deleting ? '删除中...' : '确认删除' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/format'
import { articleService } from '@/services/articleService'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { ArticleListItem } from '@/types/article'

const articles = ref<ArticleListItem[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

const deleteTarget = ref<ArticleListItem | null>(null)
const deleting = ref(false)

const statusTabs = [
  { label: '全部', value: '' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '草稿', value: 'DRAFT' }
]

async function fetchArticles() {
  loading.value = true
  error.value = ''
  try {
    const params: { page: number; pageSize: number; status?: string } = {
      page: page.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await articleService.getMine(params)
    articles.value = res.records
    total.value = res.total
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchArticles()
  window.scrollTo({ top: 0 })
}

function confirmDelete(item: ArticleListItem) {
  deleteTarget.value = item
}

async function handleDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await articleService.delete(deleteTarget.value.id)
    deleteTarget.value = null
    fetchArticles()
  } catch {
    /* ignore */
  } finally {
    deleting.value = false
  }
}

onMounted(() => fetchArticles())
</script>
