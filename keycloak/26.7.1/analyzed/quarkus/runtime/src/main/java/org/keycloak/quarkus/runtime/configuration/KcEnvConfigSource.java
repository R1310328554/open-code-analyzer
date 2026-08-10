/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.quarkus.runtime.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;
import org.keycloak.quarkus.runtime.configuration.mappers.WildcardPropertyMapper;

import io.smallrye.config.EnvConfigSource;
import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX;

import static io.smallrye.config.common.utils.StringUtil.replaceNonAlphanumericByUnderscores;

/**
 * Keycloak 环境变量配置源：将 {@code KC_}/{@code KCRAW_} 前缀的环境变量映射为 {@code kc.*} 属性。
 * <p>
 * 不继承 {@link io.smallrye.config.EnvConfigSource}，因其自动键名转换会导致重复项
 * （如 {@code kc.db-password} 与 {@code kc.db.password}）及 getter 比较问题；此处自行完成映射。
 */
public class KcEnvConfigSource extends PropertiesConfigSource {

    /** 配置源名称标识。 */
    public static final String NAME = "KcEnvVarConfigSource";
    /** 显式指定环境变量键到 kc 属性名的映射前缀。 */
    public static final String KCKEY_PREFIX = "KCKEY_";
    /** 原始值前缀：跳过 SmallRye 变量插值与额外转换。 */
    public static final String KCRAW_PREFIX = "KCRAW_";

    /** 测试用环境变量覆盖表。 */
    static final Map<String, String> ENV_OVERRIDE = new HashMap<String, String>();

    public KcEnvConfigSource(Map<String, String> env) {
        super(buildProperties(env), NAME, 500);
    }

    private static Map<String, String> buildProperties(Map<String, String> env) {
        Map<String, String> properties = new HashMap<>();
        String kcPrefix = replaceNonAlphanumericByUnderscores(NS_KEYCLOAK_PREFIX.toUpperCase());

        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!(key.startsWith(kcPrefix) || key.startsWith(KCRAW_PREFIX))) {
                continue;
            }

            boolean isRaw = key.startsWith(KCRAW_PREFIX);
            String baseKey;

            if (isRaw) {
                baseKey = key.substring(KCRAW_PREFIX.length());

                // 同一基础键同时设置 KC_ 与 KCRAW_ 时快速失败
                if (env.containsKey(kcPrefix + baseKey)) {
                    throw new IllegalArgumentException(
                            "Both " + kcPrefix + baseKey + " and " + KCRAW_PREFIX + baseKey
                                    + " are set. Use only one.");
                }
            } else {
                baseKey = key.substring(kcPrefix.length());
            }

            // 解析转换后的 kc 属性键
            String transformedKey;
            String actualKey = env.get(KCKEY_PREFIX + baseKey);
            if (actualKey != null) {
                // 使用 KCKEY_ 显式映射
                transformedKey = NS_KEYCLOAK_PREFIX + actualKey;
            } else {
                // 按约定或通配符规则推断映射
                transformedKey = NS_KEYCLOAK_PREFIX + baseKey.toLowerCase().replace("_", "-");

                PropertyMapper<?> mapper = PropertyMappers.getMapper(transformedKey);

                if (mapper != null && mapper.hasWildcard()) {
                    // 通配符属性不遵循默认下划线转连字符规则
                    WildcardPropertyMapper<?> wildcardPropertyMapper = (WildcardPropertyMapper<?>) mapper;

                    transformedKey = wildcardPropertyMapper.getKcKeyForEnvKey(key, transformedKey)
                            .orElseThrow();
                }
            }

            // KCRAW_ 值将 $ 转义为 $$，避免 SmallRye 变量插值；
            // 再将 \$ 替换为 \\，防止 escapeDollarIfExists 吞掉反斜杠
            properties.put(transformedKey, isRaw ? value.replace("$", "$$").replace("\\$", "\\\\") : value);
        }

        return properties;
    }

    public static Collection<ConfigSource> getConfigSources() {
        Map<String, String> env = System.getenv();

        if (ENV_OVERRIDE.isEmpty()) {
            return List.of(new KcEnvConfigSource(env));
        }

        env = new HashMap<String, String>(env);
        env.putAll(ENV_OVERRIDE);

        return List.of(new KcEnvConfigSource(env), new EnvConfigSource(ENV_OVERRIDE, EnvConfigSource.ORDINAL + 1));
    }
}
