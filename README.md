# LieShouBoot · 猎手云开源单体版

> 把猎手云微服务版（LieShouCloud）**重组为单个 Spring Boot 应用**的开源单体版：认证 / 用户 / 管理 / 审批一体内置，前端四端完全复用，一条 `docker compose` 命令即可自部署体验。

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5-green" alt="Spring Boot 3.5"/>
  <img src="https://img.shields.io/badge/License-Apache--2.0-brightgreen" alt="Apache-2.0"/>
</p>

## 定位

同一开源产品线的**两种形态**：

| | LieShouBoot（本仓·单体版） | LieShouCloud（微服务版） |
|---|---|---|
| 架构 | 单应用（gateway/user/admin/auth/approval 重组为包内模块） | 微服务（gateway + 5 服务 + Nacos） |
| 上手门槛 | **低**：一条命令全栈跑通 | 较高：需 compose 起 5 服务 + Nacos |
| 前端 | 四端复用（submodule） | 四端复用（submodule） |
| 演进 | 可平滑迁移到微服务版 / 商业版 | 可扩展复杂部署 |

## 目录结构

```
LieShouBoot/
├── backend/            # 单体 Spring Boot 应用（common/user/admin/auth/approval 包内模块）
│   └── src/main/resources/db/migration/   # Flyway V1-V11（user + approval 合并）
├── apps/               # 四端 submodule（admin-web / desktop / mobile / mini-program）
├── open/               # 前端共享包 submodule（LieShouCloud-web）
├── packages/boot/      # LieShouBoot 专属增量包（品牌 + 产品介绍门户页）
├── scripts/prepare.mjs # 生成前端版别注入（editions/boot.extra.ts）
└── deploy/             # 部署编排 + 入口机 nginx conf
```

## 快速开始

```bash
# 1. clone + 初始化全部 submodule
git clone ... && cd LieShouBoot
git submodule update --init --recursive

# 2. 生成前端版别注入（品牌 + 专属门户）
node scripts/prepare.mjs

# 3. 一键起全栈（postgres + 后端 + 前端）
docker compose up -d --build
```

启动后：
- 前端门户：`http://localhost:5200`（专属产品介绍页）
- API：`http://localhost:8095`（单体后端）
- 默认管理员：`admin / admin123`（租户 `huntercat`，Flyway seed）

## 开发

### 后端（单体）

```bash
cd backend && mvn -B -ntp -DskipTests package   # Java 21 + Maven
java -jar target/lieshouboot-backend-*.jar       # PORT=8095 DB_PORT=5433 可覆盖
```

### 前端（admin-web）

```bash
cd apps/admin && pnpm install
VITE_DEV_PROXY_TARGET=http://localhost:8095 pnpm dev --port 5200
```

> 前端品牌/门户定制：改 `packages/boot/` 后重跑 `node scripts/prepare.mjs`。

## 与商业版关系

- 开源：**LieShouBoot**（本仓，单体）+ **LieShouCloud**（微服务）
- 商业：**LieShouCloudPro**（多租户 + 行业模块 + 商业部署编排）

需要多租户、行业能力或企业级合规时，从 LieShouBoot 平滑升级到 LieShouCloudPro。

## License

Apache-2.0。详见 [LICENSE](./LICENSE)。
