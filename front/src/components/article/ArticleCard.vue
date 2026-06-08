<template>
  <article
    class="group bg-white rounded-xl border border-zinc-100 hover:border-accent/30 hover:shadow-md transition-all duration-200 cursor-pointer overflow-hidden"
    @click="goDetail"
  >
    <div v-if="article.coverImage" class="aspect-[16/9] overflow-hidden bg-zinc-100">
      <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-[1.02]" loading="lazy" />
    </div>

    <div class="p-6">
      <div class="flex items-center gap-2 mb-3">
        <span class="w-7 h-7 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-500 text-xs font-semibold flex-shrink-0 overflow-hidden">
          <img v-if="article.author.avatar" :src="article.author.avatar" class="w-full h-full object-cover" />
          <template v-else>{{ article.author.username.charAt(0).toUpperCase() }}</template>
        </span>
        <span class="text-sm font-medium text-zinc-500 truncate">{{ article.author.username }}</span>
        <span class="text-zinc-300">&middot;</span>
        <span class="text-xs text-zinc-400 whitespace-nowrap">{{ formatDate(article.createdAt) }}</span>
      </div>

      <h3 class="text-lg font-bold text-zinc-900 mb-2 line-clamp-2 group-hover:text-accent transition-colors duration-200 font-serif">
        {{ article.title }}
      </h3>

      <p class="text-sm text-zinc-500 mb-4 line-clamp-2 leading-relaxed">
        {{ article.summary || '暂无简介' }}
      </p>

      <div class="flex items-center justify-between gap-2">
        <div class="flex items-center gap-4 text-xs text-zinc-400">
          <span class="flex items-center gap-1">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
            {{ formatNumber(article.viewCount) }}
          </span>
          <span class="flex items-center gap-1">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
            {{ formatNumber(article.likeCount) }}
          </span>
          <span class="flex items-center gap-1">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
            {{ formatNumber(article.commentCount) }}
          </span>
        </div>
        <div v-if="article.tags?.length" class="flex gap-1.5">
          <span v-for="tag in article.tags.slice(0, 2)" :key="tag.id" class="px-2.5 py-0.5 text-xs font-medium text-zinc-500 bg-zinc-100 rounded-md">
            {{ tag.name }}
          </span>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { formatDate, formatNumber } from '@/utils/format'
import type { ArticleListItem } from '@/types/article'

const props = defineProps<{ article: ArticleListItem }>()
const router = useRouter()

function goDetail() {
  router.push(`/article/${props.article.id}`)
}
</script>
