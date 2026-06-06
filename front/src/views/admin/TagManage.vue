<template>
  <div class="tag-manage-page">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-semibold">标签管理</h2>
      <el-button type="primary" @click="showCreate = true">新建标签</el-button>
    </div>

    <el-card>
      <div v-if="error" class="mb-4">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>

      <el-table :data="list" v-loading="loading" empty-text="暂无标签" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="articleCount" label="文章数" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreate" title="新建标签" width="400px">
      <el-input v-model="createName" placeholder="标签名称" maxlength="30" />
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEdit" title="编辑标签" width="400px">
      <el-input v-model="editName" placeholder="标签名称" maxlength="30" />
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleEdit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tagService } from '@/services/admin/tagService'
import type { Tag } from '@/types/article'

const list = ref<Tag[]>([])
const loading = ref(false)
const error = ref('')
const submitting = ref(false)

const showCreate = ref(false)
const createName = ref('')

const showEdit = ref(false)
const editName = ref('')
const editingId = ref(0)

async function fetchList() {
  loading.value = true
  error.value = ''
  try {
    list.value = await tagService.getList()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!createName.value.trim()) return
  submitting.value = true
  try {
    await tagService.create(createName.value.trim())
    showCreate.value = false
    createName.value = ''
    await fetchList()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '创建失败'
  } finally {
    submitting.value = false
  }
}

function openEdit(row: Tag) {
  editingId.value = row.id
  editName.value = row.name
  showEdit.value = true
}

async function handleEdit() {
  if (!editName.value.trim()) return
  submitting.value = true
  try {
    await tagService.update(editingId.value, editName.value.trim())
    showEdit.value = false
    await fetchList()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '更新失败'
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await tagService.delete(id)
    await fetchList()
  } catch {
    // user canceled or failed
  }
}

onMounted(() => fetchList())
</script>
