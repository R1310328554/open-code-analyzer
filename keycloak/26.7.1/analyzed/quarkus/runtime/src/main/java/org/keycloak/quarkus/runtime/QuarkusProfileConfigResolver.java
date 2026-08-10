package org.keycloak.quarkus.runtime;

import org.keycloak.common.profile.CommaSeparatedListProfileConfigResolver;
import org.keycloak.quarkus.runtime.configuration.Configuration;

/**
 * Quarkus 环境下的 Profile 特性解析器：从持久化配置或当前配置读取启用/禁用特性列表。
 */
public class QuarkusProfileConfigResolver extends CommaSeparatedListProfileConfigResolver {

    /** 读取 {@code kc.features} 与 {@code kc.features-disabled} 并交给父类解析。 */
    public QuarkusProfileConfigResolver() {
        super(getConfig("kc.features"), getConfig("kc.features-disabled"));
    }

    /**
     * 优先读取持久化属性，否则回退到当前 MicroProfile 配置值。
     *
     * @param key 配置键名
     * @return 配置字符串值
     */
    static String getConfig(String key) {
        return Configuration.getRawPersistedProperty(key)
                .orElse(Configuration.getConfigValue(key).getValue());
    }

}
