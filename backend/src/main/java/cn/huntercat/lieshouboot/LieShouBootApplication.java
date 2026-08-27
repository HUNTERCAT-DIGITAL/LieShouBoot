package cn.huntercat.lieshouboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * LieShouBoot（猎手云开源单体版）启动入口.
 *
 * <p>单应用包含认证 / 用户 / 管理 / 审批四大模块（微服务版 LieShouCloud 重组为包内模块，本地直调）。
 */
@SpringBootApplication(scanBasePackages = {"cn.huntercat.lieshouboot", "cn.huntercat.lieshou.framework"})
@EntityScan(basePackages = {"cn.huntercat.lieshouboot", "cn.huntercat.lieshou.framework.domain", "cn.huntercat.lieshou.framework.approval.domain"})
@EnableJpaRepositories(basePackages = {"cn.huntercat.lieshouboot", "cn.huntercat.lieshou.framework.domain", "cn.huntercat.lieshou.framework.approval.domain"})
public class LieShouBootApplication {

  public static void main(String[] args) {
    SpringApplication.run(LieShouBootApplication.class, args);
  }
}
