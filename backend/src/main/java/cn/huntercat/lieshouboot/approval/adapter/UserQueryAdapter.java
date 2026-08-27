package cn.huntercat.lieshouboot.approval.adapter;

import cn.huntercat.lieshou.framework.domain.User;
import cn.huntercat.lieshou.framework.approval.port.UserView;
import cn.huntercat.lieshou.framework.domain.UserRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * approval → user 本地调用适配器（单体现状：同进程直调，实现 framework UserQueryPort）。
 */
@Component
public class UserQueryAdapter implements cn.huntercat.lieshou.framework.approval.port.UserQueryPort {

  private final UserRepository userRepo;

  public UserQueryAdapter(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  @Override
  public List<UserView> listTenantUsers(String tenantId) {
    Long tid = tenantId == null ? null : Long.parseLong(tenantId);
    if (tid == null) {
      return userRepo.findAll().stream().map(this::toView).toList();
    }
    return userRepo.findByTenantId(tid).stream().map(this::toView).toList();
  }

  @Override
  public UserView getUserById(Long id, String tenantId) {
    return userRepo.findById(id).map(this::toView).orElse(null);
  }

  private UserView toView(User u) {
    List<String> roles =
        u.getRoles() == null || u.getRoles().isEmpty()
            ? List.of("USER")
            : u.getRoles().stream().map(r -> r.getCode()).toList();
    return new UserView(
        u.getId(),
        u.getUsername(),
        u.getDisplayName(),
        u.getEmail(),
        u.getStatus() == null ? "ACTIVE" : u.getStatus().name(),
        roles);
  }
}
