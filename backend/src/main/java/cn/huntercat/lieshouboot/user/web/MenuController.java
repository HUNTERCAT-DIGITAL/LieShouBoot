package cn.huntercat.lieshouboot.user.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.huntercat.lieshouboot.user.service.MenuNode;
import cn.huntercat.lieshouboot.user.service.MenuService;

/**
 * 当前用户菜单（ADR-0024 Phase 2 阶段 4 · 后端裁决）.
 *
 * <p>GET /api/users/me/menus —— 默认清单 ⊕ 权限过滤 → 已排序菜单树。
 * 用户上下文（角色）由 JwtAuthenticationFilter 模拟 gateway 注入 X-User-Roles header。
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Menu", description = "当前用户菜单树（权限过滤）")
public class MenuController {

  private final MenuService menuService;

  public MenuController(MenuService menuService) {
    this.menuService = menuService;
  }

  @Operation(summary = "My menus", description = "当前用户菜单树（默认清单 ⊕ 权限过滤，后端裁决）")
  @GetMapping("/me/menus")
  public List<MenuNode> myMenus(
      @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
    return menuService.menusFor(rolesHeader);
  }
}
