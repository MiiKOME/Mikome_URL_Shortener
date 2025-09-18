# URL短链接项目 Makefile

.PHONY: help build start stop restart logs clean test dev prod status health

# 默认目标
.DEFAULT_GOAL := help

# 颜色定义
BLUE := \033[36m
GREEN := \033[32m
YELLOW := \033[33m
NC := \033[0m

## 显示帮助信息
help:
	@echo "$(BLUE)URL短链接项目 - 可用命令:$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-15s$(NC) %s\n", $$1, $$2}'
	@echo ""

## 🔨 构建并启动开发环境
dev:
	@echo "$(BLUE)🚀 启动开发环境...$(NC)"
	docker-compose up --build -d
	@$(MAKE) status

## 🚀 构建并启动生产环境
prod:
	@echo "$(BLUE)🚀 启动生产环境...$(NC)"
	docker-compose -f docker-compose.prod.yml up --build -d
	@$(MAKE) status

## 🏗️ 只构建镜像(不启动)
build:
	@echo "$(BLUE)🔨 构建镜像...$(NC)"
	docker-compose build

## 🏗️ 无缓存构建镜像
build-no-cache:
	@echo "$(BLUE)🔨 无缓存构建镜像...$(NC)"
	docker-compose build --no-cache

## 🟢 启动服务
start:
	@echo "$(BLUE)▶️ 启动服务...$(NC)"
	docker-compose up -d

## ⏹️ 停止服务
stop:
	@echo "$(BLUE)⏹️ 停止服务...$(NC)"
	docker-compose down

## 🔄 重启服务
restart:
	@echo "$(BLUE)🔄 重启服务...$(NC)"
	docker-compose restart

## 📋 查看服务状态
status:
	@echo "$(BLUE)📊 服务状态:$(NC)"
	@docker-compose ps
	@echo ""
	@echo "$(GREEN)🌐 访问地址:$(NC)"
	@echo "  前端应用: http://localhost:3000"
	@echo "  后端API:  http://localhost:8080"
	@echo "  健康检查: http://localhost:8080/actuator/health"

## 📝 查看服务日志
logs:
	docker-compose logs -f

## 📝 查看后端日志
logs-backend:
	docker-compose logs -f backend

## 📝 查看前端日志
logs-frontend:
	docker-compose logs -f frontend

## 🏥 健康检查
health:
	@echo "$(BLUE)🏥 检查服务健康状态...$(NC)"
	@curl -s http://localhost:8080/actuator/health | python3 -m json.tool || \
	echo "$(YELLOW)⚠️ 后端服务不可访问$(NC)"
	@echo ""
	@curl -s -I http://localhost:3000 | head -1 || \
	echo "$(YELLOW)⚠️ 前端服务不可访问$(NC)"

## 🧪 运行并发测试
test:
	@if [ -f "concurrency-test/load-test.js" ]; then \
		echo "$(BLUE)🧪 运行并发测试...$(NC)"; \
		node concurrency-test/load-test.js; \
	else \
		echo "$(YELLOW)⚠️ 测试文件不存在$(NC)"; \
	fi

## 🧪 快速并发测试
test-quick:
	@if [ -f "concurrency-test/quick-test.sh" ]; then \
		echo "$(BLUE)🧪 运行快速并发测试...$(NC)"; \
		./concurrency-test/quick-test.sh; \
	else \
		echo "$(YELLOW)⚠️ 快速测试脚本不存在$(NC)"; \
	fi

## 🐳 进入后端容器
shell-backend:
	docker-compose exec backend sh

## 🐳 进入前端容器
shell-frontend:
	docker-compose exec frontend sh

## 🧹 清理未使用的资源
clean:
	@echo "$(BLUE)🧹 清理Docker资源...$(NC)"
	docker-compose down --volumes --remove-orphans
	docker system prune -f
	docker volume prune -f

## 🗑️ 完全清理(包括镜像)
clean-all:
	@echo "$(BLUE)🗑️ 完全清理所有资源...$(NC)"
	docker-compose down --volumes --remove-orphans --rmi all
	docker system prune -a -f
	docker volume prune -f

## 📊 显示Docker使用情况
docker-stats:
	@echo "$(BLUE)📊 Docker资源使用情况:$(NC)"
	@echo ""
	@echo "$(GREEN)容器状态:$(NC)"
	@docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
	@echo ""
	@echo "$(GREEN)镜像大小:$(NC)"
	@docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | grep url-shortener || echo "无相关镜像"
	@echo ""
	@echo "$(GREEN)磁盘使用:$(NC)"
	@docker system df

## 📦 导出镜像
export:
	@echo "$(BLUE)📦 导出镜像...$(NC)"
	docker save url-shortener-backend:latest | gzip > url-shortener-backend.tar.gz
	docker save url-shortener-frontend:latest | gzip > url-shortener-frontend.tar.gz
	@echo "$(GREEN)✅ 镜像已导出为:$(NC)"
	@echo "  - url-shortener-backend.tar.gz"
	@echo "  - url-shortener-frontend.tar.gz"

## 📥 导入镜像
import:
	@echo "$(BLUE)📥 导入镜像...$(NC)"
	@if [ -f "url-shortener-backend.tar.gz" ]; then \
		gunzip -c url-shortener-backend.tar.gz | docker load; \
	fi
	@if [ -f "url-shortener-frontend.tar.gz" ]; then \
		gunzip -c url-shortener-frontend.tar.gz | docker load; \
	fi

## ⚡ 快速重新部署
redeploy: stop clean build start status

## 🔍 检查端口占用
check-ports:
	@echo "$(BLUE)🔍 检查端口占用情况:$(NC)"
	@echo "端口 3000:"
	@lsof -i :3000 || echo "  端口 3000 未被占用"
	@echo "端口 8080:"
	@lsof -i :8080 || echo "  端口 8080 未被占用"