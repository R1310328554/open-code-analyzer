package org.keycloak.quarkus.runtime;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.common.profile.SingleProfileConfigResolver;
import org.keycloak.config.FeatureOptions;
import org.keycloak.quarkus.runtime.cli.PropertyException;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;

import static org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX;

/**
 * Quarkus 单 Profile 特性状态解析器：扫描 {@code kc.*} 前缀下的特性开关配置。
 */
public class QuarkusSingleProfileConfigResolver extends SingleProfileConfigResolver {

    /** 从 Quarkus 配置构建特性启用/版本映射并交给父类。 */
    public QuarkusSingleProfileConfigResolver() {
        super(getQuarkusFeatureState());
    }

    /**
     * 遍历所有 {@code kc.} 前缀属性，解析通配符特性键及其 enabled/disabled/版本值。
     *
     * @return 特性名到布尔启用状态或版本标记的映射
     */
    protected static Map<String, Boolean> getQuarkusFeatureState() {
        var map = new HashMap<String, Boolean>();
        var wildcard = PropertyMappers.getWildcardPropertyMapper(FeatureOptions.FEATURE).orElseThrow();

        Configuration.getPropertyNames().forEach(property -> {
            if (property.startsWith(NS_KEYCLOAK_PREFIX)) {
                wildcard.extractWildcardValue(property).ifPresent(feature -> {
                    var value = Configuration.getOptionalValue(property).orElseThrow(
                            () -> new PropertyException("Missing value for feature '%s'".formatted(feature)));

                    if (value.startsWith("v")) {
                        // 版本限定特性，如 feature:v1
                        map.put(feature + ":" + value, true);
                    } else {
                        map.put(feature, switch (value) {
                            case "enabled" -> Boolean.TRUE;
                            case "disabled" -> Boolean.FALSE;
                            default -> null;
                        });
                    }
                });
            }
        });

        return map;
    }
}
