<template>
  <div class="animate-pulse space-y-4 p-2">
    <div v-for="n in rows" :key="n" class="flex gap-3">
      <div class="h-9 w-9 rounded-full bg-gray-100 flex-shrink-0"></div>
      <div class="flex-1 space-y-2">
        <div class="h-4 bg-gray-100 rounded w-3/4"></div>
        <div class="h-3 bg-gray-100 rounded w-full"></div>
        <div class="h-3 bg-gray-100 rounded w-5/6"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ rows?: number }>(), { rows: 4 })
</script>
