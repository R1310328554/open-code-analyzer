/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.keycloak.common.util.StringPropertyReplacer;
import org.keycloak.common.util.StringPropertyReplacer.PropertyResolver;
import org.keycloak.common.util.SystemEnvProperties;

/**
 * Keycloak 全局配置入口，通过 {@link ConfigProvider} 读取 SPI 提供者与分层配置项。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class Config {

    /** 当前生效的配置提供者，默认为系统属性实现。 */
    private static ConfigProvider configProvider = new SystemPropertiesConfigProvider();

    /**
     * 初始化配置提供者，并设置默认属性解析器以支持环境变量占位符。
     *
     * @param configProvider 自定义配置提供者
     */
    public static void init(ConfigProvider configProvider) {
        Config.configProvider = configProvider;
        StringPropertyReplacer.setDefaultPropertyResolver(new PropertyResolver() {
            SystemEnvProperties systemVariables = new SystemEnvProperties(Config.getAllowedSystemVariables());

            @Override
            public String resolve(String property) {
                return systemVariables.getProperty(property);
            }
        });
    }

    /** 返回管理 realm 名称，默认 {@code master}。 */
    public static String getAdminRealm() {
        return configProvider.scope("admin").get("realm", "master");
    }

    /**
     * 读取指定 SPI 的显式提供者 ID。
     *
     * @param spi SPI 名称
     * @return 提供者 ID；未配置或为空时返回 null
     */
    public static String getProvider(String spi) {
        String provider = configProvider.getProvider(spi);
        if (provider == null || provider.trim().equals("")) {
            return null;
        } else {
            return provider;
        }
    }

    /**
     * 读取指定 SPI 的默认提供者 ID。
     *
     * @param spi SPI 名称
     * @return 默认提供者 ID；未配置或为空时返回 null
     */
    public static String getDefaultProvider(String spi) {
        String provider = configProvider.getDefaultProvider(spi);
        if (provider == null || provider.trim().equals("")) {
            return null;
        } else {
            return provider;
        }
    }

    /**
     * 进入命名配置作用域。
     *
     * @param scope 作用域路径片段
     * @return 对应 {@link Scope}
     */
    public static Scope scope(String... scope) {
         return configProvider.scope(scope);
    }

    /** 从 admin 配置读取允许注入的系统变量白名单。 */
    private static Set<String> getAllowedSystemVariables() {
        Scope adminScope = configProvider.scope("admin");

        if (adminScope == null) {
            return Collections.emptySet();
        }

        String[] allowedSystemVariables = adminScope.getArray("allowed-system-variables");

        if (allowedSystemVariables == null) {
            return Collections.emptySet();
        }

        return new HashSet<>(Arrays.asList(allowedSystemVariables));
    }

    /** 配置数据源的抽象接口。 */
    public static interface ConfigProvider {

        String getProvider(String spi);

        String getDefaultProvider(String spi);

        Scope scope(String... scope);

    }

    /** 基于 {@code keycloak.*} 系统属性的 {@link ConfigProvider} 实现。 */
    public static class SystemPropertiesConfigProvider implements ConfigProvider {

        @Override
        public String getProvider(String spi) {
            return System.getProperties().getProperty("keycloak." + spi + ".provider");
        }

        @Override
        public String getDefaultProvider(String spi) {
            return System.getProperties().getProperty("keycloak." + spi + ".provider.default");
        }

        @Override
        public Scope scope(String... scope) {
            StringBuilder sb = new StringBuilder();
            sb.append("keycloak.");
            for (String s : scope) {
                sb.append(s);
                sb.append(".");
            }
            return new SystemPropertiesScope(sb.toString());
        }

    }

    /** 在固定前缀下读取系统属性的 {@link Scope} 实现。 */
    public static class SystemPropertiesScope extends AbstractScope {

        /** 属性键前缀，如 {@code keycloak.admin.}。 */
        protected String prefix;

        public SystemPropertiesScope(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String get(String key) {
            String v = System.getProperty(prefix + key, null);
            return v != null && !v.isEmpty() ? v : null;
        }

        @Override
        public Scope scope(String... scope) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix + ".");
            for (String s : scope) {
                sb.append(s);
                sb.append(".");
            }
            return new SystemPropertiesScope(sb.toString());
        }

        @Override
        public Set<String> getPropertyNames() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Scope root() {
            return new SystemPropertiesScope("keycloak.");
        }

    }

    /**
     * 分层配置作用域，支持字符串、数值、布尔及数组类型的读取。
     *
     * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
     */
    public static interface Scope {

        String get(String key);

        String get(String key, String defaultValue);

        String[] getArray(String key);

        default Integer getInt(String key) {
            return getInt(key, null);
        }

        Integer getInt(String key, Integer defaultValue);

        default Long getLong(String key) {
            return getLong(key, null);
        }

        Long getLong(String key, Long defaultValue);

        default Boolean getBoolean(String key) {
            return getBoolean(key, null);
        }

        Boolean getBoolean(String key, Boolean defaultValue);

        Scope scope(String... scope);

        /**
         * @deprecated since 26.3.0, to be removed
         *
         * <br>Was introduced for testing purposes and was not fully / correctly implements
         * across Scope implementations
         */
        @Deprecated
        Set<String> getPropertyNames();

        /**
         * 返回全局根作用域，键名格式与主配置文件一致（如 metrics-enabled、db 等）。
         *
         * @return 可访问全局配置项的 {@link Scope}
         */
        Scope root();
    }

    /** {@link Scope} 的抽象基类，提供带默认值的类型化读取。 */
    public static abstract class AbstractScope implements Scope {

        @Override
        public String get(String key, String defaultValue) {
            return getValue(key, Function.identity(), String.class, defaultValue);
        }

        @Override
        public Integer getInt(String key, Integer defaultValue) {
            return getValue(key, Integer::valueOf, Integer.class, defaultValue);
        }

        @Override
        public String[] getArray(String key) {
            return getValue(key, s -> s.split("\\s*,\\s*"), String[].class, null);
        }

        @Override
        public Long getLong(String key, Long defaultValue) {
            return getValue(key, Long::valueOf, Long.class, defaultValue);
        }

        @Override
        public Boolean getBoolean(String key, Boolean defaultValue) {
            return getValue(key, Boolean::valueOf, Boolean.class, defaultValue);
        }

        /** 读取原始字符串并转换为目标类型，缺失时使用默认值。 */
        protected <T> T getValue(String key, Function<String, T> conversion, Class<T> type, T defaultValue) {
            return Optional.ofNullable(get(key)).map(conversion).orElse(defaultValue);
        }
    }
}
