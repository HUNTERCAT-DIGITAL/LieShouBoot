package cn.huntercat.lieshouboot.approval.feign;

import java.util.List;

/**
 * approval → user 本地调用契约（单体重组：原 Feign 接口去掉 {@code @FeignClient}，
 * 由 {@link UserQueryAdapter} 直接调用 user 模块 Repository）。
 */
public interface UserQueryClient {

  /** 租户用户列表（含 roles code 数组） */
  List<UserView> listTenantUsers(String tenantId);

  /** 单个用户（通知收件人邮箱用） */
  UserView getUserById(Long id, String tenantId);
}
