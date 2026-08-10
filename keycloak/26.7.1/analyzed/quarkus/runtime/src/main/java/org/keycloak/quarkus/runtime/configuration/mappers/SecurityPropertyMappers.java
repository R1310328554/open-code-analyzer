package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.common.crypto.FipsMode;
import org.keycloak.config.SecurityOptions;

import io.smallrye.config.ConfigSourceInterceptorContext;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * 安全/FIPS 相关属性映射：{@link SecurityOptions#FIPS_MODE} 及
 * 特性 {@link org.keycloak.common.Profile.Feature#FIPS} 启用时的默认值解析。
 */
final class SecurityPropertyMappers implements PropertyMapperGrouping {


    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(SecurityOptions.FIPS_MODE).transformer(SecurityPropertyMappers::resolveFipsMode)
                        .paramLabel("mode")
                        .build()
        );
    }

    private static String resolveFipsMode(String value, ConfigSourceInterceptorContext context) {
        if (value == null) {
            if (Profile.isFeatureEnabled(Feature.FIPS)) {
                return FipsMode.NON_STRICT.toString();
            }

            return FipsMode.DISABLED.toString();
        }

        return value;
    }
}
