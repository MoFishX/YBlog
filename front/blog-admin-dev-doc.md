# 博客后台 (blog-admin) — 前端开发文档

> 基于 Vue3 + Vite + Element Plus + ECharts 的博客后台管理工程，面向运营/管理员，侧重表单、表格、弹窗等复杂交互效率

---

## 一、技术栈

| 类别        | 选型                          | 说明                                             |
| ----------- | ----------------------------- | ------------------------------------------------ |
| 框架        | Vue 3 (Composition API)       | `<script setup lang="ts">` 风格                  |
| 构建工具    | Vite 5                        | 秒级 HMR，ESBuild 预构建                         |
| UI 组件库   | **Element Plus**               | 企业级中后台组件库，丰富表单/表格/弹窗组件        |
| HTTP 客户端 | Axios                         | 请求/响应拦截器、Token 自动携带                  |
| 路由        | Vue Router 4                  | 路由守卫 + 角色权限控制                          |
| 状态管理    | Pinia                         | 管理员登录态、数据缓存等全局状态                 |
| Markdown    | `@kangc/v-md-editor`          | 文章编辑 / 预览 / 语法高亮                       |
| 图表        | **ECharts**                    | Dashboard 数据统计可视化                          |
| 工具库      | dayjs                         | 时间格式化                                       |
| 代码规范    | ESLint + Prettier             | 统一风格                                         |

> blog-web（博客前台）使用 Vue3 + TailwindCSS + shadcn-vue，独立工程，详见 `blog-web-dev-doc.md`。

---

## 二、项目目录结构

blog-admin 位于 Monorepo 的 `packages/blog-admin/`，与 `packages/shared/`（公共类型和工具）配合工作。

```
packages/blog-admin/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
├── .env.development
├── .env.production
├── public/
│   └── favicon.ico
└── src/
    ├── main.ts                  # 入口：挂载 App、注册 Element Plus
    ├── App.vue                  # 根组件
    ├── router/
    │   └── index.ts             # 路由表 + 导航守卫（含角色权限）
    ├── stores/
    │   ├── user.ts              # 管理员登录态
    │   └── app.ts               # 全局 UI 状态（侧栏折叠等）
    ├── api/
    │   ├── request.ts           # Axios 实例 + 拦截器（包含 ElMessage 错误提示）
    │   ├── modules/
    │   │   ├── auth.ts          # 管理员登录 API
    │   │   ├── article.ts       # 文章管理 API（含审核、批量操作）
    │   │   ├── comment.ts       # 评论管理 API
    │   │   ├── user.ts          # 用户管理 API
    │   │   ├── tag.ts           # 标签/分类管理 API
    │   │   └── dashboard.ts     # 统计面板 API
    │   └── index.ts
    ├── services/
    │   ├── articleService.ts    # 文章管理业务（批量操作、审核流程）
    │   ├── userService.ts       # 用户管理业务（封禁、角色变更）
    │   ├── commentService.ts    # 评论管理业务
    │   ├── dashboardService.ts  # 数据聚合与缓存
    │   └── tagService.ts        # 标签管理业务
    ├── views/
    │   ├── Dashboard.vue        # 数据统计面板（ECharts 图表）
    │   ├── ArticleList.vue      # 文章管理列表（筛选、审核、批量操作）
    │   ├── ArticleEditor.vue    # 新建 / 编辑文章（Markdown 编辑器）
    │   ├── UserList.vue         # 用户管理列表（封禁、角色变更）
    │   ├── CommentList.vue      # 评论管理列表
    │   ├── TagManage.vue        # 标签/分类管理
    │   ├── LoginView.vue        # 管理员登录页
    │   └── NotFound.vue         # 404
    ├── components/
    │   ├── layout/
    │   │   ├── AdminLayout.vue  # 管理后台布局（侧栏 + 顶栏 + 内容区）
    │   │   ├── SideMenu.vue     # 侧边菜单（el-menu）
    │   │   └── Breadcrumb.vue   # 面包屑导航
    │   ├── article/
    │   │   ├── ArticleTable.vue # 文章表格（el-table）
    │   │   └── ArticleFilter.vue # 筛选条件表单
    │   ├── user/
    │   │   └── UserTable.vue    # 用户表格
    │   └── common/
    │       ├── StatCard.vue     # 统计卡片
    │       └── ConfirmDialog.vue # 二次确认弹窗
    ├── composables/
    │   ├── useAuth.ts
    │   ├── usePagination.ts
    │   └── useBatchSelect.ts    # 批量选择逻辑（el-table 多选）
    ├── utils/
    │   ├── format.ts
    │   ├── storage.ts
    │   └── constants.ts
    └── types/
        ├── user.ts
        ├── article.ts
        ├── dashboard.ts         # 统计数据类型
        └── api.ts
```

