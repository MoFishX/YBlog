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

echo "==> 检测服务器公网IP..."

SERVER_IP=$(curl -fsSL --connect-timeout 5 ifconfig.me 2>/dev/null \
  || curl -fsSL --connect-timeout 5 ip.sb 2>/dev/null \
  || curl -fsSL --connect-timeout 5 icanhazip.com 2>/dev/null \
  || echo "")

if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
  if [ -n "$SERVER_IP" ]; then
    sed -i '' "s|^APP_DOMAIN=.*|APP_DOMAIN=${SERVER_IP}|" .env
    sed -i '' "s|^APP_URL=.*|APP_URL=http://${SERVER_IP}|" .env
  fi
else
  sed -i "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
  if [ -n "$SERVER_IP" ]; then
    sed -i "s|^APP_DOMAIN=.*|APP_DOMAIN=${SERVER_IP}|" .env
    sed -i "s|^APP_URL=.*|APP_URL=http://${SERVER_IP}|" .env
  fi
fi

echo "==> 请编辑 .env 文件:"
echo "    参考链接: https://github.com/MoFishX/YBlog#env%E5%BF%85%E9%A1%BB%E9%85%8D%E7%BD%AE%E9%A1%B9"
echo "    AI_API_KEY=sk-xxx"
echo "    OSS_QINIU_ACCESS_KEY_ID=xxx"
echo "    ..."
echo ""
echo "==> 编辑完成后运行！！！"
echo "    docker compose pull"
echo "    docker compose up -d"
