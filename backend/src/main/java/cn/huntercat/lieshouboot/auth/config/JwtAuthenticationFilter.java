package cn.huntercat.lieshouboot.auth.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.huntercat.lieshou.framework.auth.JwtService;
import cn.huntercat.lieshou.framework.jwt.JwtSupport;

/**
 * 单体 JWT 认证过滤器（全栈单体无 gateway · 装配层）.
 *
 * <p>微服务版由 gateway 在入口校验 JWT 并向请求注入 X-User-Id / X-User-Name / X-User-Roles /
 * X-Tenant-Id / X-Tenant-Code header，下游服务直接读取（无感知鉴权）。单体后端无 gateway，
 * 本 filter 复刻同样行为：
 *
 * <ul>
 *   <li>Bearer JWT 校验通过（与 gateway 同源 framework-jwt，同一 secret）→ 建立 SecurityContext
 *       （{@code /api/**} 的 authenticated() 放行）+ 注入上述 header（Controller 现有
 *       {@code @RequestHeader} 逻辑不变）；</li>
 *   <li>无效 / 缺失 token → 不建立认证（/api/** 由 Security 拒绝，语义 401 见 SecurityConfig
 *       entry point）。</li>
 * </ul>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null
        && header.startsWith(BEARER_PREFIX)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      String token = header.substring(BEARER_PREFIX.length());
      try {
        if (jwtService.validate(token)) {
          Claims claims = jwtService.parse(token);
          String username = claims.getSubject();
          Long uid = claims.get(JwtSupport.CLAIM_UID, Long.class);
          Long tenantId = claims.get(JwtSupport.CLAIM_TID, Long.class);
          String tenantCode = claims.get(JwtSupport.CLAIM_TCODE, String.class);
          List<String> roles = claims.get(JwtSupport.CLAIM_ROLES, List.class);

          List<GrantedAuthority> authorities =
              (roles == null ? List.<String>of() : roles).stream()
                  .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                  .toList();
          UsernamePasswordAuthenticationToken authentication =
              UsernamePasswordAuthenticationToken.authenticated(username, null, authorities);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);

          // 复刻 gateway：注入下游 header（Controller 直接读 X-Tenant-Id / X-User-Id 等）
          request =
              new HeaderInjectingRequest(
                  request,
                  Map.of(
                      "X-User-Id", uid == null ? "" : String.valueOf(uid),
                      "X-User-Name", username == null ? "" : username,
                      "X-User-Roles", roles == null ? "" : String.join(",", roles),
                      "X-Tenant-Id", tenantId == null ? "" : String.valueOf(tenantId),
                      "X-Tenant-Code", tenantCode == null ? "" : tenantCode));
        }
      } catch (Exception e) {
        // 无效/过期 token：不建立认证（拒绝由 Security 处理；Controller 自身校验兜底）
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  /** 请求头注入包装（gateway mutate().header 的 servlet 等价物） */
  private static final class HeaderInjectingRequest extends HttpServletRequestWrapper {

    private final Map<String, String> extraHeaders;

    HeaderInjectingRequest(HttpServletRequest request, Map<String, String> extraHeaders) {
      super(request);
      this.extraHeaders = extraHeaders;
    }

    @Override
    public String getHeader(String name) {
      String v = extraHeaders.get(name);
      return v != null ? v : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      Set<String> names = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
      names.addAll(extraHeaders.keySet());
      return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      String v = extraHeaders.get(name);
      if (v != null) {
        return Collections.enumeration(List.of(v));
      }
      return super.getHeaders(name);
    }
  }
}
