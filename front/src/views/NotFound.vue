<template>
  <div class="container mx-auto px-4 py-24 text-center">
    <h1 class="text-6xl font-bold text-gray-200 mb-4">404</h1>
    <p class="text-gray-500 mb-8">页面不存在或已被移除</p>
    <RouterLink to="/" class="inline-block px-5 py-2.5 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-800 transition-colors">
      返回首页
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>
