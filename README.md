# YBlog

基于 Vue 3 + Spring Boot 3 的个人博客系统，支持文章管理、评论、AI 摘要、标签分类等。

## 技术栈

| 模块 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Tailwind CSS |
| 后端 | Spring Boot 3 + MyBatis Plus + MySQL + Redis |
| AI | DeepSeek API |
| 存储 | 七牛云 OSS |
| 邮件 | Resend |

## 项目结构

```
blog/
├── front/                  # 前端项目
│   ├── src/                # 源码
│   ├── Dockerfile          # 前端镜像构建
│   └── nginx.conf          # Nginx 配置（含 /api 代理到后端）
├── blog-backend/           # 后端项目
│   ├── blog-server/        # 主模块
│   ├── blog-common/        # 通用模块
│   ├── blog-pojo/          # 实体模块
│   ├── sql/                # 数据库建表脚本
│   └── Dockerfile          # 后端镜像构建
├── docker-compose.yml      # 本地开发编排（编译 + 运行）
├── docker-compose.prod.yml # 生产部署编排（只拉镜像）
├── .env                    # 环境变量配置
└── .github/workflows/      # CI/CD 自动构建推送到 ghcr.io
```

## 快速部署

### 1. 准备文件

将以下文件放到服务器同一目录：

```
docker-compose.prod.yml
.env
blog-backend/sql/
```

### 2. 配置环境变量

编辑 `.env`，填入真实密钥：

```bash
MYSQL_ROOT_PASSWORD=你的数据库密码

# DeepSeek AI（必填）
AI_API_KEY=sk-xxxxxxxx

# 七牛云 OSS（图片上传需要）
OSS_QINIU_ACCESS_KEY_ID=xxx
OSS_QINIU_ACCESS_KEY_SECRET=xxx
OSS_QINIU_BUCKET_NAME=xxx

# Resend 邮件（注册验证需要）
EMAIL_RESEND_API_KEY=re_xxxxxxxx
```

### 3. 启动

```bash
docker compose -f docker-compose.prod.yml up -d
```

访问 `http://服务器IP` 即可。

## 本地开发

```bash
# 1. 编译后端
cd blog-backend
mvn clean package -DskipTests

# 2. 启动所有服务
cd ..
docker compose up -d

# 3. 前端热更新
cd front
npm run dev
```

## 端口说明

| 服务 | 容器内端口 | 对外端口 |
|------|-----------|---------|
| Nginx（前端） | 80 | 80（可配 `FRONTEND_PORT`） |
| Spring Boot（后端） | 8080 | 8085（可配 `BACKEND_PORT`） |
| MySQL | 3306 | 3306（可配 `MYSQL_PORT`） |
| Redis | 6379 | 6379（可配 `REDIS_PORT`） |