**公共模块 (`packages/shared/`)**：存放 blog-admin 和 blog-web 共享的类型定义和工具函数，通过 `@shared` 别名引用。

```
packages/shared/
├── package.json
└── src/
    ├── types/
    │   ├── api.ts               # ApiResponse<T>、PageResult<T>
    │   ├── user.ts              # 共享 User 定义
    │   └── article.ts           # 共享 Article 定义
    └── utils/
        ├── storage.ts
        └── format.ts
```

---

## 三、环境搭建

### 3.1 前置要求

- Node.js ≥ 18
- pnpm ≥ 8

### 3.2 初始化项目

```bash
# 在 Monorepo 根目录下：
pnpm create vite packages/blog-admin --template vue-ts
cd packages/blog-admin
pnpm add vue-router@4 pinia axios element-plus @kangc/v-md-editor dayjs echarts
pnpm add -D sass @types/node unplugin-vue-components unplugin-auto-import
```

### 3.3 Vite 配置 (`vite.config.ts`)

```ts
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue(),
      AutoImport({ resolvers: [ElementPlusResolver()] }),
      Components({ resolvers: [ElementPlusResolver()] })
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
        '@shared': resolve(__dirname, '../shared/src')
      }
    },
    server: {
      port: 5174,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL,
          changeOrigin: true
        }
      }
    }
  }
})
```

### 3.4 环境变量

`.env.development`
```
VITE_API_BASE_URL=http://localhost:8080
```

`.env.production`
```
VITE_API_BASE_URL=https://api.yourblog.com
```

---

## 四、核心模块设计

### 4.1 服务抽象层（Service Layer）

组件/Store 不直接调用 API 模块，而是通过 Service 层完成业务逻辑组装。三层架构如下：

```
┌──────────────────────────────────────┐
│  View / Component / Store            │  ← 只关注 UI 交互与渲染
├──────────────────────────────────────┤
│  Service 层  (业务逻辑抽象)            │  ← 批量操作流程、数据转换、错误处理
│  - articleService / userService 等   │    审核状态机、缓存策略
├──────────────────────────────────────┤
│  API 层  (纯 HTTP 调用)               │  ← 与后端接口一一对应
│  - api/modules/article.ts 等         │    只关心请求参数和返回数据
└──────────────────────────────────────┘
```

| 层级 | 职责 | 关注点 |
| --- | --- | --- |
| **View / Store** | 页面渲染、交互逻辑、状态管理 | UI 状态（loading/error/empty）、Element Plus 事件 |
| **Service** | 业务逻辑组装、数据转换、缓存管理、异常降级 | 审核流程、批量操作、数据一致性 |
| **API** | 纯 HTTP 请求封装 | 接口地址、请求方式、参数/返回值类型 |

#### Service 层示例 (`src/services/articleService.ts`)

```ts
import { articleApi } from '@/api/modules/article'
import { ElMessage } from 'element-plus'
import type { Article, ArticleQuery, PageResult } from '@shared/types/article'

export const articleService = {
  // 文章列表（管理后台需要支持状态筛选）
  async getList(params: ArticleQuery): Promise<PageResult<Article>> {
    const res = await articleApi.getList(params)
    return res.data
  },

  // 发布文章 + 清缓存
  async create(data: ArticleFormData): Promise<Article> {
    const res = await articleApi.create(data)
    ElMessage.success('文章发布成功')
    return res.data
  },

  // 更新文章
  async update(id: number, data: Partial<Article>): Promise<void> {
    await articleApi.update(id, data)
    ElMessage.success('文章更新成功')
  },

  // 删除文章（含二次确认，实际确认逻辑由组件控制）
  async delete(id: number): Promise<void> {
    await articleApi.delete(id)
    ElMessage.success('文章已删除')
  },

  // 批量删除
  async batchDelete(ids: number[]): Promise<void> {
    await articleApi.batchDelete(ids)
    ElMessage.success(`已删除 ${ids.length} 篇文章`)
  },

  // 审核文章
  async review(id: number, status: 'APPROVED' | 'REJECTED', reason?: string): Promise<void> {
    await articleApi.review(id, status)
    ElMessage.success(status === 'APPROVED' ? '审核通过' : '已驳回')
  }
}
```

