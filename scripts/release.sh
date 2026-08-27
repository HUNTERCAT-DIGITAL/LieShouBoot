#!/usr/bin/env bash
# ============================================================
# LieShouBoot · 组合发布（结合四端开源仓库 · 组合非复制）
#   ./scripts/release.sh [--with-mini] [--with-desktop] [--with-mobile] [--all]
# 输出: dist/lieshouboot-release/（jar + 各端产物 + deploy + 组合清单）
# ============================================================
set -euo pipefail
cd "$(dirname "$0")/.."

MINI=0; DESKTOP=0; MOBILE=0
for arg in "$@"; do
  case "$arg" in
    --with-mini) MINI=1 ;;
    --with-desktop) DESKTOP=1 ;;
    --with-mobile) MOBILE=1 ;;
    --all) MINI=1; DESKTOP=1; MOBILE=1 ;;
    *) echo "❌ 未知选项: $arg"; exit 1 ;;
  esac
done

OUT="dist/lieshouboot-release"
echo "=============================================="
echo "LieShouBoot 组合发布 · 输出: $OUT"
echo "  mini=$MINI desktop=$DESKTOP mobile=$MOBILE"
echo "=============================================="

# ① submodule 初始化（四端 + open）
echo "① 初始化 submodule..."
git submodule update --init --recursive 2>/dev/null || echo "  (已初始化或无需)"

# ② 核心：admin 内嵌 → backend jar（全栈单体）
echo "② 构建全栈单体（admin → 内嵌 → backend jar）..."
./scripts/build-monolith.sh

# ③ 小程序（可选 · Taro → dist/）
if [ "$MINI" = 1 ]; then
  echo "③ 构建小程序（Taro）..."
  (cd apps/mini-program && pnpm install --silent && pnpm build:weapp)
fi

# ④ 桌面端（可选 · Tauri，需 Rust）
if [ "$DESKTOP" = 1 ]; then
  echo "④ 构建桌面端（Tauri · 需 Rust toolchain）..."
  (cd apps/desktop && pnpm install --silent && pnpm tauri:build)
fi

# ⑤ 移动端（可选 · Expo）
if [ "$MOBILE" = 1 ]; then
  echo "⑤ 构建移动端（Expo）..."
  (cd apps/mobile && pnpm install --silent && pnpm build)
fi

# ⑥ 组合输出
echo "⑥ 组装发布目录..."
rm -rf "$OUT"
mkdir -p "$OUT"/{backend,deploy}

# backend jar（含 admin 内嵌前端 · 仅最新 RELEASE，排除 SNAPSHOT 残留）
cp $(ls -t backend/target/lieshouboot-backend-*.jar | grep -v SNAPSHOT | head -1) "$OUT/backend/"
cp docker-compose.yml .env.example "$OUT/deploy/"

# 各端产物（组合 · 非复制源码）
[ "$MINI" = 1 ] && [ -d apps/mini-program/dist ] && cp -r apps/mini-program/dist "$OUT/mini-program"
[ "$DESKTOP" = 1 ] && [ -d apps/desktop/src-tauri/target/release/bundle ] && cp -r apps/desktop/src-tauri/target/release/bundle "$OUT/desktop"
[ "$MOBILE" = 1 ] && [ -d apps/mobile/dist ] && cp -r apps/mobile/dist "$OUT/mobile"
cp deploy/bt-panel-nginx/*.conf "$OUT/deploy/" 2>/dev/null || true

# 组合清单（记录四端 pin，可复现）
{
  echo "# LieShouBoot 发布组合清单"
  echo ""
  echo "| 组件 | 来源仓库 | pin |"
  echo "| --- | --- | --- |"
  for s in apps/admin apps/desktop apps/mobile apps/mini-program open; do
    pin=$(git -C "$s" rev-parse --short HEAD 2>/dev/null || echo "N/A")
    url=$(git -C "$s" config --get remote.origin.url 2>/dev/null || echo "N/A")
    echo "| \`$s\` | $url | \`$pin\` |"
  done
  echo ""
  echo "## 使用"
  echo "- backend jar: \`java -jar backend/lieshouboot-backend-*.jar\`（含前端，端口 43115）"
  echo "- 或 \`docker compose -f deploy/docker-compose.yml up -d\`"
} > "$OUT/README.md"

echo ""
echo "=============================================="
echo "✅ 组合发布完成: $OUT/"
ls -la "$OUT/"
echo "=============================================="
