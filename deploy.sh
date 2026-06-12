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
  MYSQL_PASSWORD=$(openssl rand -base64 16)
  ADMIN_PASSWORD=$(openssl rand -base64 12)
else
  JWT_SECRET=$(date +%s | sha256sum | base64 | head -c 44)
  MYSQL_PASSWORD=$(date +%s | sha256sum | base64 | head -c 22)
  ADMIN_PASSWORD=$(date +%s | sha256sum | base64 | head -c 16)
fi

echo "==> 检测服务器公网IP..."

SERVER_IP=$(curl -fsSL --connect-timeout 5 ifconfig.me 2>/dev/null \
  || curl -fsSL --connect-timeout 5 ip.sb 2>/dev/null \
  || curl -fsSL --connect-timeout 5 icanhazip.com 2>/dev/null \
  || echo "")

if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "s|^MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=${MYSQL_PASSWORD}|" .env
  sed -i '' "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
  sed -i '' "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=${ADMIN_PASSWORD}|" .env
  if [ -n "$SERVER_IP" ]; then
    sed -i '' "s|^APP_DOMAIN=.*|APP_DOMAIN=${SERVER_IP}|" .env
    sed -i '' "s|^APP_URL=.*|APP_URL=http://${SERVER_IP}|" .env
  fi
else
  sed -i "s|^MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=${MYSQL_PASSWORD}|" .env
  sed -i "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
  sed -i "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=${ADMIN_PASSWORD}|" .env
  if [ -n "$SERVER_IP" ]; then
    sed -i "s|^APP_DOMAIN=.*|APP_DOMAIN=${SERVER_IP}|" .env
    sed -i "s|^APP_URL=.*|APP_URL=http://${SERVER_IP}|" .env
  fi
fi

echo "==> 填充密钥..."
echo "    MySQL/管理员/JWT 密码均已随机生成，可打开 .env 查看"
echo "    ..."
echo ""
echo "==> 编辑 .env 后运行以下命令！！！"
echo "    docker compose pull"
echo "    docker compose up -d"
