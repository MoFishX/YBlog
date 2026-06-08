<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <div class="flex items-center gap-4 mb-8">
      <RouterLink to="/dashboard" class="p-2 text-zinc-500 hover:text-zinc-900 hover:bg-zinc-100 rounded-lg transition-colors duration-200">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </RouterLink>
      <h1 class="text-2xl font-bold text-zinc-900 font-serif">{{ isEdit ? '编辑文章' : '写文章' }}</h1>
    </div>

    <div v-if="loadError" class="bg-red-50 border border-red-200 rounded-xl p-5 text-center mb-6">
      <p class="text-sm text-red-600">{{ loadError }}</p>
    </div>

    <form v-else @submit.prevent="handleSave" class="space-y-6">
      <div>
        <input
          v-model="form.title"
          type="text"
          required
          maxlength="200"
          class="w-full border-none text-2xl font-bold text-zinc-900 placeholder:text-zinc-300 focus:outline-none bg-transparent"
          placeholder="文章标题"
        />
        <div class="text-xs text-zinc-400 mt-1 pl-0.5">{{ form.title.length }}/200</div>
      </div>

      <div>
        <div class="flex items-center gap-3 mb-3">
          <div class="flex bg-zinc-100 rounded-lg p-1">
            <button
              type="button"
              v-for="opt in statusOptions"
              :key="opt.value"
              @click="form.status = opt.value"
              class="px-4 py-1.5 text-xs font-medium rounded-md transition-all duration-200 cursor-pointer"
              :class="form.status === opt.value ? 'bg-white text-zinc-900 shadow-sm' : 'text-zinc-500 hover:text-zinc-700'"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <textarea
          v-model="form.summary"
          maxlength="300"
          rows="2"
          class="w-full border border-zinc-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200 resize-none bg-white"
          placeholder="文章摘要（选填）"
        ></textarea>
        <div class="text-xs text-zinc-400 mt-1 pl-0.5">{{ form.summary.length }}/300</div>
      </div>

      <div>
        <label class="block text-sm font-medium text-zinc-700 mb-2">标签</label>
        <div class="flex flex-wrap gap-2 mb-2">
          <button
            type="button"
            v-for="tag in availableTags"
            :key="tag.id"
            @click="toggleTag(tag.id)"
            class="px-3 py-1.5 text-xs font-medium rounded-lg border transition-all duration-200 cursor-pointer"
            :class="isTagSelected(tag.id)
              ? 'bg-accent/10 border-accent/30 text-accent'
              : 'border-zinc-200 text-zinc-500 hover:border-zinc-300 hover:text-zinc-700'"
          >
            {{ tag.name }}
          </button>
        </div>
        <div v-if="tagsLoading" class="text-xs text-zinc-400">加载标签中...</div>
      </div>

      <div>
        <div class="flex items-center justify-between mb-2">
          <label class="text-sm font-medium text-zinc-700">正文 <span class="text-zinc-400 font-normal">(Markdown)</span></label>
          <span class="text-xs text-zinc-400">{{ form.content.length }} 字</span>
        </div>
        <div class="border border-zinc-200 rounded-lg overflow-hidden focus-within:ring-2 focus-within:ring-accent/20 focus-within:border-accent transition-all duration-200">
          <div class="bg-zinc-50 border-b border-zinc-100 px-4 py-2">
            <span class="text-xs text-zinc-400 font-mono">Markdown</span>
          </div>
          <textarea
            v-model="form.content"
            required
            rows="18"
            class="w-full border-none px-4 py-4 text-sm font-mono leading-relaxed text-zinc-700 placeholder:text-zinc-300 resize-y focus:outline-none bg-white"
            placeholder="# 开始写作..."
          ></textarea>
        </div>
      </div>

      <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-3">
        <p class="text-sm text-red-600">{{ error }}</p>
      </div>

      <div class="flex items-center gap-3 pt-2">
        <button
          type="submit"
          :disabled="submitting || !form.title.trim() || !form.content.trim()"
          class="px-6 py-2.5 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
        >
          {{ submitting ? '保存中...' : isEdit ? '更新文章' : '发布文章' }}
        </button>
        <RouterLink to="/dashboard" class="px-6 py-2.5 text-sm font-medium text-zinc-600 border border-zinc-200 rounded-lg hover:bg-zinc-50 transition-colors duration-200">
          取消
        </RouterLink>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { articleService } from '@/services/articleService'
import { tagService } from '@/services/tagService'
import type { Tag } from '@/types/article'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  content: '',
  summary: '',
  status: 'PUBLISHED' as 'PUBLISHED' | 'DRAFT',
  selectedTagIds: [] as number[]
})

const availableTags = ref<Tag[]>([])
const tagsLoading = ref(false)
const submitting = ref(false)
const error = ref('')
const loadError = ref('')

const statusOptions = [
  { label: '发布', value: 'PUBLISHED' as const },
  { label: '草稿', value: 'DRAFT' as const }
]

function isTagSelected(id: number) {
  return form.selectedTagIds.includes(id)
}

function toggleTag(id: number) {
  const idx = form.selectedTagIds.indexOf(id)
  if (idx > -1) {
    form.selectedTagIds.splice(idx, 1)
  } else {
    form.selectedTagIds.push(id)
  }
}

async function loadTags() {
  tagsLoading.value = true
  try {
    availableTags.value = await tagService.getList()
  } catch {
    /* ignore */
  } finally {
    tagsLoading.value = false
  }
}

async function loadArticle() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const article = await articleService.getDetail(id)
    form.title = article.title
    form.content = article.content
    form.summary = article.summary || ''
    form.status = article.status === 'DRAFT' ? 'DRAFT' : 'PUBLISHED'
    form.selectedTagIds = article.tags?.map(t => t.id) || []
  } catch (e: any) {
    loadError.value = e?.response?.data?.message || '加载文章失败'
  }
}

async function handleSave() {
  if (!form.title.trim() || !form.content.trim()) {
    error.value = '标题和内容不能为空'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    const data = {
      title: form.title.trim(),
      content: form.content,
      summary: form.summary.trim() || undefined,
      status: form.status,
      tagIds: form.selectedTagIds
    }
    if (isEdit.value) {
      await articleService.update(Number(route.params.id), data)
    } else {
      await articleService.create(data)
    }
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e?.response?.data?.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTags()
  if (isEdit.value) loadArticle()
})
</script>
