<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-if="items.length" v-for="item in items" :key="item">{{ item }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const items = computed(() => {
  const map: Record<string, string> = {
    articles: '文章管理',
    users: '用户管理',
    comments: '评论管理',
    tags: '标签管理'
  }
  const seg = route.path.split('/').filter(Boolean)
  return seg.map((s) => map[s] || s)
})
</script>
