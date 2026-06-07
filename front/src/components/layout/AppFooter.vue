<template>
  <footer class="border-t border-gray-100 bg-white mt-12">
    <div class="container mx-auto px-4 py-10">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
        <div>
          <h3 class="text-base font-semibold text-gray-900 mb-2">yvmouX Blog</h3>
          <p class="text-sm text-gray-500 leading-relaxed">一个分享技术与思考的博客平台。</p>
        </div>
        <div>
          <h4 class="text-sm font-medium text-gray-700 mb-2">导航</h4>
          <div class="space-y-1.5">
            <RouterLink to="/" class="block text-sm text-gray-500 hover:text-gray-900 transition-colors">首页</RouterLink>
            <RouterLink to="/search" class="block text-sm text-gray-500 hover:text-gray-900 transition-colors">搜索文章</RouterLink>
          </div>
        </div>
        <div>
          <h4 class="text-sm font-medium text-gray-700 mb-2">技术栈</h4>
          <p class="text-sm text-gray-400 leading-relaxed">Vue 3 + TypeScript + Tailwind CSS + Pinia + Vue Router</p>
        </div>
      </div>
      <div class="border-t border-gray-50 pt-6 text-center text-sm text-gray-400">
        &copy; {{ year }} yvmouX Blog. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
const year = new Date().getFullYear()
</script>
