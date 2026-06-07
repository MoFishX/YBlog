<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="logo-area">
        <span class="text-white text-base font-semibold">Reasonix Admin</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#1d1e2c"
        text-color="#8b8fa3"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/admin">
          <el-icon><HomeFilled /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="flex items-center justify-between h-full px-5">
          <Breadcrumb />
          <div class="flex items-center gap-3">
            <el-avatar :size="28" class="bg-gray-700 text-white text-xs font-medium">
              {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="text-sm text-gray-600">{{ userStore.user?.username }}</span>
            <el-divider direction="vertical" />
            <el-button text size="small" @click="handleLogout">退出</el-button>
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
import { useRoute, useRouter, RouterView } from 'vue-router'
import { HomeFilled, Document, User, ChatDotRound, PriceTag } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authService } from '@/services/authService'
import Breadcrumb from './Breadcrumb.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/articles')) return '/admin/articles'
  if (path.startsWith('/admin/users')) return '/admin/users'
  if (path.startsWith('/admin/comments')) return '/admin/comments'
  if (path.startsWith('/admin/tags')) return '/admin/tags'
  return '/admin'
})

async function handleLogout() {
  try { await authService.logout() } catch { /* ignore */ }
  userStore.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-aside {
  background-color: #1d1e2c;
  overflow: hidden;
}
.admin-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0;
  height: 56px;
}
.admin-main {
  background: #f5f6fa;
  min-height: calc(100vh - 56px);
}
.logo-area {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
</style>
