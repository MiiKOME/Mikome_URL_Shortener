#!/bin/bash

# URL短链接项目Docker构建脚本
# 使用方法: ./scripts/docker-build.sh [dev|prod] [--no-cache]

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "URL短链接项目Docker构建脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [dev|prod] [--no-cache] [--push]"
    echo ""
    echo "参数:"
    echo "  dev       构建开发环境 (默认)"
    echo "  prod      构建生产环境"
    echo "  --no-cache 不使用Docker缓存"
    echo "  --push    构建后推送到镜像仓库"
    echo "  --help    显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 dev              # 构建开发环境"
    echo "  $0 prod --no-cache  # 无缓存构建生产环境"
    echo "  $0 prod --push      # 构建生产环境并推送"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装或未在PATH中找到"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        log_error "Docker守护进程未运行"
        exit 1
    fi
}

# 检查Docker Compose是否安装
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose未安装"
        exit 1
    fi
}

# 清理旧的容器和镜像
cleanup_old() {
    log_info "清理旧的容器和镜像..."

    # 停止并删除容器
    docker-compose down --remove-orphans 2>/dev/null || true

    # 删除悬空镜像
    docker image prune -f 2>/dev/null || true

    log_success "清理完成"
}

# 构建镜像
build_images() {
    local env=$1
    local no_cache=$2
    local push=$3

    log_info "开始构建 $env 环境镜像..."

    # 构建参数
    local build_args=""
    if [[ "$no_cache" == "true" ]]; then
        build_args="--no-cache"
    fi

    # 根据环境选择compose文件
    local compose_file="docker-compose.yml"
    if [[ "$env" == "prod" ]]; then
        compose_file="docker-compose.prod.yml"
    fi

    # 检查compose文件是否存在
    if [[ ! -f "$compose_file" ]]; then
        log_error "Compose文件 $compose_file 不存在"
        exit 1
    fi

    # 构建镜像
    log_info "使用 $compose_file 构建镜像..."
    docker-compose -f "$compose_file" build $build_args

    # 标记镜像版本
    local timestamp=$(date +"%Y%m%d-%H%M%S")
    local git_hash=""
    if command -v git &> /dev/null && git rev-parse --git-dir > /dev/null 2>&1; then
        git_hash=$(git rev-parse --short HEAD)
        timestamp="${timestamp}-${git_hash}"
    fi

    docker tag url-shortener-backend:latest "url-shortener-backend:${timestamp}" 2>/dev/null || true
    docker tag url-shortener-frontend:latest "url-shortener-frontend:${timestamp}" 2>/dev/null || true

    log_success "镜像构建完成"

    # 推送镜像（如果指定）
    if [[ "$push" == "true" ]]; then
        push_images "$timestamp"
    fi

    # 显示构建的镜像
    log_info "构建的镜像:"
    docker images | grep url-shortener || true
}

# 推送镜像到仓库
push_images() {
    local tag=$1

    log_warning "推送功能需要配置镜像仓库地址"
    log_info "示例配置:"
    echo "  docker tag url-shortener-backend:latest your-registry/url-shortener-backend:$tag"
    echo "  docker push your-registry/url-shortener-backend:$tag"
}

# 验证构建结果
verify_build() {
    log_info "验证构建结果..."

    # 检查镜像是否存在
    if docker images | grep -q "url-shortener-backend"; then
        log_success "后端镜像构建成功"
    else
        log_error "后端镜像构建失败"
        exit 1
    fi

    if docker images | grep -q "url-shortener-frontend"; then
        log_success "前端镜像构建成功"
    else
        log_error "前端镜像构建失败"
        exit 1
    fi
}

# 显示构建后的操作提示
show_next_steps() {
    local env=$1

    log_success "构建完成！"
    echo ""
    log_info "后续操作:"

    if [[ "$env" == "dev" ]]; then
        echo "  启动开发环境: docker-compose up -d"
        echo "  查看日志:     docker-compose logs -f"
        echo "  停止服务:     docker-compose down"
    else
        echo "  启动生产环境: docker-compose -f docker-compose.prod.yml up -d"
        echo "  查看日志:     docker-compose -f docker-compose.prod.yml logs -f"
        echo "  停止服务:     docker-compose -f docker-compose.prod.yml down"
    fi

    echo "  访问应用:     http://localhost:3000"
    echo "  API接口:      http://localhost:8080"
    echo ""
    log_info "镜像信息:"
    docker images | grep url-shortener | head -10
}

# 主函数
main() {
    # 参数解析
    local env="dev"
    local no_cache="false"
    local push="false"

    while [[ $# -gt 0 ]]; do
        case $1 in
            dev|prod)
                env="$1"
                shift
                ;;
            --no-cache)
                no_cache="true"
                shift
                ;;
            --push)
                push="true"
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done

    # 切换到项目根目录
    cd "$(dirname "$0")/.."

    log_info "开始构建URL短链接项目 ($env 环境)"

    # 检查依赖
    check_docker
    check_docker_compose

    # 清理旧资源
    cleanup_old

    # 构建镜像
    build_images "$env" "$no_cache" "$push"

    # 验证构建
    verify_build

    # 显示后续步骤
    show_next_steps "$env"
}

# 运行主函数
main "$@"