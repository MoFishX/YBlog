# 高性能博客平台 — RESTful API 设计文档

> 统一 Base URL: `http://{host}:{port}/api`
> 所有接口返回格式：
> ```json
> {
>   "code": 0,
>   "message": "ok",
>   "data": { }
> }
> ```
> 前端按职责拆分为 **blog-web（博客前台）** 和 **blog-admin（博客后台）**，各接口标注了对应的消费方。前端通过 **Service 抽象层** 封装 API 调用，详见 [六、前端 Service 层与 API 映射](#六前端-service-层与-api-映射)。

---

## 一、通用约定

### 1.1 状态码

| code | 含义 | 说明 |
| ---- | ---- | ---- |
| 200 | 成功 | 请求正常返回 |
| 400 | 参数错误 | 字段校验不通过，`message` 包含具体原因 |
| 401 | 未认证 | Token 缺失 / 过期 / 无效 |
| 403 | 无权限 | 角色不足（如普通用户访问管理接口） |
| 404 | 资源不存在 | 文章 / 用户 / 评论等目标不存在 |
| 409 | 冲突 | 用户名已存在、文章标题重复等 |
| 429 | 请求过多 | 触发限流 |
| 500 | 服务器错误 | 内部异常，`message` 脱敏处理 |

### 1.2 认证方式

```
Header: Authorization: Bearer <jwt-token>
```

除标注「公开」的接口外，其余均需携带 Token。

### 1.3 分页参数

所有列表类接口统一使用以下 query 参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| ---- | ---- | ---- | ------ | ---- |
| `page` | number | 否 | 1 | 页码，从 1 开始 |
| `pageSize` | number | 否 | 10 | 每页条数，最大 100 |

分页返回结构：

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "records": [ ],
    "total": 0,
    "page": 1,
    "pageSize": 10
  }
}
```

### 1.4 日期格式

所有日期字段统一为 ISO-8601 字符串：`"2025-01-15T10:30:00+08:00"`

### 1.5 前端项目标记

| 标记 | 含义 |
| ---- | ---- |
| 🌐 blog-web | 博客前台（Vue3 + TailwindCSS + shadcn-vue）消费 |
| ⚙️ blog-admin | 博客后台（Vue3 + Element Plus）消费 |
| 🌐⚙️ 共用 | 两个前端项目均消费 |

---

## 二、模块接口

---

### 🔐 Auth — 认证（公开）

> 🌐 blog-web　⚙️ blog-admin（共用）

#### 2.1 登录

```
POST /auth/login
Content-Type: application/json
```

**请求体：**

```json
{
  "username": "string, 必填, 2-50字符",
  "password": "string, 必填, 6-100字符"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "token": "string, JWT令牌",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "string",
      "email": "string|null",
      "avatar": "string|null",
      "role": "USER"
    }
  }
}
```

**错误示例：**

```json
{ "code": 401, "message": "用户名或密码错误", "data": null }
```

> blog-admin 仅允许 `role=ADMIN` 的用户登录，由 Service 层 `authService.login()` 校验。

---

#### 2.2 注册

```
POST /auth/register
Content-Type: application/json
```

**请求体：**

```json
{
  "username": "string, 必填, 2-50字符, 字母数字下划线",
  "password": "string, 必填, 6-100字符",
  "email":    "string, 选填, 合法邮箱格式"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

> 注册成功后异步发送欢迎邮件（RabbitMQ），不阻塞响应。

**错误示例：**

```json
{ "code": 409, "message": "用户名已存在", "data": null }
```

---

#### 2.3 刷新 Token

```
POST /auth/refresh
Authorization: Bearer <token>
```

**请求体：** 无

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "token": "string, 新JWT",
    "expiresIn": 86400000
  }
}
```

**说明：** 原 Token 在到期前 30 分钟内允许刷新；过期后需重新登录。

---

#### 2.4 登出

```
POST /auth/logout
Authorization: Bearer <token>
```

**请求体：** 无

**成功响应：**

```json
{
  "code": 200,
  "message": "已登出",
  "data": null
}
```

**说明：** 服务端将当前 Token 加入 Redis 黑名单（TTL = Token 剩余有效期），后续请求直接拦截。

---

### 👤 User — 用户

> 🌐 blog-web

#### 3.1 获取当前登录用户信息

```
GET /user/me
Authorization: Bearer <token>
```

**请求参数：** 无

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "id": 1,
    "username": "string",
    "email": "string|null",
    "avatar": "string|null",
    "role": "USER",
    "articleCount": 42,
    "createdAt": "2025-01-01T00:00:00+08:00"
  }
}
```

