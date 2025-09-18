# ==================================================
# React 前端应用 Dockerfile - 多阶段构建
# ==================================================

# 第一阶段：构建阶段
FROM node:18-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制 package 文件
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源代码
COPY . .

# 设置环境变量（生产环境API地址）
ARG REACT_APP_API_BASE_URL=http://localhost:8080
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

# 构建生产版本
RUN npm run build

# ==================================================
# 第二阶段：Nginx 服务器
FROM nginx:alpine

# 设置维护者信息
LABEL maintainer="your-email@example.com"
LABEL description="URL Shortener React Frontend"

# 复制构建产物到 Nginx 目录
COPY --from=builder /app/build /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]