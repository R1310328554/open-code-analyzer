package org.keycloak.config;

/**
 * 指标（Metrics）暴露相关配置选项。
 */
public class MetricsOptions {

    /** 配置选项：metrics-enabled，是否暴露 Prometheus 指标端点。 */
    public static final Option<Boolean> METRICS_ENABLED = new OptionBuilder<>("metrics-enabled", Boolean.class)
            .category(OptionCategory.METRICS)
            .description("If the server should expose metrics. If enabled, metrics are available at the '/metrics' endpoint.")
            .buildTime(true)
            .defaultValue(Boolean.FALSE)
            .build();

}