---

#### 3.2 获取指定用户公开信息

```
GET /user/{userId}
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `userId` | path | number | 用户 ID |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "id": 1,
    "username": "string",
    "avatar": "string|null",
    "articleCount": 42,
    "createdAt": "2025-01-01T00:00:00+08:00"
  }
}
```

**说明：** 公开接口，不返回 email 等隐私字段。

---

#### 3.3 更新个人资料

```
PUT /user/me
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "email":  "string, 选填",
  "avatar": "string, 选填, URL格式"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "string",
    "email": "string|null",
    "avatar": "string|null",
    "role": "USER"
  }
}
```

---

#### 3.4 修改密码

```
PUT /user/me/password
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "oldPassword": "string, 必填",
  "newPassword": "string, 必填, 6-100字符"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

### 📝 Article — 文章

> 🌐 blog-web　⚙️ blog-admin（部分共用，管理接口仅 blog-admin）

#### 4.1 文章列表（首页）

> 🌐 blog-web

```
GET /articles
```

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `page` | number | 否 | 默认 1 |
| `pageSize` | number | 否 | 默认 10，最大 100 |
| `tagId` | number | 否 | 按标签筛选 |
| `orderBy` | string | 否 | `latest` (默认) / `hot` / `oldest` |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "string",
        "summary": "string, 前200字符纯文本摘要",
        "coverImage": "string|null",
        "author": {
          "id": 1,
          "username": "string",
          "avatar": "string|null"
        },
        "tags": [
          { "id": 1, "name": "Spring Boot" }
        ],
        "viewCount": 1280,
        "likeCount": 56,
        "commentCount": 12,
        "createdAt": "2025-01-15T10:30:00+08:00",
        "updatedAt": "2025-01-15T12:00:00+08:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

**说明：**
- `summary` 取 `content` 前 200 个纯文本字符（剥离 Markdown 标记），不足 200 则全部返回
- `orderBy=hot` 基于 Redis ZSet 排名，`score = viewCount × 1 + likeCount × 2`
- `viewCount` / `likeCount` 取自 Redis 实时值 + MySQL 基线
- blog-web 的 `articleService.getList()` 对该接口实现了 3 分钟缓存

---

#### 4.2 文章详情

> 🌐 blog-web　⚙️ blog-admin（共用）

```
GET /articles/{articleId}
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `articleId` | path | number | 文章 ID |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "id": 1,
    "title": "string",
    "content": "string, Markdown原文",
    "summary": "string",
    "coverImage": "string|null",
    "status": "PUBLISHED",
    "author": {
      "id": 1,
      "username": "string",
      "avatar": "string|null"
    },
    "tags": [
      { "id": 1, "name": "Spring Boot" }
    ],
    "viewCount": 1281,
    "likeCount": 56,
    "isLiked": false,
    "createdAt": "2025-01-15T10:30:00+08:00",
    "updatedAt": "2025-01-15T12:00:00+08:00"
  }
}
```

**说明：**
- 该请求触发阅读量 +1（Redis INCR）
- `isLiked`：当前登录用户是否已点赞（未登录时为 `false`）
- 作者本人访问时 `status` 可返回 `DRAFT`
- blog-web 的 `articleService.getDetail()` 每次拉取最新，不缓存

---

#### 4.3 发布文章

> 🌐 blog-web　⚙️ blog-admin（共用）

```
POST /articles
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "title":   "string, 必填, 1-200字符",
  "content": "string, 必填, Markdown格式, 最小10字符",
  "summary": "string, 选填, 若为空则自动截取content前200纯文本字符",
  "coverImage": "string, 选填, 封面图URL",
  "tagIds":  [1, 2, 3],
  "status":  "string, 选填, PUBLISHED | DRAFT, 默认PUBLISHED"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1,
    "title": "string",
    "status": "PUBLISHED",
    "createdAt": "2025-01-15T12:00:00+08:00"
  }
}
```

**说明：**
- 发布后异步发送 MQ 消息同步 ES 索引
- `tagIds` 空数组表示无标签
- blog-web 的 `articleService.create()` 发布后清除列表缓存

---

#### 4.4 更新文章

> 🌐 blog-web　⚙️ blog-admin（共用）

```
PUT /articles/{articleId}
Authorization: Bearer <token>
Content-Type: application/json
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `articleId` | path | number | 文章 ID |

