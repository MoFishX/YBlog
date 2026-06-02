# 博客前台 (blog-web) — 前端开发文档

> 基于 Vue3 + Vite + TailwindCSS + shadcn-vue 的博客前台工程，面向读者展示，追求轻量、美观、首屏加载快

---

## 一、技术栈

| 类别        | 选型                          | 说明                                             |
| ----------- | ----------------------------- | ------------------------------------------------ |
| 框架        | Vue 3 (Composition API)       | `<script setup lang="ts">` 风格                  |
| 构建工具    | Vite 5                        | 秒级 HMR，ESBuild 预构建                         |
| UI 组件库   | **TailwindCSS + shadcn-vue**   | 现代简洁风格，组件按需复制到项目中，高度可定制   |
| HTTP 客户端 | Axios                         | 请求/响应拦截器、Token 自动携带                  |
| 路由        | Vue Router 4                  | 路由守卫 + 权限控制                              |
| 状态管理    | Pinia                         | 用户态、文章缓存等全局状态                       |
| Markdown    | `@kangc/v-md-editor`          | 文章阅读页 Markdown 渲染                         |
| 工具库      | dayjs + @vueuse/core          | 时间格式化 / 通用 composable                     |
| 代码规范    | ESLint + Prettier             | 统一风格                                         |

> blog-admin（后台管理）使用 Vue3 + Element Plus + ECharts，独立工程，详见 `blog-admin-dev-doc.md`。

---

## 二、项目目录结构

blog-web 位于 Monorepo 的 `packages/blog-web/`，与 `packages/shared/`（公共类型和工具）配合工作。

```
packages/blog-web/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.ts
├── components.json              # shadcn-vue 配置文件
├── package.json
├── .env.development
├── .env.production
├── public/
│   └── favicon.ico
└── src/
    ├── main.ts                  # 入口：挂载 App、注册插件
    ├── App.vue                  # 根组件
    ├── router/
    │   └── index.ts             # 路由表 + 导航守卫
    ├── stores/
    │   ├── user.ts              # 用户登录态 / Token
    │   ├── article.ts           # 文章列表缓存 / 当前文章
    │   └── app.ts               # 全局 UI 状态（主题、侧栏折叠等）
    ├── api/
    │   ├── request.ts           # Axios 实例 + 拦截器
    │   ├── modules/
    │   │   ├── auth.ts          # 登录 / 注册 API
    │   │   ├── article.ts       # 文章 API
    │   │   ├── comment.ts       # 评论 API
    │   │   └── tag.ts           # 标签 API
    │   └── index.ts             # 统一导出 API 模块
    ├── services/
    │   ├── authService.ts       # 认证业务（登录态管理、Token 刷新策略）
    │   ├── articleService.ts    # 文章业务（缓存策略、乐观更新、数据组装）
    │   ├── commentService.ts    # 评论业务
    │   └── tagService.ts        # 标签业务
    ├── views/
    │   ├── HomeView.vue         # 首页（文章列表 + 热门排行）
    │   ├── ArticleDetail.vue    # 文章详情 + 评论区
    │   ├── SearchView.vue       # 搜索结果
    │   ├── LoginView.vue        # 登录页
    │   ├── RegisterView.vue     # 注册页
    │   ├── UserCenter.vue       # 个人中心
    │   └── NotFound.vue         # 404
    ├── components/
    │   ├── ui/                  # shadcn-vue 组件（Button、Card、Input 等）
    │   ├── layout/
    │   │   ├── AppHeader.vue    # 顶部导航
    │   │   ├── AppSidebar.vue   # 侧边栏
    │   │   └── AppFooter.vue    # 底部
    │   ├── article/
    │   │   ├── ArticleCard.vue  # 文章卡片
    │   │   ├── ArticleList.vue  # 文章列表
    │   │   └── HotRank.vue      # 热门排行榜
    │   ├── comment/
    │   │   ├── CommentList.vue  # 评论列表
    │   │   └── CommentItem.vue  # 单条评论
    │   ├── tag/
    │   │   └── TagSelector.vue  # 标签选择器
    │   └── common/
    │       ├── Pagination.vue   # 分页
    │       ├── EmptyState.vue   # 空状态
    │       └── LoadingSkeleton.vue # 骨架屏
    ├── composables/
    │   ├── useAuth.ts           # 登录态逻辑复用
    │   ├── usePagination.ts     # 分页逻辑复用
    │   └── useDebounce.ts       # 防抖
    ├── utils/
    │   ├── format.ts            # 日期/数字格式化
    │   ├── storage.ts           # localStorage 封装
    │   └── constants.ts         # 常量
    ├── types/
    │   ├── user.ts              # User 类型
    │   ├── article.ts           # Article 类型
    │   ├── comment.ts           # Comment 类型
    │   └── api.ts               # 通用响应类型
    └── styles/
        ├── main.css             # Tailwind 指令 + 全局样式
        └── variables.css        # CSS 变量（主题色等）
```

