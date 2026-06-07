<template>
  <div class="article-list-page">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-semibold">文章管理</h2>
      <el-button type="primary" @click="$router.push('/admin/articles/editor')">新建文章</el-button>
    </div>

    <el-card class="mb-4">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable @change="fetchList">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="标题搜索" clearable @clear="fetchList" @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </el-form-item>
      </el-form>

      <div class="mb-3" v-if="hasSelection">
        <el-button type="danger" @click="handleBatchDelete">批量删除 ({{ selectedIds.length }})</el-button>
      </div>

      <div v-if="error" class="mb-4">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>

      <el-table
        :data="list"
        v-loading="loading"
        empty-text="暂无文章"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author.username" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PUBLISHED'" type="success">已发布</el-tag>
            <el-tag v-else-if="row.status === 'DRAFT'" type="info">草稿</el-tag>
            <el-tag v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/admin/articles/editor/${row.id}`)">编辑</el-button>
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
import { useBatchSelect } from '@/composables/admin/useBatchSelect'
import { articleService } from '@/services/admin/articleService'
import type { ArticleListItem } from '@/types/article'

const list = ref<ArticleListItem[]>([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = reactive({
  status: '',
  keyword: ''
})

const { selectedIds, hasSelection, handleSelectionChange, clearSelection } = useBatchSelect<ArticleListItem>()

async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    const res = await articleService.getList({
      page: page.value,
      pageSize: pageSize.value,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined
    })
    list.value = res.records
    total.value = res.total
    clearSelection()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await articleService.delete([id])
    await fetchList()
  } catch {
    // user canceled or failed
  }
}

async function handleBatchDelete() {
  try {
    await articleService.delete(selectedIds.value)
    await fetchList()
  } catch {
    // user canceled or failed
  }
}

onMounted(() => fetchList())
</script>
