<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <div v-if="loading" class="space-y-6 animate-pulse">
      <div class="h-10 bg-zinc-100 rounded w-3/4"></div>
      <div class="flex gap-3">
        <div class="w-10 h-10 rounded-full bg-zinc-100"></div>
        <div class="space-y-2">
          <div class="h-4 bg-zinc-100 rounded w-28"></div>
          <div class="h-3 bg-zinc-100 rounded w-20"></div>
        </div>
      </div>
      <div class="space-y-3 mt-8">
        <div class="h-4 bg-zinc-100 rounded"></div>
        <div class="h-4 bg-zinc-100 rounded w-5/6"></div>
        <div class="h-4 bg-zinc-100 rounded w-2/3"></div>
      </div>
    </div>

    <div v-else-if="!loading && !article" class="min-h-[50vh] flex items-center justify-center">
      <div class="text-center">
        <div class="mb-5 inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50">
          <svg class="w-8 h-8 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-3-3v6m-7 5.3A9 9 0 1112 3a9 9 0 01-7 14.3z"/>
          </svg>
        </div>
        <h2 class="text-lg font-semibold text-zinc-900 mb-2 font-serif">出错了</h2>
        <p class="text-sm text-zinc-500 mb-6 max-w-md">{{ errorMessage }}</p>
        <div class="flex items-center justify-center gap-3">
          <button @click="fetchDetail" class="px-5 py-2.5 bg-zinc-900 text-white text-sm font-medium rounded-lg hover:bg-zinc-800 transition-colors duration-200 cursor-pointer">重试</button>
          <RouterLink to="/" class="px-5 py-2.5 border border-zinc-200 text-sm font-medium rounded-lg text-zinc-600 hover:bg-zinc-50 transition-colors duration-200">返回首页</RouterLink>
        </div>
      </div>
    </div>

    <template v-else-if="article">
      <div v-if="article.coverImage" class="aspect-[21/9] rounded-xl overflow-hidden mb-10 bg-zinc-100">
        <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" />
      </div>

      <h1 class="text-3xl md:text-4xl font-bold text-zinc-900 mb-6 leading-tight font-serif">
        {{ article.title }}
      </h1>

      <div class="flex items-center gap-3 mb-6">
        <span class="w-10 h-10 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-500 font-semibold text-sm flex-shrink-0 overflow-hidden">
          <img v-if="article.author.avatar" :src="article.author.avatar" class="w-full h-full object-cover" />
          <template v-else>{{ article.author.username.charAt(0).toUpperCase() }}</template>
        </span>
        <div>
          <div class="text-sm font-semibold text-zinc-900">{{ article.author.username }}</div>
          <div class="text-xs text-zinc-400 flex items-center gap-2">
            <span>{{ formatDateTime(article.createdAt) }}</span>
            <span v-if="article.status !== 'PUBLISHED'" class="px-1.5 py-0.5 bg-yellow-50 text-yellow-700 rounded text-xs font-medium">
              {{ article.status }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="article.tags?.length" class="flex gap-2 mb-10 flex-wrap">
        <span v-for="tag in article.tags" :key="tag.id" class="px-3 py-1 text-xs font-medium text-zinc-500 bg-zinc-100 rounded-lg">
          {{ tag.name }}
        </span>
      </div>

      <div class="flex items-center gap-6 mb-10 text-sm text-zinc-400">
        <span class="flex items-center gap-1.5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
          {{ formatNumber(article.viewCount) }} 阅读
        </span>
        <button
          v-if="userStore.isLoggedIn"
          @click="handleLike"
          class="flex items-center gap-1.5 transition-colors duration-200 cursor-pointer"
          :class="article.isLiked ? 'text-red-500' : 'hover:text-red-500'"
        >
          <svg class="w-4 h-4" :fill="article.isLiked ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
          {{ formatNumber(likeCount) }}
        </button>
        <span v-else class="flex items-center gap-1.5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
          {{ formatNumber(likeCount) }}
        </span>
      </div>

      <div v-if="article.aiSummaryLongStatus === 1 || aiGenerating" class="mb-8 bg-gradient-to-r from-pink-50 to-purple-50 rounded-xl p-5 border border-pink-100/50">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <svg class="w-5 h-5 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
            <span class="text-sm font-semibold text-zinc-700">AI 总结</span>
          </div>
          <button
            v-if="article.aiSummaryLongStatus === 1 && !aiGenerating"
            class="text-zinc-400 hover:text-zinc-600 transition-colors duration-200 p-1 rounded cursor-pointer"
            @click="aiCollapsed = !aiCollapsed"
          >
            <svg class="w-4 h-4 transition-transform duration-200" :class="{ 'rotate-180': !aiCollapsed }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
            </svg>
          </button>
        </div>
        <div v-if="aiCollapsed && article.aiSummaryLongStatus === 1 && !aiGenerating">
          <div class="text-sm text-zinc-500 cursor-pointer hover:text-accent transition-colors duration-200" @click="aiCollapsed = false">
            点击展开AI总结
            <svg class="w-3.5 h-3.5 inline-block ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
            </svg>
          </div>
        </div>
        <div v-show="!aiCollapsed || aiGenerating">
          <div v-if="aiGenerating" class="text-sm text-zinc-500">
            生成中<span class="inline-block animate-pulse tracking-[0.3em]">...</span>
          </div>
          <div v-else-if="article.aiSummaryLongStatus === 1" class="text-sm text-zinc-700 leading-relaxed">
            <div class="prose prose-zinc prose-sm max-w-none" v-html="renderedAiSummary"></div>
          </div>
          <div v-if="aiError" class="text-sm text-red-400 mt-2">{{ aiError }}</div>
        </div>
      </div>

      <div v-else-if="article.aiSummaryLongStatus === 0" class="mb-8 bg-gradient-to-r from-pink-50 to-purple-50 rounded-xl p-5 border border-pink-100/50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <svg class="w-5 h-5 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
            <span class="text-sm font-semibold text-zinc-700">AI 总结</span>
          </div>
          <button
            class="px-4 py-1.5 bg-accent text-white text-xs font-medium rounded-lg hover:bg-pink-600 transition-colors duration-200 cursor-pointer"
            @click="generateAiSummary"
          >
            生成 AI 总结
          </button>
        </div>
        <div v-if="aiError" class="text-sm text-red-400 mt-2">{{ aiError }}</div>
      </div>

      <div class="prose prose-zinc max-w-none mb-12 border-t border-zinc-100 pt-10" v-html="renderedContent"></div>

      <div class="border-t border-zinc-100 pt-10">
        <CommentList
          :list="comments" :loading="commentLoading" :total="commentTotal"
          :current-page="commentPage" :page-size="commentPageSize"
          :article-author-id="article.author.id"
          @reply="handleReply" @delete="handleDeleteComment" @page-change="handleCommentPageChange"
        />

        <div v-if="userStore.isLoggedIn" class="mt-6 bg-zinc-50 rounded-xl p-5">
          <div v-if="replyTarget" class="flex items-center justify-between text-sm text-zinc-500 mb-3">
            <span>回复 <span class="text-zinc-900 font-semibold">@{{ replyTarget }}</span></span>
            <button @click="cancelReply" class="text-zinc-400 hover:text-zinc-600 transition-colors duration-200 cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <textarea
            v-model="commentText" rows="3"
            class="w-full border border-zinc-200 rounded-lg p-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all duration-200 bg-white"
            placeholder="写下你的评论..." maxlength="1000"
          ></textarea>
          <div class="flex justify-between items-center mt-3">
            <span class="text-xs text-zinc-400">{{ commentText.length }}/1000</span>
            <button
              :disabled="!commentText.trim() || submitting"
              @click="submitComment"
              class="px-5 py-2 bg-zinc-900 text-white text-sm font-medium rounded-lg hover:bg-zinc-800 disabled:opacity-50 transition-colors duration-200 cursor-pointer"
            >
              {{ submitting ? '提交中...' : '发表评论' }}
            </button>
          </div>
        </div>
        <div v-else class="mt-6 text-center text-sm text-zinc-400 bg-zinc-50 rounded-xl p-6">
          <RouterLink to="/login" class="text-accent underline underline-offset-2 font-semibold">登录</RouterLink> 后参与评论
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { marked } from 'marked'
import { formatDateTime, formatNumber } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { articleService } from '@/services/articleService'
import { commentService } from '@/services/commentService'
import CommentList from '@/components/comment/CommentList.vue'
import type { Article } from '@/types/article'
import type { Comment } from '@/types/comment'

const route = useRoute()
const userStore = useUserStore()

const article = ref<Article | null>(null)
const loading = ref(true)
const error = ref('')
const likeCount = ref(0)

const errorMessage = computed(() => error.value || '请检查网络连接后重试')

const comments = ref<Comment[]>([])
const commentLoading = ref(false)
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(20)

const commentText = ref('')
const submitting = ref(false)
const replyTarget = ref<string | null>(null)
const replyParentId = ref<number | null>(null)

const aiError = ref('')
const aiGenerating = ref(false)
const aiCollapsed = ref(true)
let aiPollActive = false

const renderedAiSummary = ref('')
let typingTimer: ReturnType<typeof setInterval> | null = null

function startTyping(fullText: string) {
  stopTyping()
  let pos = 0
  renderedAiSummary.value = ''
  const speed = 15
  typingTimer = setInterval(() => {
    pos += 3
    if (pos >= fullText.length) {
      renderedAiSummary.value = marked(fullText, { breaks: true }) as string
      stopTyping()
      return
    }
    renderedAiSummary.value = marked(fullText.slice(0, pos), { breaks: true }) as string
  }, speed)
}

function stopTyping() {
  if (typingTimer != null) {
    clearInterval(typingTimer)
    typingTimer = null
  }
}

watch(() => article.value?.aiSummaryLong, (val) => {
  if (val && !aiGenerating.value) {
    startTyping(val)
  }
})

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return marked(article.value.content, { breaks: true })
})