**请求体：**（所有字段选填，按需更新）

```json
{
  "title":   "string, 选填, 1-200字符",
  "content": "string, 选填, Markdown格式",
  "summary": "string, 选填",
  "coverImage": "string, 选填",
  "tagIds":  [1, 2, 3],
  "status":  "PUBLISHED | DRAFT"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "title": "string",
    "status": "PUBLISHED",
    "updatedAt": "2025-01-15T13:00:00+08:00"
  }
}
```

**权限：** 仅作者本人或管理员可操作。

---

#### 4.5 删除文章

> 🌐 blog-web　⚙️ blog-admin（共用）

```
DELETE /articles/{articleId}
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `articleId` | path | number | 文章 ID |

**成功响应：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**说明：** 软删除（`deleted_at` 字段），同时从 ES 索引移除、清除相关缓存。仅作者本人或管理员可操作。

---

#### 4.6 点赞 / 取消点赞

> 🌐 blog-web

```
POST /articles/{articleId}/like
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `articleId` | path | number | 文章 ID |

**请求体：** 无

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "isLiked": true,
    "likeCount": 57
  }
}
```

**说明：** 幂等 —— 同一用户重复请求为 toggle 行为（已点赞 → 取消；未点赞 → 点赞）。点赞时异步更新 Redis ZSet 排行榜分数。blog-web 的 `articleService.like()` 实现了乐观更新（先改 UI，失败回滚）。

---

#### 4.7 热门文章排行榜

> 🌐 blog-web

```
GET /articles/hot
```

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `limit` | number | 否 | 返回条数，默认 10，最大 50 |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": [
    {
      "rank": 1,
      "id": 42,
      "title": "string",
      "summary": "string",
      "viewCount": 12800,
      "likeCount": 560,
      "author": {
        "id": 1,
        "username": "string",
        "avatar": "string|null"
      },
      "createdAt": "2025-01-15T10:30:00+08:00"
    }
  ]
}
```

**说明：** 直接读取 Redis ZSet `hot:articles`，按 `score` 倒序返回 Top N，无分页。

#### 4.8 我的文章列表

> 🌐 blog-web

```
GET /articles/mine
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `page` | number | 否 | 默认 1 |
| `pageSize` | number | 否 | 默认 10 |
| `status` | string | 否 | `PUBLISHED` / `DRAFT`，不传则全部 |

**成功响应：** 同 [4.1 文章列表](#41-文章列表首页)，额外返回 `status` 字段。

---

### 💬 Comment — 评论

> 🌐 blog-web　⚙️ blog-admin（管理接口仅 blog-admin）

#### 5.1 文章评论列表

> 🌐 blog-web

```
GET /articles/{articleId}/comments
```

| 参数 | 位置 / 类型 | 必填 | 说明 |
| ---- | ----------- | ---- | ---- |
| `articleId` | path, number | 必填 | 文章 ID |
| `page` | query, number | 否 | 默认 1 |
| `pageSize` | query, number | 否 | 默认 20，最大 100 |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "content": "string",
        "user": {
          "id": 1,
          "username": "string",
          "avatar": "string|null"
        },
        "replyTo": {
          "id": 2,
          "username": "string"
        },
        "createdAt": "2025-01-15T14:00:00+08:00"
      }
    ],
    "total": 32,
    "page": 1,
    "pageSize": 20
  }
}
```