**公共模块 (`packages/shared/`)**：存放 blog-web 和 blog-admin 共享的类型定义和工具函数，通过 `@shared` 别名引用。

```
packages/shared/
├── package.json
└── src/
    ├── types/
    │   ├── api.ts               # ApiResponse<T>、PageResult<T>
    │   ├── user.ts              # 共享 User 定义
    │   └── article.ts           # 共享 Article 定义
    └── utils/
        ├── storage.ts           # 共享 localStorage 封装
        └── format.ts            # 共享日期格式化
```

---

## 三、环境搭建

### 3.1 前置要求

- Node.js ≥ 18
- pnpm ≥ 8

### 3.2 初始化项目

```bash
# 在 Monorepo 根目录下：
pnpm create vite packages/blog-web --template vue-ts
cd packages/blog-web
pnpm add vue-router@4 pinia axios @kangc/v-md-editor dayjs @vueuse/core
pnpm add -D tailwindcss @tailwindcss/vite

# shadcn-vue 初始化
npx shadcn-vue@latest init
```

### 3.3 Vite 配置 (`vite.config.ts`)

```ts
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue(), tailwindcss()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
        '@shared': resolve(__dirname, '../shared/src')
      }
    },
    server: {
      port: 5173,
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
│  Service 层  (业务逻辑抽象)            │  ← 缓存策略、数据转换、乐观更新
│  - articleService / authService 等   │    错误重试、数据组装
├──────────────────────────────────────┤
│  API 层  (纯 HTTP 调用)               │  ← 与后端接口一一对应
│  - api/modules/article.ts 等         │    只关心请求参数和返回数据
└──────────────────────────────────────┘
```

| 层级 | 职责 | 关注点 |
| --- | --- | --- |
| **View / Store** | 页面渲染、交互逻辑、状态管理 | UI 状态（loading/error/empty） |
| **Service** | 业务逻辑组装、数据转换、缓存管理、异常降级 | 业务规则、数据一致性、性能策略 |
| **API** | 纯 HTTP 请求封装 | 接口地址、请求方式、参数/返回值类型 |

#### Service 层示例 (`src/services/articleService.ts`)

```ts
import { articleApi } from '@/api/modules/article'
import { useArticleStore } from '@/stores/article'
import type { Article, ArticleQuery, PageResult } from '@shared/types/article'

const CACHE_TTL = 3 * 60 * 1000 // 文章列表缓存 3 分钟

export const articleService = {
  // 文章列表（带缓存）
  async getList(params: ArticleQuery): Promise<PageResult<Article>> {
    const store = useArticleStore()
    const cacheKey = JSON.stringify(params)

    const cached = store.listCache.get(cacheKey)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      return cached.data
    }

    const res = await articleApi.getList(params)
    store.listCache.set(cacheKey, { data: res.data, timestamp: Date.now() })
    return res.data
  },

  // 文章详情（实时拉取，不缓存）
  async getDetail(id: number): Promise<Article> {
    const res = await articleApi.getDetail(id)
    return res.data
  },

  // 点赞（乐观更新 + 失败回滚）
  async like(id: number): Promise<void> {
    const store = useArticleStore()
    store.incrementLike(id)
    try {
      await articleApi.like(id)
    } catch {
      store.decrementLike(id)
      throw new Error('点赞失败，请稍后重试')
    }
  },

  // 搜索（不缓存）
  async search(keyword: string, page: number): Promise<PageResult<Article>> {
    const res = await articleApi.search(keyword, page)
    return res.data
  }
}
```

#### 组件调用对比

```vue
<!-- 之前：组件直接调 API -->
<script setup lang="ts">
import { articleApi } from '@/api/modules/article'

const list = ref<Article[]>([])
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const res = await articleApi.getList({ page: 1, pageSize: 10 })
    list.value = res.data.records
  } finally {
    loading.value = false
  }
}
</script>

<!-- 之后：组件通过 Service 调用 -->
<script setup lang="ts">
import { articleService } from '@/services/articleService'
// Service 接管了缓存、loading/error 状态等逻辑
const { data: list, loading, execute } = useAsyncData(() =>
  articleService.getList({ page: 1, pageSize: 10 })
)
</script>
```

### 4.2 Axios 封装 (`src/api/request.ts`)

```ts
import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
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

// 响应拦截器 — 数据解包 + 统一错误处理
instance.interceptors.response.use(
  (response) => response.data,     // 解包外层 data
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    // 错误通知使用 shadcn Toast，由调用方处理
    return Promise.reject(error)
  }
)

export default instance
```

