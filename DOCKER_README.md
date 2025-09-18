# 🐳 URL Shortener Docker 部署指南

本文档介绍如何使用 Docker 部署 URL 短链接服务（前后端一体化方案）。

## 📋 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 至少 2GB 可用内存
- 端口 3000 和 8080 未被占用

## 🚀 快速开始

### 方法一：使用 Docker Compose（推荐）

```bash
# 1. 进入项目根目录
cd URL_Shortener

# 2. 一键构建并启动所有服务
docker-compose up --build

# 3. 后台运行（可选）
docker-compose up -d --build
```

### 方法二：分别构建

```bash
# 构建后端镜像
docker build -t url-shortener-backend .

# 构建前端镜像
docker build -t url-shortener-frontend ./frontend

# 创建网络
docker network create url-shortener-network

# 运行后端
docker run -d \
  --name backend \
  --network url-shortener-network \
  -p 8080:8080 \
  url-shortener-backend

# 运行前端
docker run -d \
  --name frontend \
  --network url-shortener-network \
  -p 3000:80 \
  url-shortener-frontend
```

## 🌐 访问应用

- **前端界面**: http://localhost:3000
- **后端API**: http://localhost:8080
- **H2数据库控制台**: http://localhost:8080/h2-console
- **健康检查**: http://localhost:8080/actuator/health

## ⚙️ 环境变量配置

### 后端环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `SPRING_PROFILES_ACTIVE` | prod | Spring 配置文件 |
| `JAVA_OPTS` | -Xmx512m -Xms256m | JVM 参数 |
| `SERVER_PORT` | 8080 | 服务端口 |

### 前端环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `REACT_APP_API_BASE_URL` | http://localhost:8080 | 后端API地址 |

### 自定义配置示例

```bash
# 修改 docker-compose.yml 中的环境变量
services:
  backend:
    environment:
      - JAVA_OPTS=-Xmx1g -Xms512m
      - SPRING_PROFILES_ACTIVE=prod

  frontend:
    build:
      args:
        - REACT_APP_API_BASE_URL=https://api.yoursite.com
```

## 📊 容器监控

### 查看容器状态

```bash
# 查看所有容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 资源使用情况

```bash
# 查看容器资源使用
docker stats

# 查看磁盘使用
docker system df
```

## 🔧 维护操作

### 更新应用

```bash
# 停止服务
docker-compose down

# 重新构建并启动
docker-compose up --build -d

# 查看更新后的状态
docker-compose ps
```

### 清理资源

```bash
# 停止并删除容器
docker-compose down

# 删除所有相关镜像
docker-compose down --rmi all

# 删除数据卷（谨慎操作）
docker-compose down -v

# 清理未使用的 Docker 资源
docker system prune -a
```

## 🐛 故障排除

### 常见问题

1. **端口被占用**
   ```bash
   # 查找占用端口的进程
   lsof -i :8080
   lsof -i :3000

   # 修改 docker-compose.yml 中的端口映射
   ports:
     - "8081:8080"  # 改用 8081 端口
   ```

2. **内存不足**
   ```bash
   # 减少 JVM 内存使用
   environment:
     - JAVA_OPTS=-Xmx256m -Xms128m
   ```

3. **构建失败**
   ```bash
   # 清理 Docker 缓存后重新构建
   docker builder prune
   docker-compose build --no-cache
   ```

4. **容器无法启动**
   ```bash
   # 查看详细错误日志
   docker-compose logs backend

   # 进入容器调试
   docker exec -it url-shortener-backend sh
   ```

### 健康检查

```bash
# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查前端可访问性
curl -I http://localhost:3000

# 测试API功能
curl -X POST http://localhost:8080/api/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'
```

## 🔒 生产环境部署

### 安全配置

1. **移除开发环境配置**
   ```yaml
   # 生产环境不暴露 H2 控制台
   # 注释掉 docker-compose.yml 中的相关配置
   ```

2. **使用外部数据库**
   ```yaml
   backend:
     environment:
       - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/urlshortener
       - SPRING_DATASOURCE_USERNAME=app
       - SPRING_DATASOURCE_PASSWORD=your_password
     depends_on:
       - db

   db:
     image: mysql:8.0
     environment:
       MYSQL_DATABASE: urlshortener
       MYSQL_USER: app
       MYSQL_PASSWORD: your_password
       MYSQL_ROOT_PASSWORD: root_password
   ```

3. **启用HTTPS**
   ```yaml
   frontend:
     environment:
       - REACT_APP_API_BASE_URL=https://your-api-domain.com
   ```

### 性能优化

```yaml
backend:
  environment:
    - JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 1G
      reservations:
        cpus: '1'
        memory: 512M

frontend:
  deploy:
    resources:
      limits:
        cpus: '0.5'
        memory: 128M
```

## 📈 监控和日志

### 日志收集

```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### Prometheus 监控（可选）

```yaml
services:
  backend:
    environment:
      - management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

## 🆘 支持

如果遇到问题：

1. 查看日志：`docker-compose logs`
2. 检查容器状态：`docker-compose ps`
3. 验证网络连接：`docker network ls`
4. 检查端口占用：`netstat -tulpn | grep :8080`

---

**注意**：首次构建可能需要较长时间下载依赖，请耐心等待。