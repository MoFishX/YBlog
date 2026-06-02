<template>
  <el-card class="stat-card" shadow="hover">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm text-gray-500">{{ label }}</p>
        <p class="text-2xl font-bold text-gray-900 mt-1">{{ value }}</p>
      </div>
      <div class="w-12 h-12 rounded-lg flex items-center justify-center" :style="{ background: color }">
        <span class="text-white text-xl">{{ icon }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  icon?: string
  color?: string
}>()
</script>
