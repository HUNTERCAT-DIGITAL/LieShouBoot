# AGENTS.md — LieShouBoot（开源全栈单体）

> 猎手云产品线「单体形态」：把 LieShouCloud 微服务版重组为单个 Spring Boot 应用，
> **前端 admin-web 构建产物内嵌 static**（全栈单体，单 jar 起全栈）。

## 架构速览

```
LieShou-framework（业务唯一源 · 0.1.0）→ backend 薄壳（Controller/Adapter/配置）
前端：apps/{admin,desktop,mobile,mini-program} submodule（复用）+ packages/boot 专属
专属代码：packages/boot/src/features/<name>/{ui,model,api}（FSD 分层 · 自动装配）
```

## 关键约定

- **业务改动**：改 LieShou-framework → `mvn install` → 本仓 bump 版本（勿在本仓改业务）
- **前端专属功能**：在 `packages/boot/src/features/` 加目录（ui/model/api 三层）→ `node scripts/prepare.mjs` 自动装配四端
- **生成的注入文件勿提交**：`editions/boot.extra.ts`、`pages/boot/`、`app/(main)/boot/`（prepare.mjs 构建时生成）
- **发布**：`./scripts/release.sh [--with-mini|--with-desktop|--with-mobile]` 组合打包；tag `v*` 触发 CI 发布
- **端口**：8095（前后端同源）· dev 前端 5200 · DB 5433
- **测试**：`cd backend && mvn verify`（framework 需先本地 install）

## 危险操作

- ❌ 改 submodule 内容（apps/*、open/*）——四端改动回各自仓
- ❌ 提交密钥/.env；改 .gitignore、CI workflow 前先确认
