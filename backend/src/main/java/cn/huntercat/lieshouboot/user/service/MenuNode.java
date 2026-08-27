package cn.huntercat.lieshouboot.user.service;

import java.util.List;

/**
 * 菜单节点（对齐 @lieshoucloud/contract-types MenuNode · ADR-0024 Phase 2 阶段 4）.
 *
 * <p>GET /api/users/me/menus 返回的菜单树：默认清单 ⊕ 权限过滤 → 已排序树；
 * icon 为字符串 key（前端 BasicLayout ICON_MAP 映射为 ReactNode）。
 */
public record MenuNode(
    String key, String path, String name, String icon, String accessKey, int sort, List<MenuNode> children) {

  /** 构造叶子节点（无权限码 → 登录可见）。 */
  public static MenuNode leaf(String key, String path, String name, String icon, int sort) {
    return new MenuNode(key, path, name, icon, null, sort, List.of());
  }

  /** 构造叶子节点（带权限码）。 */
  public static MenuNode leaf(
      String key, String path, String name, String icon, String accessKey, int sort) {
    return new MenuNode(key, path, name, icon, accessKey, sort, List.of());
  }

  /** 构造分组节点（权限码缺省 = 登录可见）。 */
  public static MenuNode group(
      String key, String path, String name, String icon, String accessKey, int sort, List<MenuNode> children) {
    return new MenuNode(key, path, name, icon, accessKey, sort, children);
  }
}
