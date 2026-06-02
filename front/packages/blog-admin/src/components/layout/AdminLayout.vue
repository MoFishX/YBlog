<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="logo-area">
        <h2 class="text-white text-lg font-bold text-center py-4">Blog Admin</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/">
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="/articles">
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/comments">
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/tags">
          <span>标签管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="flex items-center justify-between h-full px-4">
          <Breadcrumb />
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-600">{{ userStore.user?.username }}</span>
            <el-button size="small" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'
import Breadcrumb from './Breadcrumb.vue'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/articles')) return '/articles'
  if (path.startsWith('/users')) return '/users'
  if (path.startsWith('/comments')) return '/comments'
  if (path.startsWith('/tags')) return '/tags'
  return '/'
})

async function handleLogout() {
  try {
    await authService.logout()
  } catch {
    // ignore
  }
  userStore.logout()
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-aside {
  background-color: #304156;
}
.admin-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}
.admin-main {
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}
.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