#### 组件调用对比

```vue
<!-- 之前：组件直接调 API，手动处理消息 -->
<script setup lang="ts">
import { articleApi } from '@/api/modules/article'
import { ElMessage } from 'element-plus'

async function handleDelete(id: number) {
  try {
    await articleApi.delete(id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<!-- 之后：组件通过 Service 调用 -->
<script setup lang="ts">
import { articleService } from '@/services/articleService'

async function handleDelete(id: number) {
  await articleService.delete(id)
  await fetchList()   // Service 层已处理消息提示
}
</script>
```

### 4.2 Axios 封装 (`src/api/request.ts`)

```ts
import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 — 自动携带 JWT
instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截器 — 数据解包 + 统一错误提示
instance.interceptors.response.use(
  (response) => response.data,           // 解包外层 data
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default instance
```

### 4.3 路由设计

| 路径                      | 名称           | 组件             | 权限     | 说明             |
| ------------------------- | -------------- | ---------------- | -------- | ---------------- |
| `/`                       | Dashboard      | `Dashboard`      | ADMIN    | 数据统计面板     |
| `/articles`               | ArticleList    | `ArticleList`    | ADMIN    | 文章管理列表     |
| `/articles/editor`        | ArticleNew     | `ArticleEditor`  | ADMIN    | 新建文章         |
| `/articles/editor/:id`    | ArticleEdit    | `ArticleEditor`  | ADMIN    | 编辑文章         |
| `/users`                  | UserList       | `UserList`       | ADMIN    | 用户管理         |
| `/comments`               | CommentList    | `CommentList`    | ADMIN    | 评论管理         |
| `/tags`                   | TagManage      | `TagManage`      | ADMIN    | 标签/分类管理    |
| `/login`                  | Login          | `LoginView`      | 未登录   | 管理员登录       |
| `/:pathMatch(.*)`         | NotFound       | `NotFound`       | 公开     | 404              |

**路由守卫（含角色校验）：**

```ts
// src/router/index.ts
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  // 需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 需要 Admin 权限（blog-admin 特有）
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next({ name: 'NotFound' })
  }

  // 已登录时访问登录页 → 重定向到 Dashboard
  if (to.name === 'Login' && userStore.isLoggedIn) {
    return next({ name: 'Dashboard' })
  }

  next()
})
```

### 4.4 Pinia 状态管理

#### 用户 Store (`src/stores/user.ts`)

```ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import { storage } from '@/utils/storage'
import type { User } from '@shared/types/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string>(storage.get('admin_token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(username: string, password: string) {
    const res = await authService.login({ username, password })
    token.value = res.token
    user.value = res.user
    storage.set('admin_token', res.token)
  }

  function logout() {
    token.value = ''
    user.value = null
    storage.remove('admin_token')
    authService.clearCache()
  }

  return { user, token, isLoggedIn, isAdmin, login, logout }
})
```

> blog-admin 的登录态使用独立的 `admin_token` key，与 blog-web 的 Token 区分，避免冲突。

### 4.5 API 模块示例 (`src/api/modules/article.ts`)

```ts
import request from '../request'
import type { ApiResponse, PageResult } from '@shared/types/api'
import type { Article, ArticleQuery } from '@shared/types/article'

export const articleApi = {
  getList(params: ArticleQuery): Promise<ApiResponse<PageResult<Article>>> {
    return request.get('/articles', { params })
  },

  getDetail(id: number): Promise<ApiResponse<Article>> {
    return request.get(`/articles/${id}`)
  },

  create(data: { title: string; content: string; tags: number[] }): Promise<ApiResponse<Article>> {
    return request.post('/articles', data)
  },

  update(id: number, data: Partial<Article>): Promise<ApiResponse<null>> {
    return request.put(`/articles/${id}`, data)
  },

  delete(id: number): Promise<ApiResponse<null>> {
    return request.delete(`/articles/${id}`)
  },

  batchDelete(ids: number[]): Promise<ApiResponse<null>> {
    return request.post('/articles/batch-delete', { ids })
  },

  review(id: number, status: 'APPROVED' | 'REJECTED'): Promise<ApiResponse<null>> {
    return request.put(`/articles/${id}/review`, { status })
  }
}
```

**Dashboard API (`src/api/modules/dashboard.ts`)**:

