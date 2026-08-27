#!/usr/bin/env node
/**
 * lieshou-boot · prepare.mjs（四端自动装配器 · 约定优于配置）
 *
 * 扫描 packages/boot/src/features/，生成四端注入（客户聚合仓模式）：
 *   - admin-web  : apps/admin/src/config/editions/boot.extra.ts（品牌+门户+extraRoutes）
 *   - desktop    : apps/desktop/src/config/editions/boot.extra.ts（extraRoutes）
 *   - mini-program: src/config/editions/extra.ts（EXTRA_PAGES/ENTRIES）+ 薄壳页 pages/boot/workspace/
 *   - mobile     : app/(main)/boot/workspace.tsx 薄壳页（Expo Router 文件路由自动注册）
 *
 * 约定：features/<name>/ui/admin/*.tsx = 专属页面（admin/desktop 注入 extraRoutes）；
 *       features/portal/ui/admin/BootPortal.tsx 特殊 = 门户（portal 槽位）。
 * 薄壳页内联端专属 UI + import bootBrand（品牌单源）。
 * 用法: node scripts/prepare.mjs [仓库根]
 */
import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(process.argv[2] ?? '.');
const featuresDir = path.join(root, 'packages/boot/src/features');
const bootSrc = path.join(root, 'packages/boot/src');

if (!fs.existsSync(bootSrc)) {
  console.error(`❌ packages/boot 不存在: ${bootSrc}`);
  process.exit(1);
}

// —— 扫描 features（约定：ui/admin/*.tsx 为专属页面）——
const adminPages = [];
if (fs.existsSync(featuresDir)) {
  for (const feat of fs.readdirSync(featuresDir)) {
    const uiAdmin = path.join(featuresDir, feat, 'ui', 'admin');
    if (!fs.existsSync(uiAdmin)) continue;
    for (const f of fs.readdirSync(uiAdmin).filter((x) => x.endsWith('.tsx'))) {
      adminPages.push({ feat, name: f.replace(/\.tsx?$/, ''), file: `${feat}/ui/admin/${f.replace(/\.tsx?$/, '')}` });
    }
  }
}
const nonPortal = adminPages.filter((p) => p.feat !== 'portal');

// 相对路径助手
const rel = (from, to) => path.relative(from, to).replace(/\\/g, '/');

// —— ① admin-web ——
{
  const editionsDir = path.join(root, 'apps/admin/src/config/editions');
  const relBoot = rel(editionsDir, bootSrc);
  const content = `/**
 * boot.extra.ts · 由 lieshou-boot scripts/prepare.mjs 自动生成，勿手改。
 * 自动装配 features：品牌 + 门户 + 专属路由。
 */
import { bootBrand } from '@lieshoucloud/boot';

export default {
  ...bootBrand,
  portal: { load: () => import('@lieshoucloud/boot/features/portal/ui/admin/BootPortal') },
  extraRoutes: [
${nonPortal.map((p) => `  { path: '/${p.feat}', load: () => import('@lieshoucloud/boot/features/${p.file}'), menu: { name: '${p.name}' } },`).join('\n')}
  ],
};
`;
  fs.mkdirSync(editionsDir, { recursive: true });
  fs.writeFileSync(path.join(editionsDir, 'boot.extra.ts'), content);
  console.log('✅ admin   : editions/boot.extra.ts');
}

// —— ② desktop（DesktopEdition · extraRoutes 无 menu）——
{
  const editionsDir = path.join(root, 'apps/desktop/src/config/editions');
  const relBoot = rel(editionsDir, bootSrc);
  const content = `/**
 * boot.extra.ts · 由 lieshou-boot scripts/prepare.mjs 自动生成，勿手改。
 * 自动装配 features：专属路由（DesktopEdition.extraRoutes）。
 */
import type { DesktopEdition } from './types';

export default {
  extraRoutes: [
${nonPortal.map((p) => `  { path: '/${p.feat}', load: () => import('@lieshoucloud/boot/features/${p.file}') },`).join('\n')}
  ],
} satisfies Partial<DesktopEdition>;
`;
  fs.mkdirSync(editionsDir, { recursive: true });
  fs.writeFileSync(path.join(editionsDir, 'boot.extra.ts'), content);
  console.log('✅ desktop: editions/boot.extra.ts');
}

