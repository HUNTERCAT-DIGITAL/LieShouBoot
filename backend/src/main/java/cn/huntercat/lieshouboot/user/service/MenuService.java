package cn.huntercat.lieshouboot.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 菜单裁决服务（ADR-0024 Phase 2 阶段 4 · 后端权威）.
 *
 * <p>角色 → 权限码推导对齐前端 {@code access.ts derivePermissions}（permissions 表缺失时回退），
 * 再按 accessKey 过滤默认清单：声明 accessKey 的节点需权限码命中，缺省 = 登录可见；
 * 子节点全被过滤的分组一并隐藏。
 */
@Service
public class MenuService {

  /**
   * 租户内业务权限码（非值班员；与前端 access.ts derivePermissions 的 tenantBiz 完全一致）.
   *
   * ⚠️ 单一事实源：后端 MenuService 是权威（ADR-0024 后端裁决），
   * 前端 access.ts 仅保留降级兜底副本（菜单接口失败时本地过滤），两端映射必须保持一致。
   */
  private static final List<String> TENANT_BIZ =
      List.of("approval:use", "crm:use", "finance:use", "inventory:use", "audit:read");

  /** 从 X-User-Roles header 推导权限码集合（对齐前端 derivePermissions 角色分支） */
  public List<String> derivePermissions(String rolesHeader) {
    List<String> roles = parseRoles(rolesHeader);
    boolean platformAdmin = roles.contains("PLATFORM_ADMIN");
    boolean tenantAdmin = platformAdmin || roles.contains("TENANT_ADMIN");
    boolean dutyOfficer = roles.contains("DUTY_OFFICER");

    List<String> codes = new ArrayList<>();
    if (!dutyOfficer) {
      codes.addAll(TENANT_BIZ);
    }
    if (tenantAdmin) {
      codes.add("user:manage");
      codes.add("user:list");
    }
    if (platformAdmin) {
      codes.add("tenant:manage");
    }
    codes.add("iot:monitor");
    if (!dutyOfficer) {
      codes.add("iot:config");
    }
    codes.add("legal:use");
    return codes;
  }

  /** 默认清单 ⊕ 权限过滤 → 当前用户可见菜单树（排序保留 catalog 声明序） */
  public List<MenuNode> menusFor(String rolesHeader) {
    List<String> permissions = derivePermissions(rolesHeader);
    return MenuCatalog.DEFAULT_MENUS.stream()
        .map(n -> filter(n, permissions))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  /** 单节点递归过滤：accessKey 未命中 → 隐藏；子节点全空 → 分组隐藏 */
  private MenuNode filter(MenuNode node, List<String> permissions) {
    if (node.accessKey() != null && !permissions.contains(node.accessKey())) {
      return null;
    }
    List<MenuNode> children =
        node.children() == null
            ? List.of()
            : node.children().stream().map(c -> filter(c, permissions)).filter(java.util.Objects::nonNull).toList();
    if (node.children() != null && !node.children().isEmpty() && children.isEmpty()) {
      return null;
    }
    return MenuNode.group(node.key(), node.path(), node.name(), node.icon(), node.accessKey(), node.sort(), children);
  }

  /** 解析逗号分隔角色 header（空/非法 → 空集合） */
  private static List<String> parseRoles(String rolesHeader) {
    if (rolesHeader == null || rolesHeader.isBlank()) {
      return List.of();
    }
    return List.of(rolesHeader.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
  }
}
