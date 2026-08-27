package cn.huntercat.lieshouboot.web;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * 全栈单体静态资源 + SPA 历史路由回退.
 *
 * <p>admin-web 使用 BrowserRouter（history 模式）：客户端路由（/login、/welcome、
 * /customer/detail/1 等）直接访问或刷新时，服务端须回退到 index.html 交由前端路由接管。
 *
 * <p>策略（静态处理器保持首位，仅「未命中真实文件」时介入）：
 * <ul>
 *   <li>真实静态文件（/assets/*.js、/logo.png …）→ 原样返回；</li>
 *   <li>SPA 路由（无扩展名、非 API 前缀）→ 回退 index.html；</li>
 *   <li>缺失文件（含扩展名）与未知 API 子路径 → 404（由 NotFoundAdvice 统一 JSON）。</li>
 * </ul>
 *
 * <p>不用 {@code @GetMapping} 兜底：注解控制器优先级高于静态处理器，会截走 /assets 等真实文件；
 * 在 PathResourceResolver 内回退则静态文件永远先被命中。
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

  /** 不回退 HTML 的路径前缀（resourcePath 无前导 /；未知子路径应保持 404 JSON） */
  private static final String[] NO_FORWARD_PREFIXES = {
    "api/", "actuator/", "v3/api-docs", "swagger-ui/", "error", "_health"
  };

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/**")
        .addResourceLocations(
            "classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/")
        .resourceChain(true)
        .addResolver(
            new PathResourceResolver() {
              @Override
              protected Resource getResource(String resourcePath, Resource location) throws IOException {
                Resource requested = location.createRelative(resourcePath);
                if (requested.exists() && requested.isReadable()) {
                  return requested;
                }
                if (resourcePath.contains(".") || isNoForward(resourcePath)) {
                  return null;
                }
                return new ClassPathResource("/static/index.html");
              }
            });
  }

  private static boolean isNoForward(String resourcePath) {
    for (String prefix : NO_FORWARD_PREFIXES) {
      if (resourcePath.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
