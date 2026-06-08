<template>
  <div class="user-list-page">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-semibold">用户管理</h2>
    </div>

    <el-card>
      <el-form :inline="true" class="mb-4">
        <el-form-item label="搜索">
          <el-input v-model="keyword" placeholder="用户名" clearable @clear="fetchList" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </el-form-item>
      </el-form>

      <div v-if="error" class="mb-4">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>

      <el-table :data="list" v-loading="loading" empty-text="暂无用户" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="文章数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt || '') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleToggleStatus(row)">
              {{ toggleLabel(row.status) }}
            </el-button>
            <el-button size="small" @click="handleChangeRole(row)">
              {{ row.role === 'ADMIN' ? '降为用户' : '升管理员' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { formatDate } from '@/utils/format'
import { userService } from '@/services/admin/userService'
import type { User } from '@/types/user'

const list = ref<User[]>([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const keyword = ref('')

function statusTagType(status: string): string {
  const map: Record<string, string> = { ACTIVE: 'success', INACTIVE: 'warning', BANNED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { ACTIVE: '正常', INACTIVE: '未激活', BANNED: '已封禁' }
  return map[status] || status
}

function toggleLabel(status: string): string {
  const map: Record<string, string> = { ACTIVE: '封禁', INACTIVE: '激活', BANNED: '解封' }
  return map[status] || '操作'
}

async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    const res = await userService.getList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    list.value = res.records
    total.value = res.total
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(row: User) {
  try {
    await userService.toggleStatus(row.id, row.status || 'ACTIVE')
    await fetchList()
  } catch {
    // user canceled or failed
  }
}

async function handleChangeRole(row: User) {
  const newRole = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  try {
    await userService.changeRole(row.id, newRole)
    await fetchList()
  } catch {
    // user canceled or failed
  }
}

onMounted(() => fetchList())
</script>
