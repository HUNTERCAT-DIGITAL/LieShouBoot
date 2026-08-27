package cn.huntercat.lieshouboot.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * 菜单权限推导单测 —— 锁定角色 → 权限码映射表.
 *
 * <p>⚠️ 与前端 {@code access.ts derivePermissions} 的一致性契约（单一事实源 = 后端，
 * 前端降级副本必须保持相同映射；改这里必须同步改前端并更新两端测试）。
 */
class MenuServiceTest {

  private final MenuService service = new MenuService();

  /** 普通业务用户 / 空角色（缺省宽松：非值班员默认业务全开） */
  private static final List<String> USER_CODES =
      List.of(
          "approval:use", "crm:use", "finance:use", "inventory:use", "audit:read",
          "iot:monitor", "iot:config", "legal:use");

  @Test
  void 缺省或普通用户_业务全开加iot与legal() {
    assertThat(service.derivePermissions(null)).containsExactlyElementsOf(USER_CODES);
    assertThat(service.derivePermissions("USER")).containsExactlyElementsOf(USER_CODES);
    assertThat(service.derivePermissions("")).containsExactlyElementsOf(USER_CODES);
  }

  @Test
  void 租户管理员_增加用户管理() {
    assertThat(service.derivePermissions("TENANT_ADMIN"))
        .containsExactlyInAnyOrderElementsOf(
            concat(USER_CODES, List.of("user:manage", "user:list")));
  }

  @Test
  void 平台管理员_增加租户管理() {
    assertThat(service.derivePermissions("PLATFORM_ADMIN"))
        .containsExactlyInAnyOrderElementsOf(
            concat(USER_CODES, List.of("user:manage", "user:list", "tenant:manage")));
  }

  @Test
  void 平台管理员含租户管理员_不重复() {
    List<String> codes = service.derivePermissions("PLATFORM_ADMIN,TENANT_ADMIN");
    assertThat(codes).contains("tenant:manage", "user:manage", "user:list");
    // 无重复码
    assertThat(codes).doesNotHaveDuplicates();
  }

  @Test
  void 值班员_仅iot只读监控加legal() {
    // 与前端一致：DUTY_OFFICER 仅保留 iot:monitor（无业务域/iot:config），legal:use 无条件追加
    assertThat(service.derivePermissions("DUTY_OFFICER"))
        .containsExactly("iot:monitor", "legal:use");
  }

  @Test
  void 角色header逗号分隔与空白容忍() {
    List<String> codes = service.derivePermissions(" PLATFORM_ADMIN , USER ");
    assertThat(codes).contains("tenant:manage").doesNotContain("DUTY_OFFICER");
  }

  private static List<String> concat(List<String> a, List<String> b) {
    return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
  }
}
