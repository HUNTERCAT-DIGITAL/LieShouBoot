package cn.huntercat.lieshouboot.auth.adapter;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import cn.huntercat.lieshou.framework.auth.UserAuthPort;
import cn.huntercat.lieshou.framework.common.api.BaseException;
import cn.huntercat.lieshou.framework.common.api.ErrorCode;
import cn.huntercat.lieshou.framework.common.dto.UserAuthView;
import cn.huntercat.lieshou.framework.domain.Tenant;
import cn.huntercat.lieshou.framework.domain.User;
import cn.huntercat.lieshou.framework.domain.UserRepository;
import cn.huntercat.lieshou.framework.domain.VerificationCode;
import cn.huntercat.lieshou.framework.service.UserService;
import cn.huntercat.lieshou.framework.service.VerificationService;
import java.util.Map;

/**
 * 单体 UserAuthPort 适配（装配层 · ADR-0044 阶段 3 收敛）.
 *
 * <p>认证查询 / 创建用户 / lastLogin 回写统一走 framework-service {@link UserService}
 * （与 admin 建用户同源，消除端口版分叉）；验证码 / 租户选项 / 改密为轻量直连。
 * 端口语义保持：用户不存在返回 null（AuthService 据此抛 USER_NOT_FOUND），
 * 租户停用抛 TENANT_DISABLED（403，阻断登录——与 REST 认证视图一致）。
 */
@Component
public class UserAuthAdapter implements UserAuthPort {

  private final UserService userService;
  private final UserRepository userRepo;
  private final VerificationService verificationService;
  private final PasswordEncoder passwordEncoder;

  public UserAuthAdapter(
      UserService userService,
      UserRepository userRepo,
      VerificationService verificationService,
      PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.userRepo = userRepo;
    this.verificationService = verificationService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserAuthView findByTenantAndUsername(String tenantCode, String username) {
    try {
      return userService.authViewByTenantAndUsername(tenantCode, username);
    } catch (BaseException e) {
      // NOT_FOUND → null（端口语义）；TENANT_DISABLED 透传（403 阻断登录）
      if (ErrorCode.NOT_FOUND.name().equals(e.errorCode())) {
        return null;
      }
      throw e;
    }
  }

  @Override
  public java.util.List<java.util.Map<String, Object>> tenantOptions(String username) {
    return userService.tenantOptions(username);
  }

  @Override
  public void markLastLogin(Long id) {
    userService.markLastLogin(id);
  }

  @Override
  public void sendVerificationCode(Map<String, String> body) {
    verificationService.send(
        VerificationCode.Channel.valueOf(body.get("channel")),
        body.get("target"),
        VerificationCode.Purpose.valueOf(body.get("purpose")));
  }

  @Override
  public void verifyVerificationCode(Map<String, String> body) {
    verificationService.verify(
        VerificationCode.Channel.valueOf(body.get("channel")),
        body.get("target"),
        VerificationCode.Purpose.valueOf(body.get("purpose")),
        body.get("code"));
  }

  @Override
  public UserAuthView findByPhone(String phone) {
    try {
      return userService.authViewByPhone(phone);
    } catch (BaseException e) {
      if (ErrorCode.NOT_FOUND.name().equals(e.errorCode())) {
        return null;
      }
      throw e;
    }
  }

  @Override
  public UserAuthView findByEmail(String email) {
    try {
      return userService.authViewByEmail(email);
    } catch (BaseException e) {
      if (ErrorCode.NOT_FOUND.name().equals(e.errorCode())) {
        return null;
      }
      throw e;
    }
  }

  @Override
  public Map<String, Object> createUser(Map<String, String> body) {
    String username = body.get("username");
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("USERNAME_REQUIRED");
    }
    UserService.CreateResult result =
        userService.create(
            username,
            body.getOrDefault("displayName", username),
            body.get("password"),
            body.get("email"),
            body.get("phone"),
            body.get("tenantCode"),
            body.get("inviteCode"),
            null);
    User u = result.user();
    return Map.of("id", u.getId(), "tenantId", u.getTenantId() == null ? -1L : u.getTenantId());
  }

  @Override
  public void updateUserPassword(Long id, Map<String, String> body) {
    User u =
        userRepo
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
    u.setPasswordHash(passwordEncoder.encode(body.get("password")));
    userRepo.save(u);
  }
}
