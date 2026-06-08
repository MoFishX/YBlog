<template>
  <div class="py-4 first:pt-0 last:pb-0">
    <div class="flex items-start gap-3">
      <div class="w-7 h-7 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-500 text-xs font-semibold flex-shrink-0">
        {{ comment.user.username.charAt(0).toUpperCase() }}
      </div>
      <div class="flex-1 min-w-0">
        <div class="flex items-center flex-wrap gap-x-2 gap-y-1 mb-1">
          <span class="text-sm font-semibold text-zinc-900">{{ comment.user.username }}</span>
          <span v-if="comment.replyTo" class="text-xs text-zinc-500">回复 @{{ comment.replyTo.username }}</span>
          <span class="text-xs text-zinc-400">{{ formatDateTime(comment.createdAt) }}</span>
        </div>
        <p class="text-sm text-zinc-600 leading-relaxed">{{ comment.content }}</p>
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
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import type { Comment } from '@/types/comment'

const props = defineProps<{ comment: Comment; articleAuthorId?: number }>()
defineEmits<{ reply: [commentId: number, username: string]; delete: [commentId: number] }>()

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const canDelete = computed(() =>
  userStore.user?.id === props.comment.user.id ||
  userStore.user?.id === props.articleAuthorId ||
  userStore.isAdmin
)
</script>
