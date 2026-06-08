<template>
  <div class="flex items-center justify-center gap-2 mt-12 mb-6">
    <button
      :disabled="currentPage <= 1" @click="$emit('change', currentPage - 1)"
      class="px-3 py-2 text-sm font-medium text-zinc-500 rounded-lg hover:bg-zinc-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors duration-200 cursor-pointer"
    >
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
    </button>

    <button
      v-for="p in displayedPages" :key="p"
      @click="typeof p === 'number' && $emit('change', p)"
      class="w-9 h-9 text-sm font-semibold rounded-lg transition-all duration-200"
      :class="p === currentPage
        ? 'bg-zinc-900 text-white shadow-sm'
        : p === '...'
        ? 'text-zinc-400 cursor-default'
        : 'text-zinc-600 hover:bg-zinc-100 cursor-pointer'"
      :disabled="p === '...'"
    >
      {{ p }}
    </button>

    <button
      :disabled="currentPage >= totalPages" @click="$emit('change', currentPage + 1)"
      class="px-3 py-2 text-sm font-medium text-zinc-500 rounded-lg hover:bg-zinc-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors duration-200 cursor-pointer"
    >
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{ currentPage: number; total: number; pageSize?: number }>(), { pageSize: 10 })
defineEmits<{ change: [page: number] }>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const displayedPages = computed(() => {
  const pages: (number | string)[] = []
  const t = totalPages.value
  const c = props.currentPage
  if (t <= 7) { for (let i = 1; i <= t; i++) pages.push(i) }
  else {
    pages.push(1)
    if (c > 3) pages.push('...')
    for (let i = Math.max(2, c - 1); i <= Math.min(t - 1, c + 1); i++) pages.push(i)
    if (c < t - 2) pages.push('...')
    pages.push(t)
  }
  return pages
})
</script>
