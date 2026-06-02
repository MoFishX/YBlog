<template>
  <article class="bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow p-5 cursor-pointer" @click="goDetail">
    <div class="flex items-start gap-3 mb-3">
      <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-sm font-medium flex-shrink-0">
        {{ article.author.username.charAt(0).toUpperCase() }}
      </div>
      <div class="min-w-0 flex-1">
        <div class="flex items-center gap-2 mb-1">
          <span class="text-sm text-gray-500">{{ article.author.username }}</span>
          <span class="text-xs text-gray-400">{{ formatDate(article.createdAt) }}</span>
        </div>
      </div>
    </div>

    <h3 class="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
      {{ article.title }}
    </h3>
    <p class="text-sm text-gray-600 mb-4 line-clamp-3">
      {{ article.summary }}
    </p>

    <div class="flex items-center justify-between text-xs text-gray-400">
      <div class="flex items-center gap-3">
        <span class="flex items-center gap-1">
          <span>&#128065;</span> {{ formatNumber(article.viewCount) }}
        </span>
        <span class="flex items-center gap-1">
          <span>&#10084;</span> {{ formatNumber(article.likeCount) }}
        </span>
        <span class="flex items-center gap-1">
          <span>&#128172;</span> {{ formatNumber(article.commentCount) }}
        </span>
      </div>
      <div class="flex gap-1" v-if="article.tags?.length">
        <span
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id"
          class="px-2 py-0.5 bg-blue-50 text-blue-600 rounded text-xs"
        >
          {{ tag.name }}
        </span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/storage'
import type { ArticleListItem } from '@shared/types/article'

const props = defineProps<{
  article: ArticleListItem
}>()

const router = useRouter()

function goDetail() {
  router.push({ name: 'ArticleDetail', params: { id: props.article.id } })
}
</script>
