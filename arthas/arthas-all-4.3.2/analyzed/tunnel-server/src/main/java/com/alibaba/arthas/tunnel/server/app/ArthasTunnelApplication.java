package com.alibaba.arthas.tunnel.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Arthas Tunnel Server Spring Boot 启动类：扫描 Web 配置与 Actuator 端点并启用缓存。
 */
@SpringBootApplication(scanBasePackages = { "com.alibaba.arthas.tunnel.server.app",
        "com.alibaba.arthas.tunnel.server.endpoint" })
@EnableCaching
public class ArthasTunnelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArthasTunnelApplication.class, args);
    }

}
