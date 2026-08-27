package cn.huntercat.lieshouboot.admin.feign;

import cn.huntercat.lieshouboot.admin.feign.dto.UserDTO;
import cn.huntercat.lieshou.framework.domain.User;
import cn.huntercat.lieshou.framework.domain.UserRepository;
import org.springframework.stereotype.Component;

/**
 * admin → user 本地调用适配器（单体重组：替代原 Feign Client + fallback）。
 *
 * <p>微服务版 Feign + Resilience4j 熔断降级；单体版同进程直调，无需 fallback。
 */
@Component
public class UserFeignAdapter implements UserFeignClient {

  private final UserRepository userRepo;

  public UserFeignAdapter(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  @Override
  public Long count() {
    return userRepo.count();
  }

  @Override
  public UserDTO findById(Long id) {
    return userRepo.findById(id).map(this::toDto).orElse(null);
  }

  @Override
  public UserDTO findByUsername(String username) {
    return userRepo.findByUsername(username).map(this::toDto).orElse(null);
  }

  private UserDTO toDto(User u) {
    return new UserDTO(u.getId(), u.getUsername(), u.getDisplayName(), u.getCreatedAt());
  }
}
