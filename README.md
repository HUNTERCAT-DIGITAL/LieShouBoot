# LieShouBoot · 猎手云开源单体版

> 把猎手云微服务版（LieShouCloud）**重组为单个 Spring Boot 应用**的开源单体版：认证 / 用户 / 管理 / 审批一体内置（业务核心来自 **LieShou-framework**），**前端 admin-web 构建产物内嵌**（全栈单体），一条 `docker compose` 命令即可自部署体验。

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5-green" alt="Spring Boot 3.5"/>
  <img src="https://img.shields.io/badge/framework-0.1.0-blue" alt="framework 0.1.0"/>
  <img src="https://img.shields.io/badge/License-Apache--2.0-brightgreen" alt="Apache-2.0"/>
</p>

## 定位

同一开源产品线的**两种形态**：

| | LieShouBoot（本仓·单体版） | LieShouCloud（微服务版） |
|---|---|---|
| 后端 | **全栈单体**：1 个 Spring Boot jar（API + 内嵌前端），业务核心依赖 LieShou-framework | 微服务（gateway + 5 服务 + Nacos），同样依赖 framework |
| 前端 | admin-web build 产物内嵌 `static/`（单 jar serve） | 四端独立部署（submodule） |
| 上手门槛 | **低**：一条命令全栈跑通 | 较高：需 compose 起 5 服务 + Nacos |
| 演进 | 可平滑迁移到微服务版 / 商业版 | 可扩展复杂部署 |

## 前后端引用关系

```
LieShou-framework（业务核心 · 上游同源唯一）
   ↑ maven 依赖
backend/（单体 Spring Boot 薄壳）
├── src/main/java/.../lieshouboot/    # Controller + 端口实现(Adapter) + Security 配置
│   ├── auth/       AuthController + UserAuthAdapter(实现 UserAuthPort)
│   ├── user/       UserController 等（web 层）
│   ├── approval/   ApprovalController + UserQueryAdapter/Notifier(实现端口)
│   └── common/web/ SpaForwardController(SPA fallback)
├── src/main/resources/static/        # 🆕 前端内嵌产物（admin-web build 输出）
└── src/main/resources/db/migration/  # Flyway V1-V11

前端（构建时引用）
├── apps/admin/            # submodule → LieShouCloud-admin-web（唯一渲染端）
├── open/                  # 前端共享包 submodule（api-client/types/ui/config）
└── packages/boot/         # 专属品牌 + 产品介绍门户（scripts/prepare.mjs 注入）
```

## 快速开始（全栈单体 · 一条命令）

```bash
# 1. clone + 初始化 submodule（apps/admin + open 及其嵌套）
git clone ... && cd LieShouBoot
git submodule update --init --recursive

# 2. 一键构建全栈单体（前端 build → 内嵌 static → 后端打包）
./scripts/build-monolith.sh

# 3. 一键起全栈（仅 postgres + backend 两容器，前端已内嵌）
docker compose up -d --build
```

启动后：
- **前端门户 + API 同源**：`http://localhost:8095`（单 jar serve，专属产品介绍页 + SPA 路由 + `/api/**`）
- 默认管理员：`admin / admin123`（租户 `huntercat`，Flyway seed）

## 开发

### 后端（单体薄壳）

```bash
cd backend && mvn -B -ntp -DskipTests package   # Java 21 + Maven；依赖本地已 install 的 framework 0.1.0
java -jar target/lieshouboot-backend-*.jar       # PORT=8095 DB_PORT=5433 可覆盖
```

> 业务改动：在 LieShou-framework 进行 → `mvn install` → 本仓 bump 版本。

### 前端（admin-web 独立 dev · 可选）

生产用内嵌产物；开发热更新可独立跑：

```bash
cd apps/admin && pnpm install
VITE_DEV_PROXY_TARGET=http://localhost:8095 pnpm dev --port 5200   # http://localhost:5200
```

> 前端品牌/门户定制：改 `packages/boot/` → `node scripts/prepare.mjs` → 重新 `build-monolith.sh`。

## 与商业版关系

- 开源：**LieShouBoot**（本仓，全栈单体）+ **LieShouCloud**（微服务）
- 商业：**LieShouCloudPro**（多租户 + 行业模块 + 商业部署编排）

需要多租户、行业能力或企业级合规时，从 LieShouBoot 平滑升级到 LieShouCloudPro。

## License

Apache-2.0。详见 [LICENSE](./LICENSE)。
