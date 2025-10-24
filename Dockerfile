# syntax=docker/dockerfile:1.6

# ===== Build Stage =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# 提前拉取依赖，加速构建
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# 复制源码并打包
COPY src ./src
RUN mvn -q -B -DskipTests package

# ===== Runtime Stage =====
FROM eclipse-temurin:17-jre-alpine
LABEL org.opencontainers.image.title="Java_gdzc" \
      org.opencontainers.image.description="资产管理系统（Spring Boot）" \
      org.opencontainers.image.vendor="gdzc" \
      org.opencontainers.image.version="latest"

ENV TZ=Asia/Shanghai \
    LANG=zh_CN.UTF-8 \
    LC_ALL=zh_CN.UTF-8 \
    JAVA_OPTS="" \
    SERVER_PORT=13697

# 时区与依赖（如需字体或本地化可在此安装）
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 13697
USER app:app

# 支持通过环境变量调整JVM与端口
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${SERVER_PORT}" ]