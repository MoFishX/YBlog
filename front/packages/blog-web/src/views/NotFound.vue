<template>
  <div class="container mx-auto px-4 py-16 text-center">
    <h1 class="text-6xl font-bold text-gray-300 mb-4">404</h1>
    <p class="text-gray-500 mb-6">页面不存在</p>
    <RouterLink to="/" class="text-blue-600 hover:underline">返回首页</RouterLink>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>