// —— ③ mini-program（EXTRA_PAGES/ENTRIES + 薄壳页）——
{
  const extraFile = path.join(root, 'apps/mini-program/src/config/editions/extra.ts');
  const pageDir = path.join(root, 'apps/mini-program/src/pages/boot/workspace');
  const relBoot = rel(path.join(pageDir), bootSrc);
  const entries = nonPortal.map(
    (p) => `  { key: 'boot-${p.feat}', label: '🛠️ ${p.name}', url: '/pages/boot/workspace/index' },`
  ).join('\n');

  const extraContent = `/**
 * lieshou-boot 专属页面注册（scripts/prepare.mjs 生成 · 勿手改/勿提交）.
 */
export interface ClientEntry {
  key: string;
  label: string;
  url: string;
}

export const EXTRA_PAGES: string[] = ['pages/boot/workspace/index'];

export const EXTRA_ENTRIES: ClientEntry[] = [
${entries}
];
`;
  fs.mkdirSync(path.dirname(extraFile), { recursive: true });
  fs.writeFileSync(extraFile, extraContent);

  // 薄壳页（内联 Taro UI + 品牌单源）
  const shell = `/**
 * lieshou-boot 专属工作台薄壳（scripts/prepare.mjs 生成 · 勿手改/勿提交）.
 * 内联 Taro UI；品牌文案来自 packages/boot（bootBrand 单源）。
 */
import { Text, View } from '@tarojs/components';
import { bootBrand } from '@lieshoucloud/boot';

export default function BootWorkspace() {
  return (
    <View style={{ minHeight: '100vh', padding: '24rpx', backgroundColor: '#f7f7f7' }}>
      <View style={{ marginBottom: '24rpx' }}>
        <Text style={{ fontSize: '34rpx', fontWeight: 700, color: '#1f1f1f' }}>{bootBrand.brandName} · 专属工作台</Text>
      </View>
      <View style={{ backgroundColor: '#fff', borderRadius: '12rpx', padding: '24rpx', border: '1rpx solid #eee' }}>
        <Text style={{ fontSize: '26rpx', color: '#666' }}>{bootBrand.slogan}</Text>
      </View>
      <Text style={{ fontSize: '22rpx', color: '#999', marginTop: '16rpx' }}>
        本页由 prepare.mjs 注入（薄壳 + 品牌单源）；专属业务页面按 feature 扩展。
      </Text>
    </View>
  );
}
`;
  fs.mkdirSync(pageDir, { recursive: true });
  fs.writeFileSync(path.join(pageDir, 'index.tsx'), shell);
  fs.writeFileSync(
    path.join(pageDir, 'index.config.ts'),
    `// 微信小程序页面配置: boot/workspace（prepare.mjs 注入）
export default {
  navigationBarTitleText: "lieshou-boot 工作台",
} as const;
`
  );
  console.log('✅ mini    : editions/extra.ts + pages/boot/workspace/ 薄壳');
}

// —— ④ mobile（薄壳页 · Expo Router 文件路由自动注册）——
{
  const pageDir = path.join(root, 'apps/mobile/app/(main)/boot');
  const relBoot = rel(path.join(pageDir), bootSrc);
  const shell = `/**
 * lieshou-boot 专属工作台薄壳（scripts/prepare.mjs 生成 · 勿手改/勿提交）.
 * 内联 RN UI；品牌文案来自 packages/boot（bootBrand 单源）。
 */
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { bootBrand } from '@lieshoucloud/boot';

export default function BootWorkspace() {
  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>{bootBrand.brandName} · 专属工作台</Text>
      <View style={styles.card}>
        <Text style={styles.cardText}>{bootBrand.slogan}</Text>
      </View>
      <Text style={styles.foot}>本页由 prepare.mjs 注入（薄壳 + 品牌单源）。</Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f7f7f7', padding: 24 },
  title: { fontSize: 22, fontWeight: '700', color: '#1f1f1f', marginBottom: 16 },
  card: { backgroundColor: '#fff', borderRadius: 12, padding: 24, borderWidth: StyleSheet.hairlineWidth, borderColor: '#eee' },
  cardText: { fontSize: 15, color: '#666' },
  foot: { fontSize: 12, color: '#999', marginTop: 16 },
});
`;
  fs.mkdirSync(pageDir, { recursive: true });
  fs.writeFileSync(path.join(pageDir, 'workspace.tsx'), shell);
  console.log('✅ mobile  : app/(main)/boot/workspace.tsx 薄壳');
}

console.log(`\n🎉 四端注入完成 · features: ${adminPages.map((p) => p.feat).join(', ') || '无'}`);
