<template>
  <div>
    <LoadingSkeleton v-if="loading" :rows="6" />
    <EmptyState v-else-if="articles.length === 0" text="暂无文章" />
    <div v-else class="grid gap-6 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
      <ArticleCard v-for="item in articles" :key="item.id" :article="item" />
    </div>
  </div>
</template>

<script setup lang="ts">
import ArticleCard from './ArticleCard.vue'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import type { ArticleListItem } from '@/types/article'

defineProps<{ articles: ArticleListItem[]; loading: boolean }>()
</script>
