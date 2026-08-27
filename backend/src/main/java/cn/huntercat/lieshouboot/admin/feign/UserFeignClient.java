package cn.huntercat.lieshouboot.admin.feign;

import cn.huntercat.lieshouboot.admin.feign.dto.UserDTO;

/**
 * admin → user 本地调用契约（单体重组：原 Feign 接口去掉 {@code @FeignClient}，
 * 由 {@link UserFeignAdapter} 直接调用 user 模块 Repository）。
 */
public interface UserFeignClient {

  Long count();

  UserDTO findById(Long id);

  UserDTO findByUsername(String username);
}
