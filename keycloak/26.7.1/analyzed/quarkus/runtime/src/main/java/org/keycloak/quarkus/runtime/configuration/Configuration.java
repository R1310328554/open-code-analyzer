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

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.keycloak.config.Option;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper;
import org.keycloak.utils.StringUtil;

import io.quarkus.runtime.configuration.ConfigUtils;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.SmallRyeConfig;

import static org.keycloak.quarkus.runtime.cli.Picocli.ARG_PREFIX;
import static org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX;

/**
 * 服务器配置访问入口：封装 SmallRye/MicroProfile 配置的读取、转换与持久化状态查询。
 */
public final class Configuration {

    /** 选项名分段分隔符字符（用于 kebab-case 转换）。 */
    public static final char OPTION_PART_SEPARATOR_CHAR = '-';
    /** 选项名分段分隔符字符串。 */
    public static final String OPTION_PART_SEPARATOR = String.valueOf(OPTION_PART_SEPARATOR_CHAR);
    /** 标记服务器已优化构建的持久化属性键。 */
    public static final String KC_OPTIMIZED = NS_KEYCLOAK_PREFIX + "optimized";

    private static SmallRyeConfig config;

    private Configuration() {

    }

    /** 判断指定布尔选项是否为 true（默认 false）。 */
    public static boolean isTrue(Option<Boolean> option) {
        return getOptionalBooleanValue(NS_KEYCLOAK_PREFIX + option.getKey()).orElse(false);
    }

    /**
     * 判断配置值是否由用户可修改的来源提供（非派生且非纯默认值）。
     *
     * @param configValue 待检查的配置值
     * @return 若配置源存在且非默认 ordinal 则为 true
     */
    public static boolean isUserModifiable(ConfigValue configValue) {
        return configValue.getConfigSourceName() != null && !isDefault(configValue);
    }

    /** 判断配置值是否来自默认 ordinal 及以下的配置源。 */
    public static boolean isDefault(ConfigValue configValue) {
        return configValue.getConfigSourceOrdinal() <= PropertyMapper.DEFAULT_VALUE_ORDINAL;
    }

    /** 判断用户是否显式设置了该选项（非默认值）。 */
    public static boolean isSet(Option<?> option) {
        return Optional.ofNullable(getKcConfigValue(option.getKey()))
                .filter(Configuration::isUserModifiable)
                .isPresent();
    }

    public static boolean isTrue(String propertyName) {
        return getOptionalBooleanValue(propertyName).orElse(false);
    }

    public static boolean isKcPropertyTrue(String propertyName) {
        return getOptionalBooleanKcValue(propertyName).orElse(false);
    }

    /** 判断 Keycloak 选项值是否为空或空白。 */
    public static boolean isBlank(Option<?> option) {
        return getOptionalKcValue(option.getKey())
                .map(StringUtil::isBlank)
                .orElse(true);
    }

    public static boolean contains(Option<?> option, String value) {
        return getOptionalValue(NS_KEYCLOAK_PREFIX + option.getKey())
                .filter(f -> f.contains(value))
                .isPresent();
    }

    public static boolean equals(Option<?> option, String value) {
        return getOptionalValue(NS_KEYCLOAK_PREFIX + option.getKey())
                .filter(f -> f.equals(value))
                .isPresent();
    }

    /** 全局配置实例是否已初始化。 */
    public static boolean isInitialized() {
        return config != null;
    }

    /** 懒加载并返回 SmallRye 配置实例（含 Keycloak 定制拦截器）。 */
    public static synchronized SmallRyeConfig getConfig() {
        if (config == null) {
            config = ConfigUtils.emptyConfigBuilder().addDiscoveredSources().withCustomizers(new ConfigBuilderCustomizer()).build();
        }
        return config;
    }

    /** 重置配置缓存并重新加载配置源（Profile 切换等场景）。 */
    public static void resetConfig() {
        config = null;
        KeycloakConfigSourceProvider.reload();
    }

    /**
     * 获取持久化属性中与用户原始输入一致的原始值。
     *
     * @param name 持久化属性名
     * @return 原始持久化值（若存在）
     */
    public static Optional<String> getRawPersistedProperty(String name) {
        return Optional.ofNullable(PersistedConfigSource.getInstance().getValue(name));
    }

