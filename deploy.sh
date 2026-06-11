#!/bin/bash
# YBlog 一键部署脚本
# 用法: curl -fsSL https://raw.githubusercontent.com/MoFishX/YBlog/master/deploy.sh | bash

set -e

BASE="https://raw.githubusercontent.com/MoFishX/YBlog/master"

echo "==> 下载部署文件..."

curl -fsSL "$BASE/docker-compose.yml" -o docker-compose.yml
curl -fsSL "$BASE/.env.example" -o .env

echo "==> 生成安全凭证..."

if command -v openssl &> /dev/null; then
  JWT_SECRET=$(openssl rand -base64 32)
else
  JWT_SECRET=$(date +%s | sha256sum | base64 | head -c 44)
fi

if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
else
  sed -i "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
fi

echo "==> 请编辑 .env 填入你的密钥:"
echo "    AI_API_KEY=sk-xxx"
echo "    OSS_QINIU_ACCESS_KEY_ID=xxx"
echo "    ..."
echo ""
echo "==> 编辑完成后运行:"
echo "    docker compose pull"
echo "    docker compose up -d"
