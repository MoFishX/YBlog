<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <div class="flex items-center gap-3 mb-8">
      <RouterLink to="/dashboard" class="p-1.5 -ml-1.5 text-zinc-400 hover:text-zinc-600 hover:bg-zinc-100 rounded-lg transition-colors duration-200">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </RouterLink>
      <h1 class="text-2xl font-bold text-zinc-900 font-serif">{{ isEdit ? '编辑文章' : '写文章' }}</h1>
    </div>

    <div v-if="loadError" class="bg-red-50 border border-red-200 rounded-xl p-6 mb-6">
      <p class="text-sm text-red-600">{{ loadError }}</p>
    </div>

    <form v-else @submit.prevent="handleSave" class="space-y-8">
      <div>
        <input
          v-model="form.title"
          type="text"
          required
           maxlength="50"
           class="w-full border-none text-3xl font-bold text-zinc-900 placeholder:text-zinc-300 focus:outline-none bg-transparent"
           placeholder="文章标题"
         />
         <div class="text-xs text-zinc-400 mt-1">{{ form.title.length }}/50</div>
      </div>

      <div>
        <label class="block text-sm font-medium text-zinc-700 mb-2">摘要</label>
        <textarea
          v-model="form.summary"
          maxlength="300"
          rows="2"
          class="w-full border border-zinc-200 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200 resize-none"
          placeholder="用一两句话概括文章内容（选填）"
        ></textarea>
        <div class="text-xs text-zinc-400 mt-1">{{ form.summary.length }}/300</div>

        <div class="flex items-center gap-2 mt-3">
          <button
            type="button"
            @click="form.genAiSummary = form.genAiSummary === 1 ? 0 : 1"
            class="w-4 h-4 rounded border-2 flex items-center justify-center transition-colors duration-200 cursor-pointer flex-shrink-0"
            :class="form.genAiSummary === 1 ? 'bg-accent border-accent' : 'border-zinc-300'"
          >
            <svg v-if="form.genAiSummary === 1" class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/></svg>
          </button>
          <span class="text-xs text-zinc-500 select-none cursor-pointer" @click="form.genAiSummary = form.genAiSummary === 1 ? 0 : 1">发布后生成 AI 摘要</span>
        </div>
        <p v-if="form.genAiSummary === 1" class="text-xs text-amber-600 mt-1.5">开启后你填写的摘要将被 AI 生成的摘要替代</p>

        <div class="flex items-center gap-2 mt-3">
          <button
            type="button"
            @click="form.genAiSummaryLong = form.genAiSummaryLong === 1 ? 0 : 1"
            class="w-4 h-4 rounded border-2 flex items-center justify-center transition-colors duration-200 cursor-pointer flex-shrink-0"
            :class="form.genAiSummaryLong === 1 ? 'bg-accent border-accent' : 'border-zinc-300'"
          >
            <svg v-if="form.genAiSummaryLong === 1" class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/></svg>
          </button>
          <span class="text-xs text-zinc-500 select-none cursor-pointer" @click="form.genAiSummaryLong = form.genAiSummaryLong === 1 ? 0 : 1">生成 AI 总结</span>
        </div>
      </div>

      <div>
        <label class="block text-sm font-medium text-zinc-700 mb-2">标签</label>
        <div v-if="tagsLoading" class="text-xs text-zinc-400">加载中...</div>
        <div v-else class="flex flex-wrap gap-2">
          <button
            type="button"
            v-for="tag in availableTags"
            :key="tag.id"
            @click="toggleTag(tag)"
            class="px-3 py-1.5 text-xs font-medium rounded-lg border transition-all duration-200 cursor-pointer"
            :class="isTagSelected(tag)
              ? 'bg-accent/10 border-accent/30 text-accent'
              : 'border-zinc-200 text-zinc-500 hover:border-zinc-300 hover:text-zinc-700'"
          >
            {{ tag.name }}
          </button>
          <div class="flex items-center gap-1">
            <input
              v-model="newTagInput"
              @keydown.enter.prevent="handleAddTag"
              placeholder="新标签"
              maxlength="20"
              class="w-20 px-2 py-1.5 text-xs border border-dashed border-zinc-300 rounded-lg focus:outline-none focus:border-accent transition-colors"
            />
            <button
              type="button"
              @click="handleAddTag"
              :disabled="!newTagInput.trim() || selectedTags.length >= 10"
              class="px-2 py-1.5 text-xs font-medium text-zinc-400 hover:text-accent disabled:opacity-40 transition-colors cursor-pointer"
            >+</button>
          </div>
        </div>
        <div v-if="selectedTags.length" class="text-xs text-zinc-400 mt-1">已选 {{ selectedTags.length }}/10 个</div>
      </div>

      <div>
        <div class="flex items-center justify-between mb-2">
          <label class="text-sm font-medium text-zinc-700">正文 <span class="text-zinc-400 font-normal">(Markdown)</span></label>
          <span class="text-xs text-zinc-400">{{ form.content.length }}/10000</span>
        </div>
        <div class="border border-zinc-200 rounded-xl overflow-hidden focus-within:ring-2 focus-within:ring-accent/20 focus-within:border-accent transition-all duration-200">
          <div class="bg-zinc-50 border-b border-zinc-100 px-4 py-2.5 flex items-center justify-between">
            <span class="text-xs text-zinc-400 font-mono">Markdown</span>
            <span class="text-xs text-zinc-300">支持 Markdown 语法</span>
          </div>
          <textarea
            v-model="form.content"
            required
            maxlength="10000"
            rows="20"
            class="w-full border-none px-4 py-4 text-sm font-mono leading-relaxed text-zinc-700 placeholder:text-zinc-300 resize-y focus:outline-none bg-white"
            placeholder="# 开始写作..."
          ></textarea>
        </div>
      </div>

      <div v-if="error" class="bg-red-50 border border-red-200 rounded-xl p-4">
        <p class="text-sm text-red-600">{{ error }}</p>
      </div>

      <div class="flex items-center gap-3 pt-2">
        <button
          type="submit"
          :disabled="submitting || !form.title.trim() || !form.content.trim()"
          class="px-6 py-2.5 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
        >
          {{ submitting && form.status === 'PUBLISHED' ? '发布中...' : isEdit ? '更新文章' : '发布文章' }}
        </button>
        <button
          type="button"
          :disabled="submitting || !form.title.trim()"
          @click="handleSaveDraft"
          class="px-6 py-2.5 text-sm font-medium text-zinc-600 border border-zinc-200 rounded-lg hover:bg-zinc-50 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
        >
          {{ submitting && form.status === 'DRAFT' ? '保存中...' : '保存草稿' }}
        </button>
        <RouterLink to="/dashboard" class="px-6 py-2.5 text-sm font-medium text-zinc-400 hover:text-zinc-600 transition-colors duration-200">
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
  genAiSummary: 0 as 0 | 1,
  genAiSummaryLong: 0 as 0 | 1
})

