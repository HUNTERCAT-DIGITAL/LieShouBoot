# LieShouBoot 单体后端镜像 · multi-stage
# 构建上下文 = 仓库根（docker-compose build context: .），聚合构建 framework submodule + backend
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY . /workspace
# 阿里云 Maven 镜像加速（国内构建）
COPY backend/.m2-settings.xml /root/.m2/settings.xml
# 聚合构建：framework（submodule）+ backend，reactor 内解析 framework-* 依赖，无需 install
RUN mvn -B -ntp -f /workspace/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /workspace/backend/target/lieshouboot-backend-*.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
