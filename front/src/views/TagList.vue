<template>
  <div class="min-h-screen bg-zinc-50">
    <div class="container mx-auto px-4 py-14 max-w-4xl">
      <div class="text-center mb-14">
        <h1 class="text-3xl font-bold text-zinc-900 mb-3 font-serif">探索标签</h1>
        <p class="text-zinc-500 text-sm">共 {{ tags.length }} 个标签，点击浏览相关文章</p>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="flex gap-2">
          <span v-for="i in 3" :key="i" class="w-3 h-3 rounded-full bg-accent opacity-60 animate-bounce" :style="{ animationDelay: `${i * 0.15}s` }"></span>
        </div>
      </div>

      <div v-else-if="error" class="text-center py-20">
        <p class="text-zinc-400 mb-4">{{ error }}</p>
        <button @click="fetchTags" class="text-accent text-sm font-medium hover:underline cursor-pointer">重试</button>
      </div>

      <div v-else class="flex flex-wrap justify-center gap-4">
        <div
          v-for="(tag, index) in tags"
          :key="tag.id"
          class="tag-bubble group cursor-pointer select-none"
          :style="{
            fontSize: `${getFontSize(tag.articleCount || 0)}px`,
            background: gradients[index % gradients.length],
            animationDelay: `${index * 0.05}s`,
          }"
          @click="goToTag(tag.name)"
        >
          <span class="relative z-10">{{ tag.name }}</span>
          <span class="tag-count-badge">{{ tag.articleCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { tagService } from '@/services/tagService'
import type { Tag } from '@/types/article'

const router = useRouter()

const tags = ref<Tag[]>([])
const loading = ref(true)
const error = ref('')

const gradients = [
  'linear-gradient(135deg, #EC4899 0%, #BE185D 100%)',
  'linear-gradient(135deg, #ec4899cc 0%, #f472b6 100%)',
  'linear-gradient(135deg, #3F3F46 0%, #18181B 100%)',
  'linear-gradient(135deg, #52525B 0%, #27272A 100%)',
  'linear-gradient(135deg, #71717A 0%, #EC4899 100%)',
  'linear-gradient(135deg, #3F3F46 0%, #ec4899cc 100%)',
  'linear-gradient(135deg, #18181B 0%, #EC4899 100%)',
  'linear-gradient(135deg, #f472b6 0%, #BE185D 100%)',
  'linear-gradient(135deg, #52525B 0%, #EC4899 100%)',
  'linear-gradient(135deg, #27272A 0%, #3F3F46 100%)',
]

function getFontSize(count: number): number {
  const min = 16
  const max = 36
  const maxCount = Math.max(...tags.value.map(t => t.articleCount || 0), 1)
  if (maxCount <= 1) return 20
  return min + ((count / maxCount) * (max - min))
}

function goToTag(name: string) {
  router.push({ name: 'Home', query: { tagName: name } })
}

async function fetchTags() {
  loading.value = true
  error.value = ''
  try {
    tags.value = await tagService.getList()
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchTags())
</script>

<style scoped>
.tag-bubble {
  position: relative;
  padding: 12px 28px;
  border-radius: 40px;
  color: #fff;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.5s ease backwards;
  overflow: visible;
}

.tag-bubble:hover {
  transform: translateY(-4px) scale(1.08);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.tag-bubble:active {
  transform: translateY(-1px) scale(1.03);
}

.tag-count-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: rgba(255, 255, 255, 0.95);
  color: #3F3F46;
  font-size: 11px;
  font-weight: 700;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
