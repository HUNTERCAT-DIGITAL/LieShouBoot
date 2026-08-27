#!/usr/bin/env node
/**
 * LieShouBoot · prepare.mjs
 * 生成 admin-web 内的版别注入文件 editions/boot.extra.ts，
 * 把 LieShouBoot 品牌 + 专属门户（packages/boot）注入到通用前端。
 *
 * 参照客户仓 deploy:prepare.mjs 模式：注入文件生成在 submodule 内，
 * 不随主仓提交，部署时运行本脚本再生。
 * 用相对路径引用 packages/boot（不依赖 admin-web 的 @lieshoucloud/<client> alias——
 * 该 alias 对客户仓根挂载成立，而 LieShouBoot 挂载在 apps/admin，路径不同）。
 *
 * 用法: node scripts/prepare.mjs [仓库根]
 */
import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(process.argv[2] ?? '.');
const editionsDir = path.join(root, 'apps/admin/src/config/editions');
const target = path.join(editionsDir, 'boot.extra.ts');
const bootSrc = path.join(root, 'packages/boot/src');

if (!fs.existsSync(bootSrc)) {
  console.error(`❌ packages/boot 不存在: ${bootSrc}`);
  process.exit(1);
}

// editions/ → packages/boot/src 的相对路径（如 ../../../../packages/boot/src）
const relBoot = path.relative(editionsDir, bootSrc).replace(/\\/g, '/');
const relPortal = `${relBoot}/portal/BootPortal`;

const content = `/**
 * boot.extra.ts · 由 LieShouBoot scripts/prepare.mjs 生成，勿手改。
 * 把 LieShouBoot 品牌 + 专属门户注入通用前端（getEdition 叠加本 extra）。
 */
import { bootBrand } from '${relBoot}';

export default {
  ...bootBrand,
  portal: { load: () => import('${relPortal}') },
};
`;

fs.mkdirSync(editionsDir, { recursive: true });
fs.writeFileSync(target, content);
console.log(`✅ 已生成 ${target}\n   boot 引用: ${relBoot}`);
