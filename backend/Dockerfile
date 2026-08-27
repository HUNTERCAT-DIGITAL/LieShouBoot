# LieShouBoot 单体后端镜像 · multi-stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY . /workspace
# 阿里云 Maven 镜像加速（国内构建）
COPY .m2-settings.xml /root/.m2/settings.xml
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /workspace/target/lieshouboot-backend-*.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
