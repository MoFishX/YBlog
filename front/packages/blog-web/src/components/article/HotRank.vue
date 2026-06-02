<template>
  <div class="bg-white rounded-lg border border-gray-200 shadow-sm p-5">
    <h3 class="text-lg font-semibold text-gray-900 mb-4">热门排行</h3>
    <LoadingSkeleton v-if="loading" :rows="5" />
    <EmptyState v-else-if="list.length === 0" text="暂无数据" />
    <ul v-else class="space-y-3">
      <li v-for="item in list" :key="item.id" class="cursor-pointer hover:bg-gray-50 rounded p-2 -mx-2 transition-colors" @click="goDetail(item.id)">
        <div class="flex items-center gap-3">
          <span
            class="w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold text-white flex-shrink-0"
            :class="item.rank <= 3 ? 'bg-red-500' : 'bg-gray-400'"
          >
            {{ item.rank }}
          </span>
          <div class="min-w-0 flex-1">
            <p class="text-sm text-gray-800 truncate">{{ item.title }}</p>
            <p class="text-xs text-gray-400 mt-0.5">
              {{ formatNumber(item.viewCount) }} 阅读 &middot; {{ formatNumber(item.likeCount) }} 点赞
            </p>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { formatNumber } from '@/utils/storage'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import type { HotArticle } from '@shared/types/article'

defineProps<{
  list: HotArticle[]
  loading: boolean
}>()

const router = useRouter()

function goDetail(id: number) {
  router.push({ name: 'ArticleDetail', params: { id } })
}
</script>
