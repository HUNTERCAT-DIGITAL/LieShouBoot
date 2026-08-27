package cn.huntercat.lieshouboot.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import cn.huntercat.lieshou.framework.auth.JwtService;
import cn.huntercat.lieshou.framework.jwt.JwtSupport;

/**
 * JwtAuthenticationFilter 单测（单体 JWT 认证 · 安全关键）.
 *
 * <p>覆盖：有效 token 建立认证 + 注入 gateway 同款 5 个 header；无效/缺失 token 不认证；
 * SecurityContext 无泄漏（每用例后清空）。
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;

  private JwtAuthenticationFilter newFilter() {
    return new JwtAuthenticationFilter(jwtService);
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  private static Claims mockClaims(String username, Long uid, Long tid, String tcode, java.util.List<String> roles) {
    Claims c = org.mockito.Mockito.mock(Claims.class);
    when(c.getSubject()).thenReturn(username);
    when(c.get(JwtSupport.CLAIM_UID, Long.class)).thenReturn(uid);
    when(c.get(JwtSupport.CLAIM_TID, Long.class)).thenReturn(tid);
    when(c.get(JwtSupport.CLAIM_TCODE, String.class)).thenReturn(tcode);
    when(c.get(JwtSupport.CLAIM_ROLES, java.util.List.class)).thenReturn(roles);
    return c;
  }

  @Test
  void 有效token_建立认证并注入gateway同款header() throws Exception {
    when(jwtService.validate("valid.token")).thenReturn(true);
    Claims claims =
        mockClaims("admin", 1L, 1L, "huntercat", java.util.List.of("PLATFORM_ADMIN"));
    when(jwtService.parse("valid.token")).thenReturn(claims);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid.token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    newFilter().doFilter(request, response, chain);

    // 认证建立
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getName()).isEqualTo("admin");
    assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_PLATFORM_ADMIN");
    // header 注入（gateway 语义：X-User-Id / X-User-Name / X-User-Roles / X-Tenant-Id / X-Tenant-Code）
    assertThat(chain.getRequest()).isNotNull();
    HttpServletRequest mutated = (HttpServletRequest) chain.getRequest();
    assertThat(mutated.getHeader("X-User-Id")).isEqualTo("1");
    assertThat(mutated.getHeader("X-User-Name")).isEqualTo("admin");
    assertThat(mutated.getHeader("X-User-Roles")).isEqualTo("PLATFORM_ADMIN");
    assertThat(mutated.getHeader("X-Tenant-Id")).isEqualTo("1");
    assertThat(mutated.getHeader("X-Tenant-Code")).isEqualTo("huntercat");
  }

  @Test
  void 无Authorization_不认证且放行() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    newFilter().doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(chain.getRequest()).isNotNull(); // 放行
  }

  @Test
  void 无效token_不认证() throws Exception {
    when(jwtService.validate(anyString())).thenReturn(false);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer bad.token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    newFilter().doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void parse异常_清除上下文并放行() throws Exception {
    when(jwtService.validate("evil")).thenReturn(true);
    when(jwtService.parse("evil")).thenThrow(new io.jsonwebtoken.JwtException("bad"));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer evil");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    newFilter().doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(chain.getRequest()).isNotNull();
  }
}
