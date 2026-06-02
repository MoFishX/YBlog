<template>
  <div class="article-editor-page">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-semibold">{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-card>
      <div v-if="error" class="mb-4">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>

      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="文章标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="选填，不填则自动截取" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="发布状态">
            <el-option label="发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <textarea
            v-model="form.content"
            class="editor-textarea"
            placeholder="Markdown 格式内容..."
          ></textarea>
        </el-form-item>
        <el-form-item>
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSave">
            {{ isEdit ? '更新' : '发布' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleService } from '@/services/articleService'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  content: '',
  summary: '',
  status: 'PUBLISHED' as 'PUBLISHED' | 'DRAFT'
})

const submitting = ref(false)
const error = ref('')

async function loadArticle() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const article = await articleService.getDetail(id)
    form.title = article.title
    form.content = article.content
    form.summary = article.summary
    form.status = article.status as 'PUBLISHED' | 'DRAFT'
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
        status: form.status
      })
    } else {
      await articleService.create({
        title: form.title,
        content: form.content,
        summary: form.summary,
        status: form.status
      })
    }
    router.push('/articles')
  } catch (e: any) {
    error.value = e?.response?.data?.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit.value) loadArticle()
})
</script>

<style scoped>
.editor-textarea {
  width: 100%;
  min-height: 400px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  outline: none;
}
.editor-textarea:focus {
  border-color: #409EFF;
}
</style>
