"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Dashboard = exports.BootPortal = exports.bootBrand = void 0;
/**
 * packages/boot 自动装配入口（类似 spring.factories · 约定优于配置）。
 *
 * 约定：每个 feature = src/features/<name>/ 目录（含 ui/model/api 分层），
 * 在本清单登记后可被 prepare.mjs 自动装配到各端。
 * 新增专属功能 = 加一个 feature 目录 + 在此登记（或由 prepare.mjs 自动扫描）。
 */
var boot_1 = require("./boot");
Object.defineProperty(exports, "bootBrand", { enumerable: true, get: function () { return boot_1.bootBrand; } });
// —— features 自动装配清单 ——
var BootPortal_1 = require("./features/portal/ui/admin/BootPortal");
Object.defineProperty(exports, "BootPortal", { enumerable: true, get: function () { return BootPortal_1.default; } });
var Dashboard_1 = require("./features/dashboard/ui/admin/Dashboard");
Object.defineProperty(exports, "Dashboard", { enumerable: true, get: function () { return Dashboard_1.default; } });
