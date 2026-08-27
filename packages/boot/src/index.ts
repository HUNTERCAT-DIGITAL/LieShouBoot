/**
 * packages/boot 自动装配入口（类似 spring.factories · 约定优于配置）。
 *
 * 约定：每个 feature = src/features/<name>/ 目录（含 ui/model/api 分层），
 * 在本清单登记后可被 prepare.mjs 自动装配到各端。
 * 新增专属功能 = 加一个 feature 目录 + 在此登记（或由 prepare.mjs 自动扫描）。
 */
export { bootBrand } from './boot';

// —— features 自动装配清单 ——
export { default as BootPortal } from './features/portal/ui/admin/BootPortal';
export { default as Dashboard } from './features/dashboard/ui/admin/Dashboard';
