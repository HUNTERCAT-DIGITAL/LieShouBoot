#!/usr/bin/env bash
# LieShouBoot 全栈单体构建：前端 build → 内嵌 static → 后端打包
set -euo pipefail
cd "$(dirname "$0")/.."

echo "① 生成前端版别注入（品牌 + 专属门户）"
node scripts/prepare.mjs

echo "② 构建前端（admin-web vite build）"
cd apps/admin
npx vite build
cd ..

echo "③ 内嵌前端产物到后端 static"
rm -rf backend/src/main/resources/static
mkdir -p backend/src/main/resources/static
cp -r apps/admin/dist/* backend/src/main/resources/static/

echo "④ 打包后端（含内嵌前端）"
cd backend
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}
mvn -B -ntp -DskipTests package
cd ..

echo "✅ 完成: backend/target/lieshouboot-backend-*.jar（单进程起全栈）"
