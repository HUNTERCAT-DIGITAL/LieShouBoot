package cn.huntercat.lieshouboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * LieShouBoot（猎手云开源单体版）启动入口.
 *
 * <p>单应用包含认证 / 用户 / 管理 / 审批四大模块（原微服务版 LieShouCloud 的
 * gateway+user+admin+auth+approval 重组为包内模块，Feign 调用改为本地 Service 调用）。
 */
@SpringBootApplication
@EntityScan(basePackages = {"cn.huntercat.lieshouboot", "cn.huntercat.lieshou.framework.domain"})
@EnableJpaRepositories(basePackages = {"cn.huntercat.lieshouboot", "cn.huntercat.lieshou.framework.domain"})
public class LieShouBootApplication {

  public static void main(String[] args) {
    SpringApplication.run(LieShouBootApplication.class, args);
  }
}