### 4.3 路由设计

| 路径              | 名称           | 组件             | 权限   | 说明           |
| ----------------- | -------------- | ---------------- | ------ | -------------- |
| `/`               | Home           | `HomeView`       | 公开   | 首页文章列表   |
| `/article/:id`    | ArticleDetail  | `ArticleDetail`  | 公开   | 文章详情       |
| `/search`         | Search         | `SearchView`     | 公开   | 全文搜索       |
| `/login`          | Login          | `LoginView`      | 未登录 | 登录页         |
| `/register`       | Register       | `RegisterView`   | 未登录 | 注册页         |
| `/user/:id`       | UserCenter     | `UserCenter`     | 公开   | 用户主页       |
| `/:pathMatch(.*)` | NotFound       | `NotFound`       | 公开   | 404            |

**路由守卫：**

```ts
// src/router/index.ts
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    return next({ name: 'Home' })
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
  const token = ref<string>(storage.get('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(username: string, password: string) {
    const res = await authService.login({ username, password })
    token.value = res.token
    user.value = res.user
    storage.set('token', res.token)
  }

  function logout() {
    token.value = ''
    user.value = null
    storage.remove('token')
    authService.clearCache()
  }

  return { user, token, isLoggedIn, isAdmin, login, logout }
})
```

#### 文章 Store — 缓存策略

- 首页文章列表缓存 3 分钟，由 `articleService.getList()` 内部通过 `listCache` (Map) 维护
- 文章详情页由 Service 层每次拉取最新数据（实时阅读量/点赞数）
- 搜索结果不缓存

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

  like(id: number): Promise<ApiResponse<null>> {
    return request.post(`/articles/${id}/like`)
  },

  search(keyword: string, page: number): Promise<ApiResponse<PageResult<Article>>> {
    return request.get('/articles/search', { params: { keyword, page } })
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
}

