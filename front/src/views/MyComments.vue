<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <div class="flex items-center gap-3 mb-8">
      <RouterLink :to="`/user/${userStore.user?.id}`" class="p-1.5 -ml-1.5 text-zinc-400 hover:text-zinc-600 hover:bg-zinc-100 rounded-lg transition-colors duration-200">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </RouterLink>
      <h1 class="text-2xl font-bold text-zinc-900 font-serif">我的评论</h1>
    </div>

    <div class="flex items-center gap-3 mb-6">
      <div class="flex bg-zinc-100 rounded-lg p-1">
        <button
          v-for="tab in tabs" :key="tab.key"
          @click="switchTab(tab.key)"
          class="px-4 py-1.5 text-xs font-medium rounded-md transition-all duration-200 cursor-pointer"
          :class="activeTab === tab.key ? 'bg-white text-zinc-900 shadow-sm' : 'text-zinc-500 hover:text-zinc-700'"
        >
          {{ tab.label }}
        </button>
      </div>
      <span v-if="!loading" class="text-xs text-zinc-400">{{ total }} 条</span>
    </div>

    <LoadingSkeleton v-if="loading" :rows="4" />

    <div v-else-if="list.length === 0" class="bg-white rounded-xl border border-zinc-100 p-12 text-center">
      <svg class="w-16 h-16 text-zinc-200 mx-auto mb-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.102 15.328 3 14.644 3 14c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
      <p class="text-sm text-zinc-500">{{ emptyText }}</p>
    </div>

    <div v-else class="space-y-4">
      <article
        v-for="item in list" :key="item.id"
        class="group bg-white rounded-xl border border-zinc-100 hover:border-zinc-200 hover:shadow-sm transition-all duration-200 p-6"
      >
        <div class="flex items-center gap-2 mb-1.5">
          <RouterLink :to="`/article/${item.articleId}#comment-${item.id}`" class="text-sm font-semibold text-zinc-900 group-hover:text-accent transition-colors duration-200 truncate">
            {{ item.articleTitle }}
          </RouterLink>
          <span v-if="item.status === 'HIDDEN'" class="inline-flex items-center px-1.5 py-0.5 rounded text-[11px] font-semibold bg-amber-50 text-amber-600 flex-shrink-0">已隐藏</span>
        </div>

        <div v-if="item.replyTo" class="text-xs text-zinc-400 mb-1">回复 @{{ item.replyTo.username }}</div>

        <p class="text-sm text-zinc-600 leading-relaxed line-clamp-2 mb-3">{{ item.content }}</p>

        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4 text-xs text-zinc-400">
            <span>{{ formatDateTime(item.createdAt) }}</span>
          </div>
          <button
            @click="handleDelete(item.id)"
            class="px-2.5 py-1 text-xs font-medium text-zinc-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors duration-200 cursor-pointer"
          >
            删除
          </button>
        </div>
      </article>

      <Pagination v-if="total > pageSize" :current-page="page" :total="total" :page-size="pageSize" @change="handlePageChange" />
    </div>

    <Teleport to="body">
      <div
        v-if="deleteTarget !== null"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 backdrop-blur-sm"
        @click.self="deleteTarget = null"
      >
        <div class="bg-white rounded-2xl p-6 max-w-sm w-full mx-4 shadow-xl">
          <h3 class="text-lg font-bold text-zinc-900 mb-2 font-serif">确认删除</h3>
          <p class="text-sm text-zinc-500 mb-6">确定要删除这条评论吗？此操作不可撤销。</p>
          <div class="flex gap-3 justify-end">
            <button @click="deleteTarget = null" class="px-4 py-2 text-sm font-medium text-zinc-600 border border-zinc-200 rounded-lg hover:bg-zinc-50 transition-colors duration-200 cursor-pointer">取消</button>
            <button :disabled="deleting" @click="confirmDelete" class="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-50 transition-colors duration-200 cursor-pointer">{{ deleting ? '删除中...' : '确认删除' }}</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { commentService } from '@/services/commentService'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { UserComment } from '@/types/comment'

type TabKey = 'mine' | 'replies'

const userStore = useUserStore()

const tabs: { key: TabKey; label: string }[] = [
  { key: 'mine', label: '我的评论' },
  { key: 'replies', label: '回复我的' }
]

const activeTab = ref<TabKey>('mine')
const list = ref<UserComment[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const deleteTarget = ref<number | null>(null)
const deleting = ref(false)

const emptyText = computed(() => activeTab.value === 'mine' ? '你还没有发表过评论' : '还没有人回复你的评论')

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    const res = activeTab.value === 'mine'
      ? await commentService.getMyComments(params)
      : await commentService.getReplies({ ...params, unreadOnly: 0 })
    list.value = res.records
    total.value = res.total
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function switchTab(key: TabKey) {
  activeTab.value = key
  page.value = 1
  fetchData()
}

function handlePageChange(p: number) {
  page.value = p
  fetchData()
  window.scrollTo({ top: 0 })
}

async function handleDelete(id: number) {
  deleteTarget.value = id
}

async function confirmDelete() {
  if (deleteTarget.value === null) return
  deleting.value = true
  try {
    await commentService.delete(deleteTarget.value)
    deleteTarget.value = null
    await fetchData()
  } catch {
    // silent
  } finally {
    deleting.value = false
  }
}

onMounted(() => fetchData())
</script>