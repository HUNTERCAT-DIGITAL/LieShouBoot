#!/usr/bin/env node
/**
 * LieShouBoot · prepare.mjs（自动装配器 · 约定优于配置）
 *
 * 扫描 packages/boot/src/features/ 的 admin 页面，自动生成 admin-web 的
 * editions/boot.extra.ts（品牌 + 门户 + 专属路由），零手配。
 *
 * 约定：features/<name>/ui/admin/<X>.tsx 默认导出 = 专属页面；
 *       features/portal/ui/admin/BootPortal.tsx 特殊 = 门户（portal 槽位）。
 * 用法: node scripts/prepare.mjs [仓库根]
 */
import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(process.argv[2] ?? '.');
const editionsDir = path.join(root, 'apps/admin/src/config/editions');
const target = path.join(editionsDir, 'boot.extra.ts');
const featuresDir = path.join(root, 'packages/boot/src/features');
const bootSrc = path.join(root, 'packages/boot/src');

if (!fs.existsSync(bootSrc)) {
  console.error(`❌ packages/boot 不存在: ${bootSrc}`);
  process.exit(1);
}

// 1) 扫描 features（约定：ui/admin/*.tsx）
const adminPages = [];
if (fs.existsSync(featuresDir)) {
  for (const feat of fs.readdirSync(featuresDir)) {
    const uiAdmin = path.join(featuresDir, feat, 'ui', 'admin');
    if (!fs.existsSync(uiAdmin)) continue;
    for (const f of fs.readdirSync(uiAdmin).filter((x) => x.endsWith('.tsx') || x.endsWith('.tsx'))) {
      const name = f.replace(/\.tsx?$/, '');
      adminPages.push({ feat, name, file: `${feat}/ui/admin/${name}` });
    }
  }
}

// 2) 相对路径（editions/ → packages/boot/src）
const relBoot = path.relative(editionsDir, bootSrc).replace(/\\/g, '/');
const relPortal = `${relBoot}/features/portal/ui/admin/BootPortal`;

// 3) 生成注入文件
const extraRoutes = adminPages
  .filter((p) => p.feat !== 'portal')
  .map(
    (p) => `  { path: '/${p.feat}', load: () => import('${relBoot}/features/${p.file}'), menu: { name: '${p.name}' } },`
  )
  .join('\n');

const content = `/**
 * boot.extra.ts · 由 LieShouBoot scripts/prepare.mjs 自动生成，勿手改。
 * 扫描 features/ 自动装配：品牌 + 门户 + 专属路由。
 */
import { bootBrand } from '${relBoot}';

export default {
  ...bootBrand,
  portal: { load: () => import('${relPortal}') },
  extraRoutes: [
${extraRoutes}
  ],
};
`;

fs.mkdirSync(editionsDir, { recursive: true });
fs.writeFileSync(target, content);
console.log(`✅ 已生成 ${target}`);
console.log(`   features 装配: ${adminPages.map((p) => p.feat).join(', ') || '无'}`);
