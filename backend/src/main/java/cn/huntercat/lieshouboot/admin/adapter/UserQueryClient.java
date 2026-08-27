package cn.huntercat.lieshouboot.admin.adapter;

import cn.huntercat.lieshouboot.admin.adapter.dto.UserDTO;

/**
 * admin → user 本地调用契约（单体重组：原 Feign 接口去掉 {@code @FeignClient}，
 * 由 {@link UserQueryAdapter} 直接调用 user 模块 Repository）。
 */
public interface UserQueryClient {

  Long count();

  UserDTO findById(Long id);

  UserDTO findByUsername(String username);
}
