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
# 强制 Java 21（用户环境可能默认 17，release 21 会编译失败）
JAVA_21_CANDIDATES=(/usr/lib/jvm/java-21-openjdk-amd64 /usr/local/openjdk-21 /opt/java/openjdk "$HOME/.sdkman/candidates/java/current")
if ! "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"21'; then
  for j in "${JAVA_21_CANDIDATES[@]}"; do
    if [ -x "$j/bin/java" ] && "$j/bin/java" -version 2>&1 | grep -q '"21'; then
      export JAVA_HOME="$j"
      break
    fi
  done
fi
mvn -B -ntp -f "$ROOT/pom.xml" -DskipTests package

echo "✅ 完成: backend/target/lieshouboot-backend-*.jar（单进程起全栈）"