**说明：**
- `replyTo`：回复的目标用户，`null` 表示直接评论文章
- 按 `createdAt` 升序排列（最早在前）

---

#### 5.2 发表评论

> 🌐 blog-web

```
POST /articles/{articleId}/comments
Authorization: Bearer <token>
Content-Type: application/json
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `articleId` | path | number | 文章 ID |

**请求体：**

```json
{
  "content":  "string, 必填, 1-1000字符",
  "parentId": "number, 选填, 回复的评论ID, null表示直接评论文章"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": 33,
    "content": "string",
    "createdAt": "2025-01-15T14:05:00+08:00"
  }
}
```

---

#### 5.3 删除评论

> 🌐 blog-web

```
DELETE /comments/{commentId}
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `commentId` | path | number | 评论 ID |

**成功响应：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**权限：** 评论作者、文章作者或管理员可删除。

---

#### 5.4 我收到的评论回复

> 🌐 blog-web

```
GET /comments/replies
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `page` | number | 否 | 默认 1 |
| `pageSize` | number | 否 | 默认 10 |
| `unreadOnly` | number | 否 | `1` 仅未读，默认 `0` 全部 |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 33,
        "content": "string",
        "articleId": 42,
        "articleTitle": "string",
        "user": {
          "id": 1,
          "username": "string"
        },
        "isRead": false,
        "createdAt": "2025-01-15T14:05:00+08:00"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 10
  }
}
```

---

### 🏷️ Tag — 标签

> 🌐 blog-web（读取）　⚙️ blog-admin（管理）

#### 6.1 标签列表（全部）

> 🌐 blog-web　⚙️ blog-admin（共用）

```
GET /tags
```

**请求参数：** 无

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": [
    {
      "id": 1,
      "name": "Spring Boot",
      "articleCount": 35
    },
    {
      "id": 2,
      "name": "Redis",
      "articleCount": 18
    }
  ]
}
```

**说明：** 按 `articleCount` 降序排列；文章数为 0 的不返回。

---

#### 6.2 创建标签（管理员）

> ⚙️ blog-admin

```
POST /tags
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "name": "string, 必填, 1-30字符, 不可重复"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 10,
    "name": "Vue3"
  }
}
```

**权限：** 仅管理员。

---

#### 6.3 更新标签（管理员）

> ⚙️ blog-admin

```
PUT /tags/{tagId}
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "name": "string, 必填, 1-30字符"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 10,
    "name": "Vue.js 3"
  }
}
```

---

#### 6.4 删除标签（管理员）

> ⚙️ blog-admin

```
DELETE /tags/{tagId}
Authorization: Bearer <token>
```

**成功响应：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**说明：** 删除后自动解除该标签与所有文章的关联关系。

---

### 📁 Upload — 文件上传

> 🌐 blog-web　⚙️ blog-admin（共用）

#### 7.1 上传文件

```
POST /upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `file` | File | 必填 | 文件，最大 10MB |
| `type` | string | 必填 | `avatar` / `cover` / `content` |

**成功响应：**

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://cdn.yourblog.com/uploads/2025/01/15/abc123.png",
    "filename": "abc123.png",
    "size": 204800
  }
}
```

**限制：**
- 图片格式：`jpg` / `jpeg` / `png` / `gif` / `webp`
- 单文件最大 10MB（`MultipartFile` 校验）
- 头像上传自动裁剪为 200×200
- 生产环境对接阿里云 OSS，开发环境存本地 `uploads/` 目录

---

### 📊 Admin — 管理后台（管理员）

> ⚙️ blog-admin（全部仅后台消费）

#### 8.1 用户列表

```
GET /admin/users
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `page` | number | 否 | 默认 1 |
| `pageSize` | number | 否 | 默认 20 |
| `keyword` | string | 否 | 按用户名模糊搜索 |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "string",
        "email": "string|null",
        "avatar": "string|null",
        "role": "USER",
        "articleCount": 12,
        "status": "ACTIVE",
        "createdAt": "2025-01-15T10:30:00+08:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

