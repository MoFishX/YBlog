<template>
  <div class="py-3 border-b border-gray-100 last:border-0">
    <div class="flex items-start gap-3">
      <div class="w-7 h-7 rounded-full bg-gray-100 flex items-center justify-center text-gray-500 text-xs font-medium flex-shrink-0">
        {{ comment.user.username.charAt(0).toUpperCase() }}
      </div>
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 mb-1">
          <span class="text-sm font-medium text-gray-700">{{ comment.user.username }}</span>
          <span v-if="comment.replyTo" class="text-xs text-gray-400">
            回复 @{{ comment.replyTo.username }}
          </span>
          <span class="text-xs text-gray-400">{{ formatDateTime(comment.createdAt) }}</span>
        </div>
        <p class="text-sm text-gray-600">{{ comment.content }}</p>
        <button
          v-if="isLoggedIn"
          class="text-xs text-blue-500 hover:text-blue-700 mt-1"
          @click="$emit('reply', comment.id, comment.user.username)"
        >
          回复
        </button>
      </div>
      <button
        v-if="canDelete"
        class="text-xs text-red-400 hover:text-red-600 flex-shrink-0"
        @click="$emit('delete', comment.id)"
      >
        删除
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatDateTime } from '@/utils/storage'
import { useUserStore } from '@/stores/user'
import type { Comment } from '@shared/types/comment'

const props = defineProps<{
  comment: Comment
  articleAuthorId?: number
}>()

defineEmits<{
  reply: [commentId: number, username: string]
  delete: [commentId: number]
}>()

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const canDelete = computed(() =>
  userStore.user?.id === props.comment.user.id ||
  userStore.user?.id === props.articleAuthorId ||
  userStore.isAdmin
)
</script>
