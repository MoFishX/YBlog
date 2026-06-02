<template>
  <div class="flex items-center justify-center gap-2 mt-8 mb-4">
    <button
      :disabled="currentPage <= 1"
      @click="$emit('change', currentPage - 1)"
      class="px-3 py-1.5 text-sm rounded border border-gray-300 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-100 transition-colors"
    >
      上一页
    </button>
    <span class="text-sm text-gray-600">
      第 {{ currentPage }} / {{ totalPages }} 页
    </span>
    <button
      :disabled="currentPage >= totalPages"
      @click="$emit('change', currentPage + 1)"
      class="px-3 py-1.5 text-sm rounded border border-gray-300 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-100 transition-colors"
    >
      下一页
    </button>
    <span class="text-sm text-gray-400">共 {{ total }} 条</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  currentPage: number
  total: number
  pageSize?: number
}>(), {
  pageSize: 10
})

defineEmits<{
  change: [page: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
</script>
