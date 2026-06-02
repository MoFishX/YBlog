<template>
  <div class="flex flex-col items-center justify-center py-16 text-gray-400">
    <svg class="w-16 h-16 mb-4 opacity-30" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
    </svg>
    <p class="text-sm">{{ text }}</p>
    <button v-if="showRetry" @click="$emit('retry')" class="mt-4 px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-md hover:bg-gray-50 transition-colors">
      重试
    </button>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ text: string; showRetry?: boolean }>(), { showRetry: false })
defineEmits<{ retry: [] }>()
</script>
