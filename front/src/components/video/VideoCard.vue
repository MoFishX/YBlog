<template>
  <article
    class="group cursor-pointer rounded-xl overflow-hidden bg-zinc-900 border border-zinc-800 hover:border-zinc-700 hover:bg-zinc-800 transition-all duration-300"
    @click="$emit('click')"
  >
    <div class="relative aspect-video overflow-hidden bg-zinc-950">
      <img
        v-if="video.thumbnail"
        :src="video.thumbnail"
        :alt="video.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        loading="lazy"
      />
      <div v-else class="w-full h-full flex items-center justify-center">
        <svg class="w-10 h-10 text-zinc-700" fill="currentColor" viewBox="0 0 24 24">
          <path d="M8 5v14l11-7z"/>
        </svg>
      </div>

      <div class="absolute bottom-2 left-2 flex items-center gap-2">
        <span class="flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] bg-black/70 text-zinc-300 backdrop-blur-sm">
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
          </svg>
          {{ formatCount(video.viewCount) }}
        </span>
        <span class="flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] bg-black/70 text-zinc-300 backdrop-blur-sm">
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
          </svg>
          {{ formatCount(video.commentCount) }}
        </span>
      </div>

      <span class="absolute bottom-2 right-2 px-1.5 py-0.5 rounded text-[11px] bg-black/80 text-white font-mono backdrop-blur-sm">
        {{ video.duration }}
      </span>
    </div>

    <div class="p-3.5">
      <h3 class="text-sm font-medium text-zinc-300 leading-snug line-clamp-2 group-hover:text-white transition-colors duration-200 mb-2">
        {{ video.title }}
      </h3>
      <p class="text-xs text-zinc-500">{{ video.publishedAt }}</p>
    </div>
  </article>
</template>

<script setup lang="ts">
import { formatNumber } from '@/utils/format'

export interface VideoItem {
  id: number
  title: string
  thumbnail?: string
  duration: string
  viewCount: number
  commentCount: number
  publishedAt: string
}

defineProps<{ video: VideoItem }>()
defineEmits<{ click: [] }>()

function formatCount(n: number): string {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
</script>
