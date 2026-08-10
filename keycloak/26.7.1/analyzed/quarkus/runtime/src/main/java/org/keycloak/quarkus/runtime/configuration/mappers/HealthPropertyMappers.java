package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.keycloak.config.HealthOptions;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * 健康检查（SmallRye Health）相关 {@link PropertyMapper} 分组。
 * <p>
 * {@link HealthOptions#HEALTH_ENABLED} 主要用于控制是否引入健康检查扩展/构件，
 * 无需映射到 Quarkus 选项；Quarkus 侧默认启用健康端点。
 */
final class HealthPropertyMappers implements PropertyMapperGrouping {

    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(HealthOptions.HEALTH_ENABLED)
                        // 无需映射到 quarkus 选项；该选项用于控制构件/扩展是否纳入构建
                        // Quarkus 将默认启用健康检查
                        .build()
        );
    }

}
