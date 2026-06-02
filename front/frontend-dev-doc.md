# 博客平台 — 前端开发文档

> Vue3 + Vite + Monorepo 架构，按职责拆分为两个独立子项目

| 子项目 | 说明 | UI 方案 | 开发文档 |
| --- | --- | --- | --- |
| **blog-web** | 博客前台，面向读者 | Vue3 + TailwindCSS + shadcn-vue | [blog-web-dev-doc.md](./blog-web-dev-doc.md) |
| **blog-admin** | 博客后台，面向管理员 | Vue3 + Element Plus + ECharts | [blog-admin-dev-doc.md](./blog-admin-dev-doc.md) |

### 公共模块

`packages/shared/` 存放两个项目共享的类型定义和工具函数，通过 `@shared` 别名引用。

### 项目结构

```
blog-frontend/
├── packages/
│   ├── blog-web/        # 博客前台
│   ├── blog-admin/      # 博客后台
│   └── shared/          # 公共模块
├── pnpm-workspace.yaml
└── package.json
```

### 架构设计

两个项目共享同一套架构理念：

```
View / Store  →  Service 层  →  API 层  →  HTTP
(UI 渲染)       (业务逻辑)     (HTTP 封装)   (Axios)
```

- **API 层**：纯 HTTP 调用，与后端接口一一对应
- **Service 层**（新增）：缓存策略、数据转换、乐观更新、错误处理、业务校验
- **View/Store**：只关注 UI 交互，不直接调用 API

### 快速开始 ##警告：（这里请使用npm命令）

```bash
# 安装依赖
pnpm install

# 启动 blog-web（前台）
pnpm --filter blog-web dev       # → http://localhost:5173

# 启动 blog-admin（后台）
pnpm --filter blog-admin dev     # → http://localhost:5174
```
