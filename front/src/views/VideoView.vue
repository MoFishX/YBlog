<template>
  <div class="min-h-screen bg-[#0f1115]">
    <div class="max-w-[1400px] mx-auto px-6 py-8">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-6">
          <h1 class="text-xl font-bold text-white tracking-tight">视频中心</h1>
          <nav class="flex items-center gap-1 bg-[#1a1d24] rounded-lg p-1">
            <button
              v-for="cat in categories"
              :key="cat.value"
              @click="activeCategory = cat.value"
              class="px-4 py-1.5 text-sm rounded-md transition-all duration-200"
              :class="activeCategory === cat.value
                ? 'bg-white/10 text-white'
                : 'text-gray-400 hover:text-gray-200'"
            >
              {{ cat.label }}
            </button>
          </nav>
        </div>
        <div class="flex items-center gap-3">
          <button class="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition-colors">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
            播放全部
          </button>
          <button class="px-4 py-2 rounded-lg border border-white/10 text-gray-300 hover:text-white hover:border-white/20 text-sm transition-colors">
            查看更多
          </button>
        </div>
      </div>

      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
        <div v-for="i in 8" :key="i" class="animate-pulse">
          <div class="aspect-video rounded-xl bg-[#1a1d24] mb-3"></div>
          <div class="h-4 bg-[#1a1d24] rounded w-3/4 mb-2"></div>
          <div class="h-3 bg-[#1a1d24] rounded w-1/3"></div>
        </div>
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
        <VideoCard
          v-for="video in videos"
          :key="video.id"
          :video="video"
          @click="handleClick(video.id)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import VideoCard from '@/components/video/VideoCard.vue'
import type { VideoItem } from '@/components/video/VideoCard.vue'

const activeCategory = ref('latest')
const loading = ref(false)

const categories = [
  { label: '最新发布', value: 'latest' },
  { label: '最多播放', value: 'mostViewed' },
  { label: '最多收藏', value: 'mostFavorited' }
]

const videos = ref<VideoItem[]>([
  { id: 1, title: '深入理解 Vue 3 Composition API 的设计哲学与实践', thumbnail: 'https://picsum.photos/seed/v1/640/360', duration: '24:18', viewCount: 128000, commentCount: 342, publishedAt: '3天前' },
  { id: 2, title: 'Rust 与 WebAssembly 在前端性能优化中的应用', thumbnail: 'https://picsum.photos/seed/v2/640/360', duration: '18:45', viewCount: 89600, commentCount: 215, publishedAt: '1周前' },
  { id: 3, title: '从零构建一个 TypeScript 全栈项目', thumbnail: 'https://picsum.photos/seed/v3/640/360', duration: '1:02:30', viewCount: 256000, commentCount: 891, publishedAt: '2周前' },
  { id: 4, title: 'React Server Components 深度解析', thumbnail: 'https://picsum.photos/seed/v4/640/360', duration: '32:10', viewCount: 178000, commentCount: 567, publishedAt: '5天前' },
  { id: 5, title: '现代 CSS 布局技巧：超越 Flexbox 和 Grid', thumbnail: 'https://picsum.photos/seed/v5/640/360', duration: '15:22', viewCount: 65400, commentCount: 189, publishedAt: '3天前' },
  { id: 6, title: 'Node.js 微服务架构设计与部署实战', thumbnail: 'https://picsum.photos/seed/v6/640/360', duration: '45:08', viewCount: 312000, commentCount: 1203, publishedAt: '1个月前' },
  { id: 7, title: 'AI 辅助编程：Copilot 与 Cursor 的高效使用指南', thumbnail: 'https://picsum.photos/seed/v7/640/360', duration: '28:55', viewCount: 489000, commentCount: 2340, publishedAt: '6天前' },
  { id: 8, title: '数据库性能调优：从慢查询到毫秒级响应', thumbnail: 'https://picsum.photos/seed/v8/640/360', duration: '38:12', viewCount: 145000, commentCount: 456, publishedAt: '2周前' },
])

function handleClick(id: number) {
  // placeholder
}
</script>