> blog-admin 的 `userService.getList()` 消费此接口。

---

#### 8.2 封禁 / 解封用户

```
PUT /admin/users/{userId}/status
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "status": "string, 必填, ACTIVE | BANNED"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "string",
    "status": "BANNED"
  }
}
```

> blog-admin 的 `userService.banUser()` 消费此接口，Service 层处理二次确认弹窗。

---

#### 8.3 修改用户角色

```
PUT /admin/users/{userId}/role
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "role": "string, 必填, USER | ADMIN"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "修改成功",
  "data": {
    "id": 1,
    "username": "string",
    "role": "ADMIN"
  }
}
```

> blog-admin 的 `userService.changeRole()` 消费此接口。

---

#### 8.4 所有文章列表（含草稿）

```
GET /admin/articles
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `page` | number | 否 | 默认 1 |
| `pageSize` | number | 否 | 默认 20 |
| `status` | string | 否 | `PUBLISHED` / `DRAFT` / `REVIEWING`，不传则全部 |
| `keyword` | string | 否 | 按标题模糊搜索 |

**成功响应：** 同 [4.1](#41-文章列表首页)，额外返回 `author` 完整信息（含 email）和 `status`。

> blog-admin 的 `articleService.getList()` 消费此接口，支持按状态和关键词筛选。

---

#### 8.5 强制删除文章（管理员）

```
DELETE /admin/articles/{articleId}
Authorization: Bearer <token>
```

**成功响应：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**说明：** 管理员可删除任意用户的文章（含软删除恢复）。

---

#### 8.6 批量删除文章 ⭐ 新增

```
POST /admin/articles/batch-delete
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体：**

```json
{
  "ids": [1, 2, 3, 4, 5]
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "已删除 5 篇文章",
  "data": {
    "deletedCount": 5
  }
}
```

**说明：** 管理员批量删除文章，软删除。blog-admin 的 `articleService.batchDelete()` 消费此接口，Service 层处理选中态和确认弹窗。

---

#### 8.7 审核文章 ⭐ 新增

```
PUT /admin/articles/{articleId}/review
Authorization: Bearer <token>
Content-Type: application/json
```

| 参数 | 位置 | 类型 | 说明 |
| ---- | ---- | ---- | ---- |
| `articleId` | path | number | 文章 ID |

**请求体：**

```json
{
  "status": "APPROVED | REJECTED",
  "reason": "string, 选填, 驳回原因"
}
```

**成功响应：**

```json
{
  "code": 200,
  "message": "审核通过",
  "data": {
    "id": 42,
    "title": "string",
    "status": "APPROVED"
  }
}
```

**说明：** 仅管理员可操作。审核通过后文章状态变更为 `PUBLISHED`，异步同步 ES 索引。blog-admin 的 `articleService.review()` 消费此接口。

---

#### 8.8 评论管理列表 ⭐ 新增

```
GET /admin/comments
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| `page` | number | 否 | 默认 1 |
| `pageSize` | number | 否 | 默认 20 |
| `keyword` | string | 否 | 按评论内容模糊搜索 |
| `articleId` | number | 否 | 按文章筛选 |

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 33,
        "content": "string",
        "articleId": 42,
        "articleTitle": "string",
        "user": {
          "id": 1,
          "username": "string"
        },
        "status": "ACTIVE",
        "createdAt": "2025-01-15T14:05:00+08:00"
      }
    ],
    "total": 4200,
    "page": 1,
    "pageSize": 20
  }
}
```

> blog-admin 的 `commentService.getList()` 消费此接口。

---

#### 8.9 强制删除评论（管理员）⭐ 新增

```
DELETE /admin/comments/{commentId}
Authorization: Bearer <token>
```

**成功响应：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**说明：** 管理员可删除任意评论。blog-admin 的 `commentService.delete()` 消费此接口。

---

#### 8.10 系统统计概览

