# YBlog

基于 Vue 3 + Spring Boot 3 的个人博客系统，支持文章管理、评论、AI 摘要、标签分类等。

## 技术栈

| 模块 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Tailwind CSS |
| 后端 | Spring Boot 3 + MyBatis Plus + SQL Schema + MySQL + Redis |
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
│   └── Dockerfile          # 后端镜像构建
├── docker-compose.yml      # Docker 编排（支持本地构建与生产镜像）
├── .env                    # 环境变量配置
└── .github/workflows/      # CI/CD 自动构建推送到 ghcr.io
```

## 快速部署

### 方式一：手动部署

> Writing

### 方式二：Docker Compose

使用 Docker Compose 部署，包含 MySQL 和 Reids 容器。

#### 前置条件

- Docker 20.10+
- Docker Compose v2+

#### 快速开始（一键部署）

使用自动化脚本快速搭建：

```bash
# 创建部署目录
mkdir -p yblog-deploy && cd yblog-deploy

# 下载并运行部署准备脚本
curl -sSL https://raw.githubusercontent.com/MoFishX/YBlog/master/deploy.sh | bash

# 启动服务
docker compose pull
docker compose up -d
```

#### 脚本功能

- 下载 `docker-compose.yml` 和 `.env.example`（保存为 `.env`）
- 自动生成随机密钥（JWT_SECRET、MYSQL_ROOT_PASSWORD、ADMIN_PASSWORD）
- 检测服务器公网 IP，自动填入 APP_DOMAIN / APP_URL
- 将生成的密钥和 IP 写入 `.env` 文件

#### `.env` 必须手动配置项

脚本已自动填入 MySQL/管理员/JWT 密码和服务 IP，以下需自行填写：

``` bash
# 注册邮件（否则新用户无法注册）
EMAIL_RESEND_API_KEY=re_你的resend密钥

# 如需使用七牛云 OSS 存储上传文件
STORAGE_TYPE=oss
OSS_QINIU_ACCESS_KEY_ID=你的七牛云AK
OSS_QINIU_ACCESS_KEY_SECRET=你的七牛云SK
OSS_QINIU_BUCKET_NAME=你的bucket名称
```

## 本地开发

```bash
# 1. 启动所有服务（自动编译前后端）
docker compose up -d --build

# 2. 前端热更新
cd front
npm run dev
```

## 端口说明

| 服务 | 容器内端口 | 对外端口 |
|------|-----------|---------|
| Nginx（前端） | 80 | 80（可配置） |
| Spring Boot（后端） | 1145 | 1145（可配置） |
| MySQL | 3306 | 3306（可配置） |
| Redis | 6379 | 6379（可配置） |
