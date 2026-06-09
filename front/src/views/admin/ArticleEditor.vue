<template>
  <div class="article-editor-page">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-lg font-semibold text-gray-900">{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <el-card>
      <div v-if="error" class="mb-4">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>

      <el-form label-width="60px" class="max-w-4xl">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="输入文章标题" maxlength="200" show-word-limit size="large" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" class="w-full">
                <el-option label="发布" value="PUBLISHED" />
                <el-option label="草稿" value="DRAFT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="摘要">
              <el-input v-model="form.summary" placeholder="选填" maxlength="300" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标签">
          <el-select
            v-model="form.selectedTagIds"
            multiple
            filterable
            placeholder="选择标签"
            class="w-full"
            :loading="tagsLoading"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="AI总结">
          <el-checkbox v-model="form.genAiSummaryLong" :true-value="1" :false-value="0">发布后生成 AI 总结</el-checkbox>
        </el-form-item>

        <el-form-item label="内容">
          <div class="editor-wrapper">
            <div class="editor-toolbar">
              <span class="text-xs text-gray-400">Markdown</span>
              <span class="text-xs text-gray-300">{{ form.content.length }} 字</span>
            </div>
            <textarea
              v-model="form.content"
              class="editor-textarea"
              placeholder="# 标题&#10;&#10;开始写作..."
            ></textarea>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="flex gap-3">
            <el-button type="primary" :loading="submitting" @click="handleSave" size="large">
              {{ isEdit ? '更新' : '发布' }}
            </el-button>
            <el-button @click="$router.back()" size="large">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { articleService } from '@/services/admin/articleService'
import { tagService } from '@/services/admin/tagService'
import type { Tag } from '@/types/article'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  content: '',
  summary: '',
  status: 'PUBLISHED' as 'PUBLISHED' | 'DRAFT',
  selectedTagIds: [] as number[],
  genAiSummaryLong: 0 as 0 | 1
})

const availableTags = ref<Tag[]>([])
const tagsLoading = ref(false)
const submitting = ref(false)
const error = ref('')

async function loadTags() {
  tagsLoading.value = true
  try {
    availableTags.value = await tagService.getList()
  } catch { /* ignore */ }
  finally { tagsLoading.value = false }
}

async function loadArticle() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const article = await articleService.getDetail(id)
    form.title = article.title
    form.content = article.content
    form.summary = article.summary
    form.status = article.status as 'PUBLISHED' | 'DRAFT'
    form.selectedTagIds = article.tags?.map((t: Tag) => t.id) || []
  } catch (e: any) {
    error.value = '加载文章失败'
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
    if (isEdit.value) {
      await articleService.update(Number(route.params.id), {
        title: form.title,
        content: form.content,
        summary: form.summary,
        status: form.status,
        tagIds: form.selectedTagIds,
        genAiSummaryLong: form.genAiSummaryLong
      })
    } else {
      await articleService.create({
        title: form.title,
        content: form.content,
        summary: form.summary,
        status: form.status,
        tagIds: form.selectedTagIds,
        genAiSummaryLong: form.genAiSummaryLong
      })
    }
    router.push('/admin/articles')
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

<style scoped>
.editor-wrapper {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.editor-wrapper:focus-within {
  border-color: #6366f1;
}
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.editor-textarea {
  display: block;
  width: 100%;
  min-height: 420px;
  padding: 16px;
  border: none;
  font-family: 'JetBrains Mono', 'Fira Code', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  resize: vertical;
  outline: none;
  color: #1f2937;
  background: #fcfcfc;
}
.editor-textarea::placeholder {
  color: #c4c4c4;
}
</style>
