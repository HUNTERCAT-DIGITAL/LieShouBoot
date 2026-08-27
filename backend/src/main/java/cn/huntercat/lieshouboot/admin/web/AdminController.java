package cn.huntercat.lieshouboot.admin.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.huntercat.lieshouboot.admin.adapter.UserQueryClient;
import cn.huntercat.lieshouboot.admin.adapter.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Admin 服务 REST 端点（单体现状：同进程直调 user 模块，无 Feign / 熔断）.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Cross-service admin endpoints (local in-process)")
public class AdminController {

  private final UserQueryClient userClient;

  public AdminController(UserQueryClient userClient) {
    this.userClient = userClient;
  }

  @Operation(summary = "Admin health（本地直调 user 模块统计）")
  @ApiResponse(responseCode = "200", description = "OK")
  @GetMapping("/health")
  public Map<String, Object> health() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", "UP");
    body.put("app", "lieshouboot-admin");
    body.put("timestamp", Instant.now().toString());
    body.put("userCount", userClient.count());
    return body;
  }

  @Operation(summary = "Get user by id（本地直调）")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User found or USER_NOT_FOUND error"),
  })
  @GetMapping("/users/{id}")
  public Map<String, Object> getUser(
      @Parameter(description = "User id", example = "1") @PathVariable Long id) {
    UserDTO u = userClient.findById(id);
    return u == null
        ? Map.of("error", "USER_NOT_FOUND")
        : Map.of(
            "id", u.id(),
            "username", u.username(),
            "displayName", u.displayName());
  }

  @Operation(summary = "Get user by username（本地直调）")
  @GetMapping("/users/by-username/{username}")
  public Map<String, Object> getUserByUsername(
      @Parameter(description = "Username", example = "futurewl") @PathVariable String username) {
    UserDTO u = userClient.findByUsername(username);
    return u == null
        ? Map.of("error", "USER_NOT_FOUND")
        : Map.of(
            "id", u.id(),
            "username", u.username(),
            "displayName", u.displayName());
  }
}