```ts
import request from '../request'
import type { ApiResponse } from '@shared/types/api'
import type { DashboardStats, TrendData } from '@/types/dashboard'

export const dashboardApi = {
  getStats(): Promise<ApiResponse<DashboardStats>> {
    return request.get('/dashboard/stats')
  },

  getWeeklyTrend(): Promise<ApiResponse<TrendData[]>> {
    return request.get('/dashboard/weekly-trend')
  }
}
```

### 4.6 类型定义

```ts
// @shared/types/api.ts
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

// @shared/types/article.ts
export interface Article {
  id: number
  title: string
  content: string
  htmlContent?: string
  summary: string
  authorId: number
  authorName: string
  status: 'DRAFT' | 'PUBLISHED' | 'REVIEWING' | 'REJECTED'
  viewCount: number
  likeCount: number
  tags: Tag[]
  createdAt: string
  updatedAt: string
}

export interface ArticleQuery {
  page: number
  pageSize: number
  tagId?: number
  keyword?: string
  status?: string    // 管理后台筛选用：DRAFT / PUBLISHED / REVIEWING
}

// @shared/types/user.ts
export interface User {
  id: number
  username: string
  avatar?: string
  role: 'USER' | 'ADMIN'
  status?: 'ACTIVE' | 'BANNED'
  createdAt?: string
}

// @shared/types/comment.ts
export interface Comment {
  id: number
  articleId: number
  userId: number
  username: string
  avatar?: string
  content: string
  createdAt: string
}

// src/types/dashboard.ts（blog-admin 特有）
export interface DashboardStats {
  totalArticles: number
  totalUsers: number
  totalComments: number
  todayViews: number
}

export interface TrendData {
  date: string
  count: number
}
```

---

## 五、关键功能实现要点

### 5.1 Markdown 编辑器（文章编写）

使用 `@kangc/v-md-editor`，支持实时预览和语法高亮：

```vue
<!-- src/views/ArticleEditor.vue -->
<template>
  <div class="editor-page">
    <el-input v-model="form.title" placeholder="文章标题" size="large" />
    <div class="editor-meta">
      <TagSelector v-model="form.tags" />
      <el-select v-model="form.status" placeholder="状态">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="发布" value="PUBLISHED" />
      </el-select>
    </div>
    <v-md-editor v-model="form.content" height="calc(100vh - 280px)" />
    <div class="actions">
      <el-button @click="saveDraft">保存草稿</el-button>
      <el-button type="primary" @click="publish">发布文章</el-button>
    </div>
  </div>
</template>
```

**草稿箱实现（Service 层）：**

```ts
// src/services/articleService.ts
async function saveDraft(data: ArticleFormData) {
  // 先存本地，防止意外关闭
  storage.set(`draft_${data.id ?? 'new'}`, JSON.stringify(data))
  await articleApi.saveDraft(data)
}
```

### 5.2 Dashboard 数据统计

使用 ECharts 实现折线图/柱状图展示趋势数据：

```vue
<!-- src/views/Dashboard.vue -->
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <StatCard :label="stat.label" :value="stat.value" :trend="stat.trend" />
      </el-col>
    </el-row>
    <el-card class="chart-card">
      <template #header>近 7 天访问趋势</template>
      <div ref="chartRef" style="height: 400px" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { dashboardService } from '@/services/dashboardService'

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

onMounted(async () => {
  const data = await dashboardService.getWeeklyTrend()
  chart = echarts.init(chartRef.value!)
  chart.setOption({ /* 折线图配置 */ })
})

onBeforeUnmount(() => chart?.dispose())
</script>
```

**Dashboard Service：**

```ts
// src/services/dashboardService.ts
import { dashboardApi } from '@/api/modules/dashboard'
import type { DashboardStats, TrendData } from '@/types/dashboard'

export const dashboardService = {
  async getStats(): Promise<DashboardStats> {
    const res = await dashboardApi.getStats()
    return res.data
  },

  async getWeeklyTrend(): Promise<TrendData[]> {
    const res = await dashboardApi.getWeeklyTrend()
    return res.data
  }
}
```

### 5.3 文章管理列表（含筛选和批量操作）

使用 Element Plus `el-table` + `el-pagination`，配合 `useBatchSelect` composable 实现批量选中：

