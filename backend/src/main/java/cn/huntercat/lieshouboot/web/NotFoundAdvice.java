package cn.huntercat.lieshouboot.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 资源不存在 → 404 JSON（统一错误契约）.
 *
 * <p>框架层 {@code GlobalExceptionHandler} 把 {@link NoResourceFoundException} 落入 500 兜底；
 * 缺失静态资源 / 未知 API 子路径语义应为 404，此处在本仓薄壳修正（不动共享框架）。
 */
@RestControllerAdvice
public class NotFoundAdvice {

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, String>> onNoResource(NoResourceFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", "NOT_FOUND", "message", "资源不存在"));
  }
}
