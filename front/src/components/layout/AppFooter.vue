<template>
  <footer class="border-t border-zinc-100 bg-white mt-16">
    <div class="container mx-auto px-4 py-12">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
        <div>
          <h3 class="text-base font-semibold text-zinc-900 mb-3 font-serif">yvmouX Blog</h3>
          <p class="text-sm text-zinc-500 leading-relaxed">一个分享技术与思考的博客平台。</p>
        </div>
        <div>
          <h4 class="text-sm font-semibold text-zinc-700 mb-3 uppercase tracking-wider">导航</h4>
          <div class="space-y-2">
            <RouterLink to="/" class="block text-sm text-zinc-500 hover:text-accent transition-colors duration-200">首页</RouterLink>
            <RouterLink to="/search" class="block text-sm text-zinc-500 hover:text-accent transition-colors duration-200">搜索文章</RouterLink>
            <RouterLink to="/tags" class="block text-sm text-zinc-500 hover:text-accent transition-colors duration-200">标签浏览</RouterLink>
          </div>
        </div>
        <div>
          <h4 class="text-sm font-semibold text-zinc-700 mb-3 uppercase tracking-wider">友情链接</h4>
          <div class="space-y-2">
            <a href="https://github.com/MoFishX" target="_blank" rel="noopener" class="block text-sm text-zinc-500 hover:text-accent transition-colors duration-200">MoFishX</a>
          </div>
        </div>
      </div>
      <div class="border-t border-zinc-100 pt-6 text-center text-sm text-zinc-400">
        &copy; {{ year }} yvmouX Blog. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
const year = new Date().getFullYear()
</script>
