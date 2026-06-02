<template>
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <div v-if="loading" class="space-y-6 animate-pulse">
      <div class="h-8 bg-gray-200 rounded w-3/4"></div>
      <div class="h-4 bg-gray-200 rounded w-1/4"></div>
      <div class="space-y-3">
        <div class="h-4 bg-gray-200 rounded"></div>
        <div class="h-4 bg-gray-200 rounded w-5/6"></div>
        <div class="h-4 bg-gray-200 rounded w-2/3"></div>
      </div>
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
      <p class="text-red-600 mb-4">{{ error }}</p>
      <button @click="fetchDetail" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors">
        重试
      </button>
    </div>

    <template v-else-if="article">
      <h1 class="text-3xl font-bold text-gray-900 mb-4">{{ article.title }}</h1>

      <div class="flex items-center gap-4 mb-6 text-sm text-gray-500">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-xs font-medium">
            {{ article.author.username.charAt(0).toUpperCase() }}
          </div>
          <span>{{ article.author.username }}</span>
        </div>
        <span>{{ formatDateTime(article.createdAt) }}</span>
        <span v-if="article.status !== 'PUBLISHED'" class="px-2 py-0.5 bg-yellow-100 text-yellow-700 rounded text-xs">
          {{ article.status }}
        </span>
      </div>

      <div v-if="article.tags?.length" class="flex gap-2 mb-6">
        <span
          v-for="tag in article.tags"
          :key="tag.id"
          class="px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-xs"
        >
          {{ tag.name }}
        </span>
      </div>

      <div class="flex items-center gap-6 mb-8 text-sm text-gray-500">
        <span>&#128065; {{ formatNumber(article.viewCount) }} 阅读</span>
        <button
          @click="handleLike"
          class="flex items-center gap-1 transition-colors"
          :class="article.isLiked ? 'text-red-500' : 'text-gray-500 hover:text-red-500'"
        >
          &#10084; {{ formatNumber(likeCount) }}
        </button>
      </div>

      <div class="prose prose-lg max-w-none mb-12 border-t border-gray-200 pt-8">
        <div v-html="renderedContent"></div>
      </div>

      <CommentList
        :list="comments"
        :loading="commentLoading"
        :total="commentTotal"
        :current-page="commentPage"
        :page-size="commentPageSize"
        :article-author-id="article.author.id"
        @reply="handleReply"
        @delete="handleDeleteComment"
        @page-change="handleCommentPageChange"
      />

      <div v-if="userStore.isLoggedIn" class="mt-6 border border-gray-200 rounded-lg p-4">
        <div v-if="replyTarget" class="text-sm text-gray-500 mb-2">
          回复 @{{ replyTarget }} &nbsp;
          <button @click="cancelReply" class="text-blue-500 hover:underline">取消</button>
        </div>
        <textarea
          v-model="commentText"
          rows="3"
          class="w-full border border-gray-300 rounded-lg p-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="写下你的评论..."
          maxlength="1000"
        ></textarea>
        <div class="flex justify-between items-center mt-2">
          <span class="text-xs text-gray-400">{{ commentText.length }}/1000</span>
          <button
            :disabled="!commentText.trim() || submitting"
            @click="submitComment"
            class="px-4 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {{ submitting ? '提交中...' : '发表评论' }}
          </button>
        </div>
      </div>
      <div v-else class="mt-6 text-center text-sm text-gray-500">
        <RouterLink to="/login" class="text-blue-600 hover:underline">登录</RouterLink> 后参与评论
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { formatDateTime, formatNumber } from '@/utils/storage'
import { useUserStore } from '@/stores/user'
import { articleService } from '@/services/articleService'
import { commentService } from '@/services/commentService'
import CommentList from '@/components/comment/CommentList.vue'
import type { Article } from '@shared/types/article'
import type { Comment } from '@shared/types/comment'

const route = useRoute()
const userStore = useUserStore()

const article = ref<Article | null>(null)
const loading = ref(true)
const error = ref('')
const likeCount = ref(0)

const comments = ref<Comment[]>([])
const commentLoading = ref(false)
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(20)

const commentText = ref('')
const submitting = ref(false)
const replyTarget = ref<string | null>(null)
const replyParentId = ref<number | null>(null)

function renderMarkdown(content: string): string {
  let html = content
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/### (.+)/g, '<h3 class="text-xl font-semibold mt-6 mb-2">$1</h3>')
    .replace(/## (.+)/g, '<h2 class="text-2xl font-semibold mt-8 mb-3">$1</h2>')
    .replace(/# (.+)/g, '<h1 class="text-2xl font-bold mt-8 mb-4">$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre class="bg-gray-100 rounded-lg p-4 overflow-x-auto my-3 text-sm"><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code class="bg-gray-100 px-1.5 py-0.5 rounded text-sm text-pink-600">$1</code>')
    .replace(/\n\n/g, '</p><p class="my-3">')
  html = '<p class="my-3">' + html + '</p>'
  return html
}

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return renderMarkdown(article.value.content)
})

async function fetchDetail() {
  loading.value = true
  error.value = ''
  try {
    const id = Number(route.params.id)
    article.value = await articleService.getDetail(id)
    likeCount.value = article.value.likeCount
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载文章失败'
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  commentLoading.value = true
  try {
    const id = Number(route.params.id)
    const res = await commentService.getList(id, { page: commentPage.value, pageSize: commentPageSize.value })
    comments.value = res.records
    commentTotal.value = res.total
  } catch {
    // silent
  } finally {
    commentLoading.value = false
  }
}

async function handleLike() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await articleService.like(article.value!.id)
    if (article.value) {
      article.value.isLiked = res.isLiked
    }
    likeCount.value = res.likeCount
  } catch (e: any) {
    error.value = e?.response?.data?.message || '操作失败'
  }
}

async function submitComment() {
  if (!commentText.value.trim()) return
  submitting.value = true
  try {
    const id = Number(route.params.id)
    await commentService.create(id, {
      content: commentText.value.trim(),
      parentId: replyParentId.value
    })
    commentText.value = ''
    replyTarget.value = null
    replyParentId.value = null
    commentPage.value = 1
    await fetchComments()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '评论失败'
  } finally {
    submitting.value = false
  }
}

function handleReply(commentId: number, username: string) {
  replyTarget.value = username
  replyParentId.value = commentId
  commentText.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyParentId.value = null
  commentText.value = ''
}

async function handleDeleteComment(commentId: number) {
  try {
    await commentService.delete(commentId)
    await fetchComments()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '删除失败'
  }
}

function handleCommentPageChange(p: number) {
  commentPage.value = p
  fetchComments()
}

onMounted(() => {
  fetchDetail()
  fetchComments()
})
</script>
