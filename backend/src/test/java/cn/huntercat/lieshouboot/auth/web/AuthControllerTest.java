package cn.huntercat.lieshouboot.auth.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.huntercat.lieshou.framework.auth.AuthService;
import cn.huntercat.lieshou.framework.auth.JwtService;
import cn.huntercat.lieshou.framework.auth.dto.AuthDtos.TokenResponse;
import cn.huntercat.lieshou.framework.common.api.BaseException;
import cn.huntercat.lieshou.framework.common.api.ErrorCode;
import cn.huntercat.lieshou.framework.common.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * AuthController web 层测试（MockMvc standalone + GlobalExceptionHandler）。
 * 验证：登录成功返回 Token、凭证错误映射 401、发送验证码 204。
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock AuthService authService;
  @Mock JwtService jwtService;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new AuthController(authService, jwtService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void login_success_returnsTokens() throws Exception {
    TokenResponse token = mock(TokenResponse.class);
    when(token.accessToken()).thenReturn("access-token");
    when(token.tokenType()).thenReturn("Bearer");
    when(authService.login(any())).thenReturn(token);

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }

  @Test
  void login_invalidCredentials_returns401() throws Exception {
    when(authService.login(any()))
        .thenThrow(new BaseException(ErrorCode.UNAUTHORIZED, "bad credentials"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
  }

  @Test
  void sendCode_returns204() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channel\":\"EMAIL\",\"target\":\"admin@example.com\",\"purpose\":\"LOGIN\"}"))
        .andExpect(status().isNoContent());
  }
}
