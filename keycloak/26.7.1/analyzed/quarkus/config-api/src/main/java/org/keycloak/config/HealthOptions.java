package org.keycloak.config;


/**
 * 健康检查端点相关配置选项。
 */
public class HealthOptions {

    /** 是否启用健康检查端点 */
    public static final Option<Boolean> HEALTH_ENABLED = new OptionBuilder<>("health-enabled", Boolean.class)
            .category(OptionCategory.HEALTH)
            .description("If the server should expose health check endpoints. If enabled, health checks are available at the '/health', '/health/ready' and '/health/live' endpoints.")
            .defaultValue(Boolean.FALSE)
            .buildTime(true)
            .build();

}
