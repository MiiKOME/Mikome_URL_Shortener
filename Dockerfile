# ==================================================
# Spring Boot 应用 Dockerfile - 多阶段构建
# ==================================================

# 第一阶段：构建阶段
FROM openjdk:17-jdk-alpine AS builder

# 设置工作目录
WORKDIR /app

# 安装 Maven（Alpine Linux）
RUN apk add --no-cache maven

# 复制 Maven 配置文件
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖（利用 Docker 层缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# ==================================================
# 第二阶段：运行时镜像
FROM openjdk:17-jre-alpine

# 设置维护者信息
LABEL maintainer="your-email@example.com"
LABEL description="URL Shortener Spring Boot Application"

# 创建应用用户（安全最佳实践）
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# 切换到非 root 用户
USER appuser

# 设置环境变量
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]