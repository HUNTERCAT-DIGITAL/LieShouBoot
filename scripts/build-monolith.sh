#!/usr/bin/env bash
# LieShouBoot 全栈单体构建：前端 build → 内嵌 static → 后端打包
# 注意：全部使用绝对路径（ROOT），避免 cd 链位置漂移
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "① 生成前端版别注入（自动装配 features → editions/boot.extra.ts）"
node "$ROOT/scripts/prepare.mjs" "$ROOT"

echo "② 构建前端（admin-web vite build）"
(cd "$ROOT/apps/admin" && npx vite build)

echo "③ 内嵌前端产物到后端 static"
rm -rf "$ROOT/backend/src/main/resources/static"
mkdir -p "$ROOT/backend/src/main/resources/static"
cp -r "$ROOT/apps/admin/dist/"* "$ROOT/backend/src/main/resources/static/"

echo "④ 打包后端（聚合构建：framework submodule + backend，含内嵌前端）"
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}
mvn -B -ntp -f "$ROOT/pom.xml" -DskipTests package

echo "✅ 完成: backend/target/lieshouboot-backend-*.jar（单进程起全栈）"