async function fetchDetail() {
  const id = Number(route.params.id)
  if (!id) { error.value = '无效的文章ID'; loading.value = false; return }
  loading.value = true; error.value = ''
  try {
    const res = await articleService.getDetail(id)
    article.value = res; likeCount.value = res.likeCount
  } catch (e: any) {
    const data = e?.response?.data
    error.value = data?.message || '加载失败'
  }
  finally { loading.value = false }
}

async function fetchComments() {
  const id = Number(route.params.id)
  if (!id) return
  commentLoading.value = true
  try {
    const res = await commentService.getList({ articleId: id, page: commentPage.value, pageSize: commentPageSize.value })
    comments.value = res.records; commentTotal.value = res.total
  } catch { /* silent */ }
  finally { commentLoading.value = false }
}

async function handleLike() {
  const id = Number(route.params.id)
  if (!id || !article.value) return
  try {
    const res = await articleService.like(id)
    article.value.isLiked = res.isLiked; likeCount.value = res.likeCount
  } catch { /* silent */ }
}

async function submitComment() {
  const id = Number(route.params.id)
  if (!id || !commentText.value.trim()) return
  submitting.value = true
  try {
    await commentService.create({ articleId: id, content: commentText.value, parentId: replyParentId.value })
    commentText.value = ''; replyTarget.value = null; replyParentId.value = null
    commentPage.value = 1; await fetchComments()
  } catch { /* keep text */ }
  finally { submitting.value = false }
}

