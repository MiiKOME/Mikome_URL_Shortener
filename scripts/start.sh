#!/bin/bash

# URL短链接项目快速启动脚本

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 URL短链接项目启动脚本${NC}"
echo "================================"

# 进入项目根目录
cd "$(dirname "$0")/.."

# 检查Docker是否运行
if ! docker info &> /dev/null; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 启动服务
echo "🔨 构建并启动服务..."
docker-compose up --build -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "📊 服务状态："
docker-compose ps

echo ""
echo -e "${GREEN}✅ 启动完成！${NC}"
echo ""
echo "🌐 访问地址："
echo "  前端应用: http://localhost:3000"
echo "  后端API:  http://localhost:8080"
echo "  健康检查: http://localhost:8080/actuator/health"
echo ""
echo "🔧 常用命令："
echo "  查看日志: docker-compose logs -f"
echo "  停止服务: docker-compose down"
echo "  重启服务: docker-compose restart"