// @shared/types/user.ts
export interface User {
  id: number
  username: string
  avatar?: string
  role: 'USER' | 'ADMIN'
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
```

---

## 五、关键功能实现要点

### 5.1 Markdown 渲染（文章阅读）

使用 `@kangc/v-md-editor` 的只读预览模式。文章列表只展示摘要（纯文本前 200 字），详情页完整渲染。

### 5.2 全文搜索（Elasticsearch）

```
用户输入关键词 → articleService.search(keyword, page)
→ articleApi.search() → 后端查询 ES → 返回高亮片段
→ 前端使用 v-html 渲染 <em> 标签
```

> 安全：ES 返回的高亮片段由 Service 层做 sanitize 后再返回组件。

### 5.3 热门文章排行榜

- 首页右侧展示 Top 10，Service 层调用 `GET /articles/hot?limit=10`
- 数据随主页一同请求，由 Service 层统一组装返回

### 5.4 阅读量展示

- 文章详情页进入时，后端 `INCR` Redis 阅读计数
- 前端展示 `viewCount` 为接口返回的实时值
- 定时任务异步落库，前端无感知

### 5.5 点赞功能 — 乐观更新

```ts
// src/services/articleService.ts
async function like(id: number): Promise<void> {
  const store = useArticleStore()
  store.incrementLike(id)        // 乐观：先加一
  try {
    await articleApi.like(id)
  } catch {
    store.decrementLike(id)      // 失败：回滚
    throw new Error('点赞失败')
  }
}
```

### 5.6 权限控制

| 层级     | 实现方式                                            |
| -------- | --------------------------------------------------- |
| 路由层   | `meta.requiresAuth` + `router.beforeEach` 阻断未登录 |
| 组件层   | `v-if="userStore.isLoggedIn"` 控制登录相关入口       |
| 按钮层   | `v-if="article.authorId === user.id"` 编辑/删除按钮  |

### 5.7 响应式布局

- Tailwind 响应式 class：`grid grid-cols-1 md:grid-cols-3 lg:grid-cols-[240px_1fr_300px]`
- PC 端三栏（侧栏 + 主内容 + 热门排行），平板双栏，手机单栏
- 导航栏在移动端折叠为汉堡菜单（shadcn Sheet 组件实现）

---

## 六、性能优化

| 优化项               | 方案                                                     |
| -------------------- | -------------------------------------------------------- |
| **路由懒加载**       | `() => import('@/views/ArticleDetail.vue')`              |
| **组件异步加载**     | `defineAsyncComponent` 用于非首屏组件                     |
| **图片懒加载**       | 原生 `loading="lazy"`                                    |
| **shadcn-vue 按需**   | 只复制实际使用的组件到 `components/ui/`，不做全局引入      |
| **Markdown 渲染**    | 文章列表只显示摘要（纯文本前 200 字），不渲染完整 Markdown |
| **Service 层缓存**   | 文章列表缓存 3 分钟，搜索结果不缓存                        |
| **骨架屏**           | 列表/详情页首次加载时显示骨架屏                            |

---

## 七、构建与部署

### 7.1 构建命令

```bash
pnpm --filter blog-web build      # 生产构建，输出到 packages/blog-web/dist/
pnpm --filter blog-web preview    # 本地预览构建结果
```

### 7.2 Nginx 配置

```nginx
server {
    listen 80;
    server_name yourblog.com;

    location / {
        root /usr/share/nginx/html/blog-web;
        index index.html;
        try_files $uri $uri/ /index.html;    # SPA History 模式
    }

    # 后台管理（部署在同域名 /admin/ 路径下）
    location /admin/ {
        alias /usr/share/nginx/html/blog-admin/;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 7.3 Docker 部署

```dockerfile
# docker/Dockerfile.web
FROM node:20-alpine AS build
WORKDIR /app
COPY packages/blog-web/ packages/blog-web/
COPY packages/shared/ packages/shared/
COPY pnpm-workspace.yaml pnpm-lock.yaml package.json ./
RUN corepack enable && pnpm install --frozen-lockfile
RUN pnpm --filter blog-web build

FROM nginx:alpine
COPY --from=build /app/packages/blog-web/dist/ /usr/share/nginx/html/blog-web/
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## 八、开发规范

### 8.1 命名约定

| 类型        | 规范                            | 示例                          |
| ----------- | ------------------------------- | ----------------------------- |
| 组件文件    | PascalCase                      | `ArticleCard.vue`             |
| 视图文件    | PascalCase + View 后缀          | `HomeView.vue`                |
| Composables | camelCase + use 前缀            | `useAuth.ts`                  |
| Store       | camelCase                       | `user.ts`、`article.ts`       |
| API 模块    | camelCase                       | `article.ts`、`auth.ts`       |
| Service     | camelCase + Service 后缀        | `articleService.ts`           |
| CSS class   | kebab-case                      | `.article-card__title`        |
| 类型/接口   | PascalCase                      | `Article`、`PageResult<T>`    |

### 8.2 Service 层开发约定

- Service 层方法命名与 API 层保持一致，便于追溯
- Service 层不持有 UI 状态（loading/error 由组件自行管理）
- 数据转换（日期格式化、枚举映射）统一在 Service 层完成
- 跨组件共享的缓存由 Service 层 + Pinia Store 配合实现
- Service 层不直接操作 DOM 或引用路由实例

### 8.3 Git 提交规范

```
feat(blog-web): 新增文章搜索页
fix(blog-web): 修复评论列表分页错误
style(blog-web): 调整首页卡片间距
```

### 8.4 代码审查 Checklist

- [ ] 是否使用 `<script setup lang="ts">`
- [ ] API 调用是否通过 Service 层（组件/Store 不直接调用 API 模块）
- [ ] Service 层是否有合理的缓存策略和失效机制
- [ ] 业务逻辑是否在 Service 层而非组件中
- [ ] 列表是否有空状态和加载骨架屏
- [ ] Token 过期是否正确跳转登录页
- [ ] `v-html` 是否有 XSS 风险（仅对经过 sanitize 的 HTML 使用）
- [ ] 公共类型/工具是否放在 shared 包而非重复定义

---

## 九、FAQ

**Q: Service 层和 API 层的边界在哪里？**
A: API 层是"怎么请求"，Service 层是"请求什么 + 怎么处理"。API 层只关心 HTTP 方法、URL、参数；Service 层关心缓存策略、数据转换、乐观更新、错误降级等业务逻辑。

**Q: 如何在 blog-web 中引用 shared 的类型？**
A: 通过 Vite alias：`'@shared': resolve(__dirname, '../shared/src')`，然后 `import type { ApiResponse } from '@shared/types/api'`。

**Q: Markdown 内容如何安全渲染？**
A: `@kangc/v-md-editor` 内置 XSS 防护。若自行用 `v-html`，Service 层应先通过 `DOMPurify` 处理。

**Q: 如何调试跨域？**
A: 开发环境使用 Vite proxy 代理 `/api` 到后端；生产环境由 Nginx 统一代理，不存在跨域。

**Q: Token 存在哪里？**
A: `localStorage`。若考虑 XSS 风险可改用 `httpOnly` Cookie（需后端配合）。

---

## 十、参考资源

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [shadcn-vue 文档](https://www.shadcn-vue.com/)
- [Tailwind CSS 文档](https://tailwindcss.com/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)
- [@kangc/v-md-editor](https://code-farmer-i.github.io/vue-markdown-editor/zh/)
- [Vite 官方文档](https://cn.vitejs.dev/)
