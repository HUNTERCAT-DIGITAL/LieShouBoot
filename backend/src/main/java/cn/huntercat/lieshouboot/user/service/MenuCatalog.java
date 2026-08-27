package cn.huntercat.lieshouboot.user.service;

import java.util.List;

/**
 * 默认菜单清单（后端裁决 · ADR-0024 Phase 2 阶段 4）.
 *
 * <p>镜像 admin-web 前端 {@code src/layouts/_defaultProps.tsx} 的 route 树（路径/名称/图标 key/
 * accessKey 一一对应），后端按当前用户权限码过滤后返回；前端仅做版别裁剪兜底
 * （hiddenMenus / capabilities / legal 开关），不再做权限判断。
 *
 * <p>⚠️ 增删菜单时需同步维护前端 defaultProps 与本清单（后端是权威，前端兜底渲染）。
 */
public final class MenuCatalog {

  private MenuCatalog() {}

  /** 默认菜单树（sort 越小越靠前，与前端声明顺序一致） */
  public static final List<MenuNode> DEFAULT_MENUS =
      List.of(
          MenuNode.leaf("welcome", "/welcome", "欢迎", "smile", 1),
          MenuNode.leaf("profile", "/profile", "个人中心", "user", 2),
          MenuNode.leaf("notification", "/notification", "通知中心", "bell", 3),
          MenuNode.leaf("admin", "/admin", "工作台", "dashboard", 4),
          MenuNode.group(
              "tenant",
              "/tenant",
              "租户管理",
              "cluster",
              "tenant:manage",
              5,
              List.of(
                  MenuNode.leaf("tenant-list", "/tenant/list", "租户列表", "shop", "tenant:manage", 1),
                  MenuNode.leaf("role-list", "/role/list", "角色管理", "safety", "tenant:manage", 2),
                  MenuNode.leaf("audit-list", "/audit/list", "审计日志", "file-search", "tenant:manage", 3))),
          MenuNode.group(
              "user",
              "/user",
              "用户中心",
              "team",
              "user:list",
              6,
              List.of(MenuNode.leaf("user-list", "/user/list", "用户列表", "user", "user:list", 1))),
          MenuNode.group(
              "customer",
              "/customer",
              "CRM 客户",
              "contacts",
              "crm:use",
              7,
              List.of(
                  MenuNode.leaf("customer-list", "/customer/list", "客户列表", "solution", 1),
                  MenuNode.leaf("customer-success", "/customer/success", "客户成功中心", "fund", 2),
                  MenuNode.leaf("lead-list", "/lead/list", "线索管理", "rise", 3),
                  MenuNode.leaf("contact-list", "/contact/list", "联系人", "team", 4),
                  MenuNode.leaf("contract-list", "/contract/list", "合同管理", "file-text", 5),
                  MenuNode.leaf("member-list", "/member/list", "会员管理", "idcard", 6))),
          MenuNode.group(
              "inventory",
              "/inventory",
              "进销存",
              "shop",
              null,
              8,
              List.of(
                  MenuNode.leaf("inventory-list", "/inventory/list", "库存管理", "solution", 1),
                  MenuNode.leaf("quality-list", "/quality/list", "质检追溯", "experiment", 2))),
          MenuNode.group(
              "finance",
              "/finance",
              "财务记账",
              "fund",
              "finance:use",
              9,
              List.of(
                  MenuNode.leaf("finance-list", "/finance/list", "记账本", "solution", "finance:use", 1))),
          MenuNode.group(
              "approval",
              "/approval",
              "审批流",
              "audit",
              "approval:use",
              10,
              List.of(
                  MenuNode.leaf("approval-list", "/approval/list", "审批中心", "solution", "approval:use", 1))),
          MenuNode.group(
              "legal",
              "/legal",
              "案件管理",
              "book",
              "legal:use",
              11,
              List.of(
                  MenuNode.leaf("legal-cases", "/legal/cases", "办案列表", "solution", "legal:use", 1),
                  MenuNode.leaf("legal-knowledge", "/legal/knowledge", "知识资产", "bulb", "legal:use", 2),
                  MenuNode.leaf("legal-growth", "/legal/growth", "专业成长", "rise", "legal:use", 3))),
          MenuNode.group(
              "iot",
              "/iot",
              "物联网",
              "api",
              "iot:monitor",
              12,
              List.of(
                  MenuNode.leaf("iot-cockpit", "/iot/cockpit", "驾驶舱", "radar", "iot:monitor", 1),
                  MenuNode.leaf("iot-overview", "/iot/overview", "监控总览", "dashboard", "iot:monitor", 2),
                  MenuNode.leaf("iot-topo", "/iot/topo", "电网拓扑", "apartment", "iot:monitor", 3),
                  MenuNode.leaf("iot-devices", "/iot/devices", "设备管理", "shop", "iot:config", 4),
                  MenuNode.leaf("iot-products", "/iot/products", "产品物模型", "solution", "iot:config", 5),
                  MenuNode.leaf("iot-rules", "/iot/rules", "规则配置", "safety", "iot:config", 6),
                  MenuNode.leaf("iot-alerts", "/iot/alerts", "告警中心", "file-search", "iot:monitor", 7))));
}