const availableTags = ref<Tag[]>([])
const selectedTags = ref<Tag[]>([])
const tagsLoading = ref(false)
const submitting = ref(false)
const error = ref('')
const loadError = ref('')
const newTagInput = ref('')

function isTagSelected(tag: Tag) {
  return selectedTags.value.some(t => t.id === tag.id)
}

function toggleTag(tag: Tag) {
  const idx = selectedTags.value.findIndex(t => t.id === tag.id)
  if (idx > -1) {
    selectedTags.value.splice(idx, 1)
  } else if (selectedTags.value.length < 10) {
    selectedTags.value.push(tag)
  }
}

async function handleAddTag() {
  const name = newTagInput.value.trim()
  if (!name) return
  if (selectedTags.value.length >= 10) {
    error.value = '标签不能超过10个'
    return
  }
  const existing = availableTags.value.find(t => t.name === name)
  if (!existing) {
    const tempTag = { id: -(Date.now()), name, articleCount: 0 } as Tag
    availableTags.value.push(tempTag)
    selectedTags.value.push(tempTag)
  } else if (!isTagSelected(existing)) {
    selectedTags.value.push(existing)
  }
  newTagInput.value = ''
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
    selectedTags.value = article.tags || []
  } catch (e: any) {
    loadError.value = e?.response?.data?.message || '加载文章失败'
  }
}

async function doSave(status: 'PUBLISHED' | 'DRAFT') {
  if (!form.title.trim()) {
    error.value = '请填写标题'
    return
  }
  if (form.title.length > 50) {
    error.value = '标题不能超过50字'
    return
  }
  if (form.content.length > 10000) {
    error.value = '正文不能超过10000字'
    return
  }
  if (status === 'PUBLISHED' && !form.content.trim()) {
    error.value = '请填写正文内容'
    return
  }
  if (status === 'PUBLISHED' && selectedTags.value.length === 0) {
    error.value = '请至少选择一个标签'
    return
  }
  if (selectedTags.value.length > 10) {
    error.value = '标签不能超过10个'
    return
  }
  form.status = status
  submitting.value = true
  error.value = ''
  try {
    const data = {
      title: form.title.trim(),
      content: form.content,
      summary: form.summary.trim() || undefined,
      status: status,
      tagNames: selectedTags.value.map(t => t.name),
      genAiSummary: status === 'PUBLISHED' ? form.genAiSummary : 0,
      genAiSummaryLong: status === 'PUBLISHED' ? form.genAiSummaryLong : 0
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

async function handleSave() {
  await doSave('PUBLISHED')
}

async function handleSaveDraft() {
  await doSave('DRAFT')
}

onMounted(() => {
  loadTags()
  if (isEdit.value) loadArticle()
})
</script>
