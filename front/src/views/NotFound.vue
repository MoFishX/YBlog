<template>
  <div class="container mx-auto px-4 py-32 text-center">
    <h1 class="text-8xl font-bold text-zinc-200 mb-4 font-serif">404</h1>
    <p class="text-zinc-500 mb-10">页面不存在或已被移除</p>
    <RouterLink to="/" class="inline-block px-6 py-3 bg-zinc-900 text-white text-sm font-semibold rounded-lg hover:bg-zinc-800 transition-colors duration-200">
      返回首页
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>
