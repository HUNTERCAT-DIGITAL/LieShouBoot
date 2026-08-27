package cn.huntercat.lieshouboot.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 全栈单体 SPA fallback（方案 C）.
 *
 * <p>admin-web 前端 build 产物内嵌在 {@code static/}，由本单体 serve。
 * React Router 的路由（/login、/admin 等）没有对应静态文件，fallback 到 index.html。
 *
 * <p>匹配规则：非点号（静态资源）路径 → forward /index.html；
 * /api/**、/assets/**、/actuator/** 等由真实 Controller / 静态资源处理（负向前瞻排除）。
 */
@Controller
public class SpaForwardController {

  @GetMapping({"/{path:[^\\.]*}", "/{path:^(?!api$|assets$|actuator$|v3$|swagger-ui$)[^\\.]*}/**"})
  public String spa() {
    return "forward:/index.html";
  }
}