    /** 返回全部原始持久化属性映射。 */
    public static Map<String, String> getRawPersistedProperties() {
        return PersistedConfigSource.getInstance().getProperties();
    }

    public static Iterable<String> getPropertyNames() {
        return getPropertyNames(false);
    }

    /**
     * 枚举配置属性名。
     *
     * @param onlyPersisted 为 true 时仅返回持久化配置源中的属性
     */
    public static Iterable<String> getPropertyNames(boolean onlyPersisted) {
        if (onlyPersisted) {
            return PersistedConfigSource.getInstance().getPropertyNames();
        }

        return getConfig().getPropertyNames();
    }

    public static ConfigValue getConfigValue(Option<?> option) {
        return getKcConfigValue(option.getKey());
    }

    public static ConfigValue getConfigValue(String propertyName) {
        return getConfig().getConfigValue(propertyName);
    }

    /** 按 Keycloak 选项键（不含 {@code kc.} 前缀）获取配置值。 */
    public static ConfigValue getKcConfigValue(String propertyName) {
        return getConfigValue(NS_KEYCLOAK_PREFIX.concat(propertyName));
    }

    public static Optional<String> getOptionalValue(String name) {
        return getConfig().getOptionalValue(name, String.class);
    }

    public static Optional<String> getOptionalKcValue(String propertyName) {
        return getOptionalValue(NS_KEYCLOAK_PREFIX.concat(propertyName));
    }

    public static Optional<String> getOptionalKcValue(Option<?> option) {
        return getOptionalKcValue(option.getKey());
    }

    public static Optional<Boolean> getOptionalBooleanKcValue(String propertyName) {
        return getOptionalValue(NS_KEYCLOAK_PREFIX.concat(propertyName)).map(Boolean::parseBoolean);
    }

    public static Optional<Boolean> getOptionalBooleanValue(String name) {
        return getOptionalValue(name).map(Boolean::parseBoolean);
    }

    public static Optional<Integer> getOptionalIntegerValue(Option<Integer> option) {
        return getOptionalIntegerValue(option.getKey());
    }

    public static Optional<Integer> getOptionalIntegerValue(String propertyName) {
        return getConfig().getOptionalValue(NS_KEYCLOAK_PREFIX.concat(propertyName), Integer.class);
    }

    /** 将配置键转换为环境变量格式（大写、非字母数字替换为下划线）。 */
    public static String toEnvVarFormat(String key) {
        return replaceNonAlphanumericByUnderscores(key).toUpperCase();
    }

    /** 将配置键转换为 CLI 长选项格式（{@code --} 前缀）。 */
    public static String toCliFormat(String key) {
        return ARG_PREFIX + key;
    }

    /** 将点分或 camelCase 键转换为 kebab-case CLI 键名。 */
    public static String toDashCase(String key) {
        if (key == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(key.length());
        boolean l = false;

        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '.') {
                c = '-'; // this is not documented, but was in the previous logic
                l = false;
            } else if (l && Character.isUpperCase(c)) {
                sb.append('-');
                c = Character.toLowerCase(c);
                l = false;
            } else {
                l = Character.isLowerCase(c);
            }
            sb.append(c);
        }

        return sb.toString();
    }

    /** 将名称中的非字母数字字符替换为下划线。 */
    public static String replaceNonAlphanumericByUnderscores(String name) {
        int length = name.length();
        StringBuilder sb = new StringBuilder(length);

        for(int i = 0; i < length; ++i) {
            char c = name.charAt(i);
            if (('a' > c || c > 'z') && ('A' > c || c > 'Z') && ('0' > c || c > '9')) {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /** 当前服务器是否已通过 build 标记为优化镜像。 */
    public static boolean isOptimized() {
        return Configuration.getRawPersistedProperty(KC_OPTIMIZED).isPresent();
    }

    /** 在持久化属性中写入优化构建标记。 */
    public static void markAsOptimized(Properties properties) {
        properties.put(Configuration.KC_OPTIMIZED, Boolean.TRUE.toString());
    }

    /** 在禁用持久化配置源的情况下解析配置值（用于构建时校验）。 */
    public static ConfigValue getNonPersistedConfigValue(String name) {
        return PersistedConfigSource.getInstance().runWithDisabled(() -> getConfigValue(name));
    }
}