```
GET /admin/stats
Authorization: Bearer <token>
```

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "userCount": 1280,
    "articleCount": 356,
    "commentCount": 4200,
    "totalViews": 156000,
    "totalLikes": 8900,
    "todayViews": 320,
    "todayNewUsers": 5,
    "todayNewArticles": 2
  }
}
```

> blog-admin 的 `dashboardService.getStats()` 消费此接口，Service 层缓存 5 分钟。

---

#### 8.11 周访问趋势 ⭐ 新增

```
GET /admin/stats/weekly-trend
Authorization: Bearer <token>
```

**成功响应：**

```json
{
  "code": 200,
  "message": "ok",
  "data": [
    { "date": "2025-01-09", "count": 1280 },
    { "date": "2025-01-10", "count": 1450 },
    { "date": "2025-01-11", "count": 1320 },
    { "date": "2025-01-12", "count": 980 },
    { "date": "2025-01-13", "count": 1560 },
    { "date": "2025-01-14", "count": 1890 },
    { "date": "2025-01-15", "count": 2100 }
  ]
}
```

**说明：** 返回近 7 天每天访问量。blog-admin 的 `dashboardService.getWeeklyTrend()` 消费此接口，数据用于 ECharts 折线图渲染。

---

## 三、接口汇总表

| 模块 | 接口 | 方法 | URL | 权限 | 前端项目 |
| ---- | ---- | ---- | --- | ---- | -------- |
| Auth | 登录 | POST | `/auth/login` | 公开 | 🌐⚙️ 共用 |
| Auth | 注册 | POST | `/auth/register` | 公开 | 🌐 blog-web |
| Auth | 刷新 Token | POST | `/auth/refresh` | 登录 | 🌐⚙️ 共用 |
| Auth | 登出 | POST | `/auth/logout` | 登录 | 🌐⚙️ 共用 |
| User | 当前用户信息 | GET | `/user/me` | 登录 | 🌐 blog-web |
| User | 指定用户信息 | GET | `/user/{userId}` | 公开 | 🌐 blog-web |
| User | 更新个人资料 | PUT | `/user/me` | 登录 | 🌐 blog-web |
| User | 修改密码 | PUT | `/user/me/password` | 登录 | 🌐 blog-web |
| Article | 文章列表 | GET | `/articles` | 公开 | 🌐 blog-web |
| Article | 文章详情 | GET | `/articles/{articleId}` | 公开 | 🌐⚙️ 共用 |
| Article | 发布文章 | POST | `/articles` | 登录 | 🌐⚙️ 共用 |
| Article | 更新文章 | PUT | `/articles/{articleId}` | 作者/管理员 | 🌐⚙️ 共用 |
| Article | 删除文章 | DELETE | `/articles/{articleId}` | 作者/管理员 | 🌐⚙️ 共用 |
| Article | 点赞/取消 | POST | `/articles/{articleId}/like` | 登录 | 🌐 blog-web |
| Article | 热门排行 | GET | `/articles/hot` | 公开 | 🌐 blog-web |
| Article | 搜索 | GET | `/articles/search` | 公开 | 🌐 blog-web |
| Article | 我的文章 | GET | `/articles/mine` | 登录 | 🌐 blog-web |
| Comment | 评论列表 | GET | `/articles/{articleId}/comments` | 公开 | 🌐 blog-web |
| Comment | 发表评论 | POST | `/articles/{articleId}/comments` | 登录 | 🌐 blog-web |
| Comment | 删除评论 | DELETE | `/comments/{commentId}` | 作者/管理员 | 🌐 blog-web |
| Comment | 回复通知 | GET | `/comments/replies` | 登录 | 🌐 blog-web |
| Tag | 标签列表 | GET | `/tags` | 公开 | 🌐⚙️ 共用 |
| Tag | 创建标签 | POST | `/tags` | 管理员 | ⚙️ blog-admin |
| Tag | 更新标签 | PUT | `/tags/{tagId}` | 管理员 | ⚙️ blog-admin |
| Tag | 删除标签 | DELETE | `/tags/{tagId}` | 管理员 | ⚙️ blog-admin |
| Upload | 上传文件 | POST | `/upload` | 登录 | 🌐⚙️ 共用 |
| Admin | 用户列表 | GET | `/admin/users` | 管理员 | ⚙️ blog-admin |
| Admin | 封禁/解封 | PUT | `/admin/users/{userId}/status` | 管理员 | ⚙️ blog-admin |
| Admin | 修改角色 | PUT | `/admin/users/{userId}/role` | 管理员 | ⚙️ blog-admin |
| Admin | 全部文章 | GET | `/admin/articles` | 管理员 | ⚙️ blog-admin |
| Admin | 强制删文 | DELETE | `/admin/articles/{articleId}` | 管理员 | ⚙️ blog-admin |
| Admin | 批量删除文章 | POST | `/admin/articles/batch-delete` | 管理员 | ⚙️ blog-admin |
| Admin | 审核文章 | PUT | `/admin/articles/{articleId}/review` | 管理员 | ⚙️ blog-admin |
| Admin | 评论管理列表 | GET | `/admin/comments` | 管理员 | ⚙️ blog-admin |
| Admin | 强制删评论 | DELETE | `/admin/comments/{commentId}` | 管理员 | ⚙️ blog-admin |
| Admin | 系统统计 | GET | `/admin/stats` | 管理员 | ⚙️ blog-admin |
| Admin | 周访问趋势 | GET | `/admin/stats/weekly-trend` | 管理员 | ⚙️ blog-admin |

---

## 四、Spring Security 路由白名单

```java
// SecurityConfig 中需放行的公开路由
.requestMatchers(
    HttpMethod.GET,  "/articles", "/articles/*", "/articles/hot", "/articles/search",
    "/articles/*/comments",
    "/user/*",
    "/tags"
).permitAll()
.requestMatchers("/auth/**").permitAll()
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/doc.html").permitAll()
// 其余全部需要认证
.anyRequest().authenticated()
```

**匹配规则补充说明：**

| 路径模式 | 放行的具体 URL 示例 |
| -------- | ------------------- |
| `GET /articles` | `/articles?page=1&pageSize=10` |
| `GET /articles/*` | `/articles/42` |
| `GET /articles/hot` | `/articles/hot?limit=10` |
| `GET /articles/*/comments` | `/articles/42/comments?page=1` |
| `GET /user/*` | `/user/1` |
| `GET /tags` | `/tags` |

> **注意：** `POST /articles`、`PUT /articles/*`、`DELETE /articles/*` 不在白名单，需要认证；管理员接口通过 `@PreAuthorize("hasRole('ADMIN')")` 在 Controller 层二次鉴权。`/admin/**` 路径全部要求 ADMIN 角色。

---

## 五、错误码枚举（Java 侧参考）

```java
public enum ErrorCode {
    SUCCESS             (200, "ok"),
    BAD_REQUEST         (400, "参数错误"),
    UNAUTHORIZED        (401, "未认证或Token已过期"),
    FORBIDDEN           (403, "无权限执行此操作"),
    NOT_FOUND           (404, "资源不存在"),
    CONFLICT            (409, "资源冲突"),
    TOO_MANY_REQUESTS   (429, "请求过于频繁"),
    INTERNAL_ERROR      (500, "服务器内部错误"),

    // 业务错误
    USERNAME_EXISTS     (409, "用户名已存在"),
    WRONG_PASSWORD      (401, "用户名或密码错误"),
    ARTICLE_NOT_FOUND   (404, "文章不存在"),
    COMMENT_NOT_FOUND   (404, "评论不存在"),
    USER_NOT_FOUND      (404, "用户不存在"),
    TAG_NAME_EXISTS     (409, "标签名已存在"),
    FILE_TOO_LARGE      (400, "文件大小超出限制"),
    FILE_TYPE_INVALID   (400, "文件类型不支持");
}
```

---

## 六、前端 Service 层与 API 映射

### 6.1 架构概述

前端采用三层架构，Service 层作为 API 层和组件之间的业务抽象：

```
组件 / Store  →  Service 层  →  API 层  →  HTTP (Axios)
(UI 交互)        (业务逻辑)     (HTTP封装)    (网络请求)
```

### 6.2 后端 API → 前端模块映射

| 后端 API 模块 | 前端 API 模块（api/modules/） | 前端 Service（services/） | 消费项目 |
| --- | --- | --- | --- |
| Auth | `auth.ts` | `authService.ts` | 🌐⚙️ 共用 |
| User | `user.ts` | `userService.ts` | 🌐 blog-web |
| Article (前台) | `article.ts` | `articleService.ts` | 🌐 blog-web |
| Article (管理) | `article.ts` | `articleService.ts` | ⚙️ blog-admin |
| Comment (前台) | `comment.ts` | `commentService.ts` | 🌐 blog-web |
| Comment (管理) | `comment.ts` | `commentService.ts` | ⚙️ blog-admin |
| Tag | `tag.ts` | `tagService.ts` | 🌐⚙️ 共用 |
| Upload | `upload.ts` | — (API 层直接调用) | 🌐⚙️ 共用 |
| Dashboard | `dashboard.ts` | `dashboardService.ts` | ⚙️ blog-admin |

### 6.3 Service 层职责示例

以 `blog-web/src/services/articleService.ts` 为例：

| 后端接口 | Service 方法 | Service 层额外逻辑 |
| --- | --- | --- |
| `GET /articles` | `getList(params)` | 3 分钟缓存（Map 实现）、缓存失效策略 |
| `GET /articles/{id}` | `getDetail(id)` | 每次实时拉取，不做缓存 |
| `POST /articles/{id}/like` | `like(id)` | 乐观更新（先改 UI）、失败自动回滚 |
| `POST /articles` | `create(data)` | 发布后清除列表缓存 |

以 `blog-admin/src/services/dashboardService.ts` 为例：

| 后端接口 | Service 方法 | Service 层额外逻辑 |
| --- | --- | --- |
| `GET /admin/stats` | `getStats()` | 5 分钟缓存、数据聚合 |
| `GET /admin/stats/weekly-trend` | `getWeeklyTrend()` | 数据转换为 ECharts 所需格式 |

以 `blog-admin/src/services/articleService.ts` 为例：

| 后端接口 | Service 方法 | Service 层额外逻辑 |
| --- | --- | --- |
| `DELETE /admin/articles/{id}` | `delete(id)` | ElMessage.success 提示 |
| `POST /admin/articles/batch-delete` | `batchDelete(ids)` | 批量选中为空时按钮置灰、ElMessageBox.confirm 二次确认 |
| `PUT /admin/articles/{id}/review` | `review(id, status)` | 审核状态机校验、ElMessage 成功/失败提示 |

### 6.4 开发约定

- **API 层**：一个文件对应一个后端模块，方法名与后端接口路径一一对应，只关心 URL / Method / Params / Response Type
- **Service 层**：一个文件对应一个业务领域，聚合多个 API 调用，包含缓存、数据转换、乐观更新、消息提示等逻辑
- **组件**：只调用 Service 层方法，不直接 `import` API 模块
- **跨项目共享**：公共 Service（如 `authService`）放在 `packages/shared/src/services/`，通过 `@shared` 别名引用

---

## 七、设计依据 & 后端文档对齐

| 设计点 | 后端文档章节 | 说明 |
| ------ | ------------ | ---- |
| 统一响应 `{code,message,data}` | 4.1 `ApiResponse<T>` | 全局封装 |
| JWT Bearer Token | 4.2 | `JwtAuthenticationFilter` + `SecurityConfig` |
| 阅读量 Redis INCR + 排行 | 4.3 + 4.4 | `ViewCountSyncTask` + ZSet |
| ES 搜索高亮 | 4.6 | `SearchServiceImpl` multiMatch + highlight |
| RabbitMQ 异步 | 4.7 | 文章发布 → ES 同步 |
| 接口限流 | 4.8 | Redis + Lua 计数器 |
| 全局异常处理 | 4.9 | `GlobalExceptionHandler` |
| 数据库表结构 | 5.1 | `user` / `article` / `comment` / `tag` / `article_tag` |
| 前端 Service 层 | 前端文档 4.1 节 | Service 层封装业务逻辑，组件不直接调 API |
