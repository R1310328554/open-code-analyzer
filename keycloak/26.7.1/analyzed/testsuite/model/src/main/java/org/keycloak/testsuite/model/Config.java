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
package org.keycloak.testsuite.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.keycloak.Config.ConfigProvider;
import org.keycloak.Config.Scope;
import org.keycloak.Config.SystemPropertiesScope;
import org.keycloak.common.util.StringPropertyReplacer;
import org.keycloak.common.util.SystemEnvProperties;

/**
 * 测试套件用的 {@link ConfigProvider} 实现，支持线程局部与全局两种配置存储模式。
 *
 * @author hmlnarik
 */
public class Config implements ConfigProvider {

    /** 全局模式下的默认属性映射。 */
    private final Map<String, String> defaultProperties = new ConcurrentHashMap<>();
    /** 线程局部模式下的属性映射。 */
    private final ThreadLocal<Map<String, String>> properties = new ThreadLocal<Map<String, String>>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };
    /** 判断是否使用全局配置的函数。 */
    private final BooleanSupplier useGlobalConfigurationFunc;

    /** 创建配置实例。 */
    public Config(BooleanSupplier useGlobalConfigurationFunc) {
        this.useGlobalConfigurationFunc = useGlobalConfigurationFunc;
    }

    /** 重置当前作用域内的配置项。 */
    void reset() {
        if (useGlobalConfigurationFunc.getAsBoolean()) {
            defaultProperties.clear();
        } else {
            properties.remove();
        }
    }

    /** SPI 级别配置构建器。 */
    public class SpiConfig {

        /** 配置键前缀。 */
        private final String prefix;

        /** 指定 SPI 前缀。 */
        public SpiConfig(String prefix) {
            this.prefix = prefix;
        }

        public ProviderConfig provider(String provider) {
            return new ProviderConfig(this, prefix + provider + ".");
        }

        /** 设置 SPI 的默认 provider ID。 */
        public SpiConfig defaultProvider(String defaultProviderId) {
            return config("provider", defaultProviderId);
        }

        /** 设置 SPI 级配置项；{@code value} 为 {@code null} 时移除该键。 */
        public SpiConfig config(String key, String value) {
            if (value == null) {
                getConfig().remove(prefix + key);
            } else {
                getConfig().put(prefix + key, value);
            }
            return this;
        }

        /** 切换到另一个 SPI 的配置作用域。 */
        /** 进入指定 SPI 的配置构建器。 */
    public SpiConfig spi(String spiName) {
            return new SpiConfig(spiName + ".");
        }
    }

    /** Provider 级别配置构建器。 */
    public class ProviderConfig {

        /** 所属 SPI 配置。 */
        private final SpiConfig spiConfig;
        /** Provider 配置键前缀。 */
        private final String prefix;

        /** 创建 Provider 配置作用域。 */
        public ProviderConfig(SpiConfig spiConfig, String prefix) {
            this.spiConfig = spiConfig;
            this.prefix = prefix;
        }

        /** 设置 Provider 级配置项。 */
        public ProviderConfig config(String key, String value) {
            if (value == null) {
                getConfig().remove(prefix + key);
            } else {
                getConfig().put(prefix + key, value);
            }
            return this;
        }

        public ProviderConfig provider(String provider) {
            return spiConfig.provider(provider);
        }

        public SpiConfig spi(String spiName) {
            return new SpiConfig(spiName + ".");
        }

    }

    /** 基于内存 Map 的 {@link Scope} 实现。 */
    /** 基于内存 Map 的 {@link Scope} 实现。 */
    private class MapConfigScope extends SystemPropertiesScope {

        /** 创建带前缀的配置作用域。 */
        public MapConfigScope(String prefix) {
            super(prefix);
        }

        /** 读取配置值，支持属性占位符替换与系统属性回退。 */
        @Override
        public String get(String key) {
            String v = replaceProperties(getConfig().get(prefix + key));
            if (v == null || v.isEmpty()) {
                v = System.getProperty("keycloak." + prefix + key);
            }
            return v != null && ! v.isEmpty() ? v : null;
        }

        /** 创建嵌套子作用域。 */
        @Override
        public Scope scope(String... scope) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            for (String s : scope) {
                sb.append(s);
                sb.append(".");
            }
            return new MapConfigScope(sb.toString());
        }
    }

    /** 获取 SPI 当前选中的 provider ID。 */
    @Override
    public String getProvider(String spiName) {
        return getConfig().get(spiName + ".provider");
    }

    /** 获取 SPI 的默认 provider ID。 */
    public String getDefaultProvider(String spiName) {
        return getConfig().get(spiName + ".provider.default");
    }

    /** 返回当前生效的配置 Map（全局或线程局部）。 */
    public Map<String, String> getConfig() {
        return useGlobalConfigurationFunc.getAsBoolean() ? defaultProperties : properties.get();
    }

    /** 对配置值执行环境变量/属性占位符替换。 */
    private String replaceProperties(String value) {
        return StringPropertyReplacer.replaceProperties(value, SystemEnvProperties.UNFILTERED::getProperty);
    }

    /** 创建根级或嵌套配置作用域。 */
    @Override
    public Scope scope(String... scope) {
        StringBuilder sb = new StringBuilder();
        for (String s : scope) {
            sb.append(s);
            sb.append(".");
        }
        return new MapConfigScope(sb.toString());
    }

    public SpiConfig spi(String spiName) {
        return new SpiConfig(spiName + ".");
    }

    /** 以排序后的键值对形式输出当前配置。 */
    @Override
    public String toString() {
        return getConfig().entrySet().stream()
          .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
          .map(e -> e.getKey() + " = " + e.getValue())
          .collect(Collectors.joining("\n    "));
    }
}
