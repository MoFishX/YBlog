# YBlog

基于 Vue 3 + Spring Boot 3 的个人博客系统，支持文章管理、评论、AI 摘要、标签分类等。

## 技术栈

| 模块 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Tailwind CSS |
| 后端 | Spring Boot 3 + MyBatis Plus + Flyway + MySQL + Redis |
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

- 下载`docker-compose.yml` 和 `.env.example`

- 自动生成安全凭证（JWT_SECRET）
- 创建 `.env` 文件并填充自动生成的密钥

#### `.env`必须配置项

``` bash
# MySQL 密码
MYSQL_ROOT_PASSWORD=你的密码

# 服务器IP
APP_DOMAIN=blog.example.com

# 注册邮件发送给用户的验证地址
APP_URL=http://blog.example.com

# 注册邮件（否则新用户无法注册）
EMAIL_RESEND_API_KEY=re_你的resend密钥

# JWT 密钥（如果是使用的自动化脚本部署会自动生成随机的密钥，则不需要设置）
# 如果需要手动设置，可打开任意在线 Base64 随机密钥生成工具。要求：随机字节数填 32，输出 Base64
JWT_SECRET=
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
