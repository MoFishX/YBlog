<template>
  <div>
    <h3 class="text-base font-bold text-zinc-900 mb-6 font-serif">评论 <span class="text-sm font-normal text-zinc-400">({{ total }})</span></h3>
    <LoadingSkeleton v-if="loading" :rows="3" />
    <EmptyState v-else-if="list.length === 0" text="暂无评论，来发表第一条吧" />
    <div v-else class="divide-y divide-zinc-100">
      <CommentItem
        v-for="root in tree" :key="root.id" :comment="root" :article-author-id="articleAuthorId"
        @reply="(id, name) => $emit('reply', id, name)"
        @delete="(id) => $emit('delete', id)"
      />
    </div>
    <Pagination v-if="total > pageSize" :current-page="currentPage" :total="total" :page-size="pageSize" @change="(p) => $emit('pageChange', p)" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CommentItem from './CommentItem.vue'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Pagination from '@/components/common/Pagination.vue'
import type { Comment } from '@/types/comment'

const props = defineProps<{ list: Comment[]; loading: boolean; total: number; currentPage: number; pageSize: number; articleAuthorId?: number }>()
defineEmits<{ reply: [commentId: number, username: string]; delete: [commentId: number]; pageChange: [page: number] }>()

interface CommentNode extends Comment {
  children: CommentNode[]
}

const tree = computed(() => {
  const map = new Map<number, CommentNode>()
  const roots: CommentNode[] = []

  for (const c of props.list) {
    map.set(c.id, { ...c, children: [] })
  }

  for (const node of map.values()) {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId)!.children.push(node)
    } else {
      roots.push(node)
    }
  }

  return roots
})
</script>