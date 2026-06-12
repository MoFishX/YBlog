<template>
  <div :id="`comment-${comment.id}`" class="py-4 first:pt-0 last:pb-0">
    <div class="flex items-start gap-3">
      <div class="w-7 h-7 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-500 text-xs font-semibold flex-shrink-0 overflow-hidden">
        <img v-if="comment.user.avatar" :src="comment.user.avatar" class="w-full h-full object-cover" />
        <template v-else>{{ comment.user.username.charAt(0).toUpperCase() }}</template>
      </div>
      <div class="flex-1 min-w-0">
        <div class="flex items-center flex-wrap gap-x-2 gap-y-1 mb-1">
          <span class="text-sm font-semibold text-zinc-900">{{ comment.user.username }}</span>
          <span v-if="comment.replyTo" class="text-xs text-zinc-500">回复 @{{ comment.replyTo.username }}</span>
          <span class="text-xs text-zinc-400">{{ formatDateTime(comment.createdAt) }}</span>
        </div>
        <div
          class="text-sm text-zinc-600 leading-relaxed prose prose-zinc prose-sm max-w-none"
          :class="{ 'line-clamp-3': contentCollapsed && isLong }"
          v-html="renderedContent"
        ></div>
        <button
          v-if="isLong"
          class="text-xs font-medium text-zinc-400 hover:text-accent transition-colors duration-200 cursor-pointer mt-1"
          @click="contentCollapsed = !contentCollapsed"
        >
          {{ contentCollapsed ? '展开' : '收起' }}
        </button>
        <div class="flex items-center gap-3 mt-1.5">
          <button v-if="isLoggedIn" class="text-xs font-medium text-zinc-400 hover:text-accent transition-colors duration-200 cursor-pointer" @click="$emit('reply', comment.id, comment.user.username)">
            回复
          </button>
          <button v-if="canDelete" class="text-xs font-medium text-zinc-400 hover:text-red-500 transition-colors duration-200 cursor-pointer" @click="$emit('delete', comment.id)">
            删除
          </button>
        </div>
      </div>
    </div>

    <div v-if="comment.children?.length" class="ml-8 mt-2 pl-4 border-l-2 border-zinc-100">
      <button
        v-if="!repliesExpanded"
        class="w-full py-2 text-xs font-medium text-zinc-400 hover:text-accent transition-colors duration-200 cursor-pointer text-left"
        @click="repliesExpanded = true"
      >
        展开 {{ comment.children.length }} 条回复
      </button>
      <template v-else>
        <CommentItem
          v-for="child in comment.children" :key="child.id" :comment="child" :depth="depth + 1" :article-author-id="articleAuthorId"
          @reply="(id, name) => $emit('reply', id, name)"
          @delete="(id) => $emit('delete', id)"
        />
        <button
          class="w-full py-1 text-xs font-medium text-zinc-400 hover:text-zinc-600 transition-colors duration-200 cursor-pointer text-left"
          @click="repliesExpanded = false"
        >
          收起回复
        </button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { marked } from 'marked'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

interface CommentNode {
  id: number
  content: string
  user: { id: number; username: string; avatar?: string | null }
  replyTo?: { id: number; username: string } | null
  parentId?: number | null
  createdAt: string
  children: CommentNode[]
}

const props = withDefaults(defineProps<{ comment: CommentNode; articleAuthorId?: number; depth?: number }>(), { depth: 0 })
defineEmits<{ reply: [commentId: number, username: string]; delete: [commentId: number] }>()

const contentCollapsed = ref(true)
const repliesExpanded = ref(props.depth === 0)

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const canDelete = computed(() =>
  userStore.user?.id === props.comment.user.id ||
  userStore.user?.id === props.articleAuthorId ||
  userStore.isAdmin
)

const isLong = computed(() => props.comment.content.length > 200)
const renderedContent = computed(() => marked(props.comment.content, { breaks: true }) as string)
</script>