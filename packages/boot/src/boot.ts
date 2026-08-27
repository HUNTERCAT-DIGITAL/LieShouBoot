/**
 * LieShouBoot 品牌配置（boot edition 增量，merge 进 generic 基础版）。
 *
 * 经 admin-web 的 extra 注入机制（getEdition 叠加 getExtraEdition），
 * 由 scripts/prepare.mjs 生成 editions/boot.extra.ts 引用本配置 + 专属门户。
 * 只覆盖差异字段，其余继承 generic（开源演示语境）。
 */
export const bootBrand = {
  brandName: 'LieShouBoot',
  slogan: '开源的轻量单体版 · 一条命令跑通',
  heroDesc:
    'LieShouBoot（猎手云单体版）：把微服务版 LieShouCloud 重组为单个 Spring Boot 应用，认证/用户/管理/审批一体内置，前端复用四端，一条 docker compose 命令即可自部署体验。',
  logo: '/logo.png',
  primaryColor: '#1677ff',
  defaultTenantCode: 'huntercat',
  // 与 generic 一致：开源交付包不暴露闭源商业模块入口
  hiddenMenus: ['/customer', '/inventory', '/finance', '/iot', '/legal'],
  industriesText: ['单体架构', '一键部署', '完整认证', '审批流', '四端覆盖', '开源'],
  stats: [
    { label: '单体应用', value: '1' },
    { label: '内置模块', value: '5' },
    { label: '多端支持', value: '4' },
    { label: '开源协议', value: 'Apache-2.0' },
  ],
  faq: [
    {
      q: 'LieShouBoot 和 LieShouCloud 什么关系？',
      a: '同一开源产品线的两种形态：LieShouCloud 是微服务版（可扩展复杂部署），LieShouBoot 是单体版（更低上手门槛、一条命令跑通）。前端四端与共享包完全复用。',
    },
    {
      q: '如何体验？',
      a: 'git clone 后 git submodule update --init --recursive，再 docker compose up 即可一键起全栈；默认管理员 admin / admin123（租户 huntercat）。',
    },
    {
      q: '单体版能升级到微服务版吗？',
      a: '业务模块与数据模型同源，可在保留数据库的前提下迁移到 LieShouCloud 微服务版或 LieShouCloudPro 商业版。',
    },
  ],
  cta: {
    title: '开始使用 LieShouBoot',
    desc: '开源单体版，开箱即用，可自部署 / 二次开发。',
    buttonText: '免费体验',
  },
};
