<template>
  <div class="comment-list-page">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-semibold">评论管理</h2>
    </div>

    <el-card>
      <el-form :inline="true" class="mb-4">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="评论内容搜索" clearable @clear="fetchList" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="文章ID">
          <el-input-number v-model="filters.articleId" :min="0" placeholder="按文章筛选" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </el-form-item>
      </el-form>

      <div v-if="error" class="mb-4">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>

      <el-table :data="list" v-loading="loading" empty-text="暂无评论" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="articleTitle" label="所属文章" width="180" show-overflow-tooltip />
        <el-table-column prop="user.username" label="用户" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/storage'
import { commentService } from '@/services/commentService'
import type { AdminComment } from '@/api/modules/comment'

const list = ref<AdminComment[]>([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = reactive({
  keyword: '',
  articleId: undefined as number | undefined
})

async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    const res = await commentService.getList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      articleId: filters.articleId || undefined
    })
    list.value = res.records
    total.value = res.total
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await commentService.delete(id)
    await fetchList()
  } catch {
    // user canceled or failed
  }
}

onMounted(() => fetchList())
</script>
