#!/bin/bash
# YBlog 一键部署脚本
# 用法: curl -fsSL https://raw.githubusercontent.com/MoFishX/YBlog/master/deploy.sh | bash

set -e

BASE="https://raw.githubusercontent.com/MoFishX/YBlog/master"

echo "==> 下载部署文件..."

curl -fsSL "$BASE/docker-compose.prod.yml" -o docker-compose.prod.yml
curl -fsSL "$BASE/.env.example" -o .env
mkdir -p blog-backend/sql
curl -fsSL "$BASE/blog-backend/sql/CreateTable.sql" -o blog-backend/sql/CreateTable.sql
curl -fsSL "$BASE/blog-backend/sql/deepseek_sql_20260607_756156.sql" -o blog-backend/sql/deepseek_sql_20260607_756156.sql

echo "==> 请编辑 .env 填入你的密钥:"
echo "    AI_API_KEY=sk-xxx"
echo "    OSS_QINIU_ACCESS_KEY_ID=xxx"
echo "    ..."
echo ""
echo "==> 编辑完成后运行:"
echo "    docker compose -f docker-compose.prod.yml up -d"
