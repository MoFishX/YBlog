<template>
  <div class="bg-white rounded-xl border border-zinc-100 p-6 sticky top-20">
    <h3 class="text-base font-bold text-zinc-900 mb-5 font-serif uppercase tracking-wider">热门文章</h3>
    <LoadingSkeleton v-if="loading" :rows="5" />
    <EmptyState v-else-if="list.length === 0" text="暂无数据" />
    <ul v-else class="space-y-1">
      <li
        v-for="item in list"
        :key="item.id"
        class="group cursor-pointer rounded-lg p-2.5 hover:bg-zinc-50 transition-colors duration-200"
        @click="goDetail(item.id)"
      >
        <div class="flex items-center gap-3">
          <div class="min-w-0 flex-1">
            <p class="text-sm font-medium text-zinc-800 truncate group-hover:text-accent transition-colors duration-200">
              {{ item.title }}
            </p>
            <p class="text-xs text-zinc-400 mt-0.5">
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
import { formatNumber } from '@/utils/format'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import type { HotArticle } from '@/types/article'

defineProps<{ list: HotArticle[]; loading: boolean }>()
const router = useRouter()

function goDetail(id: number) {
  router.push(`/article/${id}`)
}
</script>
