package cn.huntercat.lieshouboot.admin.adapter;

import cn.huntercat.lieshouboot.admin.adapter.dto.UserDTO;
import cn.huntercat.lieshou.framework.domain.User;
import cn.huntercat.lieshou.framework.domain.UserRepository;
import org.springframework.stereotype.Component;

/**
 * admin → user 本地调用适配器（单体现状：同进程直调 user 模块 Repository）。
 */
@Component
public class UserQueryAdapter implements UserQueryClient {

  private final UserRepository userRepo;

  public UserQueryAdapter(UserRepository userRepo) {
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
