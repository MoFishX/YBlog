<template>
  <div v-if="loading" class="animate-pulse space-y-4 p-6">
    <div v-for="n in rows" :key="n" class="flex gap-4">
      <div class="h-4 bg-gray-200 rounded w-1/4"></div>
      <div class="h-4 bg-gray-200 rounded flex-1"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  loading: boolean
  rows?: number
}>(), {
  rows: 4
})
</script>
