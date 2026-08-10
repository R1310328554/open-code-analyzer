package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.keycloak.config.MetricsOptions;

import static org.keycloak.quarkus.runtime.configuration.Configuration.isTrue;
import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * Micrometer 指标相关 {@link PropertyMapper} 分组：
 * 将 {@code metrics-enabled} 映射到 {@code quarkus.micrometer.enabled}。
 */
final class MetricsPropertyMappers implements PropertyMapperGrouping {

    /** 指标已启用时的条件描述，供其他 mapper 的 {@code isEnabled} 使用。 */
    public static final String METRICS_ENABLED_MSG = "metrics are enabled";

    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(MetricsOptions.METRICS_ENABLED)
                        .to("quarkus.micrometer.enabled")
                        .build()
        );
    }

    /** 判断 Keycloak 指标功能是否已启用。 */
    public static boolean metricsEnabled() {
        return isTrue(MetricsOptions.METRICS_ENABLED);
    }
}