function handleReply(commentId: number, username: string) {
  replyTarget.value = username; replyParentId.value = commentId; commentText.value = ''
}

function cancelReply() { replyTarget.value = null; replyParentId.value = null; commentText.value = '' }

async function generateAiSummary() {
  const id = Number(route.params.id)
  if (!id) return
  aiGenerating.value = true
  aiError.value = ''
  try {
    await articleService.genSummaryLong(id)
    await pollAiSummary(id)
  } catch (e: any) {
    aiGenerating.value = false
    aiError.value = e?.response?.data?.message || '生成失败，请重试'
  }
}

async function pollAiSummary(id: number) {
  aiPollActive = true
  const maxAttempts = 60
  for (let i = 0; i < maxAttempts && aiPollActive; i++) {
    await new Promise(r => setTimeout(r, 2000))
    if (!aiPollActive) return
    try {
      const res = await articleService.getAiSummary(id)
      if (res.status === 1) {
        aiGenerating.value = false
        if (article.value) {
          article.value.aiSummaryLong = res.summaryLong
          article.value.aiSummaryLongStatus = 1
        }
        aiCollapsed.value = false
        aiPollActive = false
        return
      }
    } catch { /* continue polling */ }
  }
  aiPollActive = false
  aiGenerating.value = false
  aiError.value = '生成超时，请重试'
}

async function handleDeleteComment(commentId: number) {
  try { await commentService.delete(commentId); await fetchComments() } catch { /* silent */ }
}

function handleCommentPageChange(p: number) { commentPage.value = p; fetchComments() }

onMounted(() => { fetchDetail(); fetchComments() })
onBeforeUnmount(() => { aiPollActive = false; stopTyping() })
</script>