```vue
<template>
  <div class="article-manage">
    <ArticleFilter v-model="filters" @search="fetchList" />
    <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
      批量删除 ({{ selectedIds.length }})
    </el-button>
    <el-table :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="authorName" label="作者" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editArticle(row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteArticle(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" @change="fetchList" />
  </div>
</template>
```

### 5.4 审核流程（状态机设计）

Service 层封装审核状态转换逻辑：

```ts
// src/services/articleService.ts
const STATUS_TRANSITIONS: Record<string, string[]> = {
  'DRAFT':      ['PUBLISHED', 'DELETED'],
  'PUBLISHED':  ['REVIEWING', 'ARCHIVED'],
  'REVIEWING':  ['APPROVED', 'REJECTED'],
}

export function canTransitionTo(current: string, target: string): boolean {
  return STATUS_TRANSITIONS[current]?.includes(target) ?? false
}

async function review(id: number, status: 'APPROVED' | 'REJECTED', reason?: string) {
  await articleApi.review(id, status)
  ElMessage.success(status === 'APPROVED' ? '审核通过' : '已驳回')
}
```

### 5.5 用户管理

```ts
// src/services/userService.ts
import { userApi } from '@/api/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'

export const userService = {
  async getList(params: UserQuery): Promise<PageResult<User>> {
    const res = await userApi.getList(params)
    return res.data
  },

  async banUser(id: number): Promise<void> {
    await ElMessageBox.confirm('确认封禁该用户？封禁后用户将无法登录。', '警告', {
      type: 'warning'
    })
    await userApi.ban(id)
    ElMessage.success('用户已封禁')
  },

  async changeRole(id: number, role: 'USER' | 'ADMIN'): Promise<void> {
    await userApi.changeRole(id, role)
    ElMessage.success('角色已更新')
  }
}
```

### 5.6 权限控制

| 层级     | 实现方式                                                    |
| -------- | ----------------------------------------------------------- |
| 路由层   | `meta.requiresAuth` + `meta.requiresAdmin` + `beforeEach`   |
| 组件层   | `v-if="userStore.isAdmin"` 控制管理功能入口                   |
| 按钮层   | `v-if="hasPermission('article:delete')"` 细粒度操作权限       |
| API 层   | 后端接口本身校验角色，前端权限控制仅为 UX 优化，不可替代后端校验 |

### 5.7 响应式布局

- Element Plus `el-row` / `el-col` 栅格系统
- 侧栏可折叠，使用 Element Plus `el-menu` collapse 模式
- 表格在小屏幕下 `el-table` 原生支持横向滚动
- Dashboard 统计卡片在移动端 `:span` 调整为 `24`（全宽）

---

## 六、性能优化

| 优化项                 | 方案                                                               |
| ---------------------- | ------------------------------------------------------------------ |
| **路由懒加载**         | `() => import('@/views/ArticleEditor.vue')`                        |
| **组件异步加载**       | `defineAsyncComponent` 用于非首屏组件                               |
| **Element Plus 按需**  | `unplugin-vue-components` + `unplugin-auto-import` 自动按需引入      |
| **ECharts 按需**       | 只注册需要的图表类型（line、bar），减小构建体积                       |
| **Dashboard 数据缓存** | Service 层缓存统计数据 5 分钟，避免频繁请求                           |

---

## 七、构建与部署

### 7.1 构建命令

```bash
pnpm --filter blog-admin build      # 生产构建，输出到 packages/blog-admin/dist/
pnpm --filter blog-admin preview    # 本地预览构建结果
```

### 7.2 Nginx 配置

```nginx
server {
    listen 80;
    server_name yourblog.com;

    # 博客前台（主域名）
    location / {
        root /usr/share/nginx/html/blog-web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # blog-admin 后台管理（子路径）
    location /admin/ {
        alias /usr/share/nginx/html/blog-admin/;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    # API 反向代理（共用后端）
    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

> **Vite 构建配置补充**：blog-admin 的 `vite.config.ts` 需设置 `base: '/admin/'` 以匹配 Nginx 子路径部署。

### 7.3 Docker 部署

```dockerfile
# docker/Dockerfile.admin
FROM node:20-alpine AS build
WORKDIR /app
COPY packages/blog-admin/ packages/blog-admin/
COPY packages/shared/ packages/shared/
COPY pnpm-workspace.yaml pnpm-lock.yaml package.json ./
RUN corepack enable && pnpm install --frozen-lockfile
RUN pnpm --filter blog-admin build

FROM nginx:alpine
COPY --from=build /app/packages/blog-admin/dist/ /usr/share/nginx/html/blog-admin/
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## 八、开发规范

