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

### 方式一：一键脚本

```bash
curl -fsSL https://raw.githubusercontent.com/MoFishX/YBlog/master/deploy.sh | bash
```

脚本会自动下载 `docker-compose.prod.yml`、`.env.example` 和 SQL 建表文件。然后：

```bash
vim .env    # 填入你的密钥
docker compose -f docker-compose.prod.yml up -d
```

### 方式二：手动准备

将以下文件放到服务器同一目录：`docker-compose.prod.yml`、`.env`、`blog-backend/sql/`。

创建 `.env` 并填入密钥后启动：

```bash
docker compose -f docker-compose.prod.yml up -d
```

访问 `http://服务器IP` 即可。

> 如果镜像拉取报 401，去 [GitHub Packages 设置](https://github.com/MoFishX/YBlog/packages) 把包改为 Public。

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
