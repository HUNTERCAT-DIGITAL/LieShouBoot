package cn.huntercat.lieshouboot.auth.feign;

import cn.huntercat.lieshouboot.user.domain.Tenant;
import cn.huntercat.lieshouboot.user.domain.TenantRepository;
import cn.huntercat.lieshouboot.user.domain.User;
import cn.huntercat.lieshouboot.user.domain.UserRepository;
import cn.huntercat.lieshouboot.user.domain.VerificationCode;
import cn.huntercat.lieshouboot.user.service.VerificationService;
import cn.huntercat.lieshou.framework.auth.UserAuthPort;
import cn.huntercat.lieshou.framework.auth.dto.UserAuthView;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * auth → user 本地调用适配器（单体重组：替代原 Feign Client，直接调用 user 模块）。
 *
 * <p>原微服务版由 Feign + Nacos 跨服务调用；单体版同进程直调，无网络/序列化开销。
 */
@Component
public class UserAuthAdapter implements UserAuthPort {

  private final UserRepository userRepo;
  private final TenantRepository tenantRepo;
  private final VerificationService verificationService;
  private final PasswordEncoder passwordEncoder;

  public UserAuthAdapter(
      UserRepository userRepo,
      TenantRepository tenantRepo,
      VerificationService verificationService,
      PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.tenantRepo = tenantRepo;
    this.verificationService = verificationService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserAuthView findByTenantAndUsername(String tenantCode, String username) {
    Tenant tenant = tenantRepo.findByCode(tenantCode).orElse(null);
    if (tenant == null) {
      throw new IllegalArgumentException("TENANT_NOT_FOUND");
    }
    return userRepo
        .findByTenantIdAndUsername(tenant.getId(), username)
        .map(this::toAuthView)
        .orElse(null);
  }

  @Override
  public java.util.List<java.util.Map<String, Object>> tenantOptions(String username) {
    return userRepo
        .findByUsername(username)
        .map(
            u -> {
              Tenant t = tenantRepo.findById(u.getTenantId()).orElse(null);
              if (t == null) return java.util.List.<java.util.Map<String, Object>>of();
              java.util.Map<String, Object> opt = new java.util.HashMap<>();
              opt.put("tenantId", t.getId());
              opt.put("tenantCode", t.getCode());
              opt.put("tenantName", t.getName());
              opt.put("tenantEdition", t.getEdition() == null ? "GENERIC" : t.getEdition().name());
              return java.util.List.of(opt);
            })
        .orElse(java.util.List.of());
  }

  @Override
  public void markLastLogin(Long id) {
    userRepo.findById(id).ifPresent(u -> {
      u.setLastLoginAt(Instant.now());
      userRepo.save(u);
    });
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
    return userRepo.findByPhone(phone).map(this::toAuthView).orElse(null);
  }

  @Override
  public UserAuthView findByEmail(String email) {
    return userRepo.findByEmail(email).map(this::toAuthView).orElse(null);
  }

  @Override
  public Map<String, Object> createUser(Map<String, String> body) {
    String username = body.get("username");
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("USERNAME_REQUIRED");
    }
    String password = body.get("password");
    String displayName =
        body.get("displayName") == null ? username : body.get("displayName");
    String tenantCode = body.getOrDefault("tenantCode", "default");
    Tenant tenant =
        tenantRepo
            .findByCode(tenantCode)
            .orElseThrow(() -> new IllegalArgumentException("TENANT_NOT_FOUND"));
    Long tenantId = tenant.getId();
    if (userRepo.existsByTenantIdAndUsername(tenantId, username)) {
      throw new IllegalArgumentException("USERNAME_TAKEN");
    }
    User u = new User(tenantId, username, displayName, passwordEncoder.encode(password));
    if (body.get("phone") != null) {
      u.setPhone(body.get("phone"));
    }
    if (body.get("email") != null) {
      u.setEmail(body.get("email"));
    }
    u = userRepo.save(u);
    return Map.of("id", u.getId(), "tenantId", u.getTenantId() == null ? -1L : u.getTenantId());
  }

  @Override
  public void updateUserPassword(Long id, Map<String, String> body) {
    User u = userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
    u.setPasswordHash(passwordEncoder.encode(body.get("password")));
    userRepo.save(u);
  }

  /** 组装鉴权视图（与 UserController.toAuthView 同构） */
  private UserAuthView toAuthView(User u) {
    Tenant tenant = tenantRepo.findById(u.getTenantId()).orElse(null);
    List<String> roleCodes =
        u.getRoles() == null || u.getRoles().isEmpty()
            ? List.of("USER")
            : u.getRoles().stream().map(r -> r.getCode()).toList();
    return new UserAuthView(
        u.getId(),
        u.getTenantId(),
        tenant == null ? null : tenant.getCode(),
        tenant == null ? null : tenant.getName(),
        tenant == null || tenant.getEdition() == null ? null : tenant.getEdition().name(),
        u.getUsername(),
        u.getDisplayName(),
        u.getPasswordHash(),
        roleCodes,
        u.getStatus() == null ? "ACTIVE" : u.getStatus().name());
  }
}
