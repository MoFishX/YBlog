<template>
  <div class="min-h-screen bg-gray-50">
    <div class="container mx-auto px-4 py-12 max-w-4xl">
      <div class="text-center mb-12">
        <h1 class="text-3xl font-bold text-gray-900 mb-3">探索标签</h1>
        <p class="text-gray-500 text-sm">共 {{ tags.length }} 个标签，点击浏览相关文章</p>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="flex gap-2">
          <span v-for="i in 3" :key="i" class="w-3 h-3 rounded-full bg-purple-400 animate-bounce" :style="{ animationDelay: `${i * 0.15}s` }"></span>
        </div>
      </div>

      <div v-else-if="error" class="text-center py-20">
        <p class="text-gray-400 mb-4">{{ error }}</p>
        <button @click="fetchTags" class="text-purple-600 text-sm hover:underline">重试</button>
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
          @click="goToTag(tag.id)"
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
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
  'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)',
  'linear-gradient(135deg, #f5576c 0%, #ff6f91 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
]

function getFontSize(count: number): number {
  const min = 16
  const max = 36
  const maxCount = Math.max(...tags.value.map(t => t.articleCount || 0), 1)
  if (maxCount <= 1) return 20
  return min + ((count / maxCount) * (max - min))
}

function goToTag(tagId: number) {
  router.push({ name: 'Home', query: { tagId: tagId.toString() } })
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
  color: #4a5568;
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