### 8.1 命名约定

| 类型        | 规范                            | 示例                          |
| ----------- | ------------------------------- | ----------------------------- |
| 组件文件    | PascalCase                      | `ArticleTable.vue`            |
| 视图文件    | PascalCase                      | `Dashboard.vue`               |
| Composables | camelCase + use 前缀            | `useBatchSelect.ts`           |
| Store       | camelCase                       | `user.ts`                     |
| API 模块    | camelCase                       | `article.ts`、`dashboard.ts`  |
| Service     | camelCase + Service 后缀        | `articleService.ts`           |
| CSS class   | BEM 或 kebab-case               | `.article-manage__filter`     |
| 类型/接口   | PascalCase                      | `DashboardStats`、`TrendData` |

### 8.2 Service 层开发约定

- 请求成功后的用户提示（`ElMessage.success`）统一在 Service 层处理，组件无需重复
- 二次确认弹窗（`ElMessageBox.confirm`）在 Service 层处理，组件直接调用 Service 方法
- 数据转换（状态码映射中文、日期格式化）统一在 Service 层完成
- Service 层可引用 `ElMessage` / `ElMessageBox`，但不操作 DOM 或路由

### 8.3 Element Plus 使用规范

- 全局组件（`el-button`、`el-table` 等）通过 `unplugin-vue-components` 自动按需引入
- 消息/弹窗通过 API 方式调用：`ElMessage.success()`、`ElMessageBox.confirm()`
- 表单校验使用 Element Plus 内置的 `el-form` rules，不在组件中手写校验逻辑
- 表格列宽优先使用 `min-width` 而非固定 `width`

### 8.4 Git 提交规范

```
feat(blog-admin): 新增 Dashboard 统计面板
feat(blog-admin): 新增文章审核功能
fix(blog-admin): 修复用户列表分页错误
refactor(blog-admin): 提取批量操作到 useBatchSelect composable
style(blog-admin): 调整侧栏菜单样式
```

### 8.5 代码审查 Checklist

- [ ] 是否使用 `<script setup lang="ts">`
- [ ] API 调用是否通过 Service 层
- [ ] Service 层是否处理了 `ElMessage` 成功/失败提示
- [ ] 删除/封禁等危险操作是否有二次确认弹窗
- [ ] 表格是否有 loading 状态和空状态处理
- [ ] 批量操作是否处理了空选择状态（按钮置灰）
- [ ] Token 过期是否正确跳转登录页
- [ ] ECharts 实例是否在 `onBeforeUnmount` 中正确销毁
- [ ] 公共类型/工具是否放在 shared 包而非重复定义

---

## 九、FAQ

**Q: 为什么 blog-admin 单独使用一套 UI 库（Element Plus）？**
A: 管理后台需要大量表单、表格、弹窗、下拉菜单等复杂交互组件，Element Plus 提供了成熟的企业级解决方案。博客前台追求简洁美观，选用 Tailwind + shadcn-vue。不同场景选不同工具是正确的架构决策。

**Q: Service 层和 API 层的边界在哪里？**
A: API 层是"怎么请求"，Service 层是"请求什么 + 怎么处理"。API 层只关心 HTTP 方法、URL、参数；Service 层关心审核流程、批量操作、错误提示、数据转换等业务逻辑。

**Q: 如何引用 shared 的类型？**
A: 通过 Vite alias：`'@shared': resolve(__dirname, '../shared/src')`，然后 `import type { ApiResponse } from '@shared/types/api'`。

**Q: blog-admin 和 blog-web 能否共用登录态？**
A: 建议隔离：blog-admin 使用独立的 `admin_token` key，仅管理员可登录后台。如果部署在同一域名下，两者 `localStorage` 共享，通过不同的 key 区分。

**Q: ECharts 图表如何按需加载？**
A: `import * as echarts from 'echarts/core'` 只导入核心，然后按需注册 `LineChart`、`BarChart` 等需要的图表类型和组件。

**Q: 如何处理跨域？**
A: 开发环境通过 Vite proxy；生产环境 Nginx 反向代理，不存在跨域。

---

## 十、参考资源

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)
- [@kangc/v-md-editor](https://code-farmer-i.github.io/vue-markdown-editor/zh/)
- [ECharts 文档](https://echarts.apache.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [unplugin-vue-components](https://github.com/unplugin/unplugin-vue-components)
