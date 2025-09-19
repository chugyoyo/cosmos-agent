#!/bin/bash

# Docker部署脚本

set -e

echo "🚀 开始部署 Cosmos Agent..."

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 创建必要的目录
echo "📁 创建必要的目录..."
mkdir -p script/docker/nginx/ssl
mkdir -p logs/backend
mkdir -p logs/nginx

# 生成自签名SSL证书（仅用于开发环境）
if [ ! -f "script/docker/nginx/ssl/cert.pem" ]; then
    echo "🔐 生成自签名SSL证书..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout script/docker/nginx/ssl/key.pem \
        -out script/docker/nginx/ssl/cert.pem \
        -subj "/C=CN/ST=Beijing/L=Beijing/O=CosmosAgent/CN=localhost"
fi

# 停止现有容器
echo "🛑 停止现有容器..."
docker-compose -f script/docker/docker-compose.yml down --remove-orphans

# 构建并启动服务
echo "🏗️ 构建并启动服务..."
docker-compose -f script/docker/docker-compose.yml up --build -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose -f script/docker/docker-compose.yml ps

# 检查健康状态
echo "💓 检查健康状态..."
docker-compose -f script/docker/docker-compose.yml exec postgres pg_isready -U postgres
docker-compose -f script/docker/docker-compose.yml exec redis redis-cli ping

echo "✅ 部署完成！"
echo ""
echo "🌐 访问地址："
echo "   前端: http://localhost:3000"
echo "   后端API: http://localhost:8080"
echo "   API文档: http://localhost:8080/swagger-ui.html"
echo ""
echo "📊 查看日志:"
echo "   docker-compose -f script/docker/docker-compose.yml logs -f"
echo ""
echo "🛑 停止服务:"
echo "   docker-compose -f script/docker/docker-compose.yml down"