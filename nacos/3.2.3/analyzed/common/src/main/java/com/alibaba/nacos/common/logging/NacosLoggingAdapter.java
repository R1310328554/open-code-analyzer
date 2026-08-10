/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.logging;

/**
 * Nacos client logging adapter.
 * <p>Nacos 客户端日志适配器 SPI：对接 Logback/Log4j2 等底层实现，将 Nacos 专用日志配置加载到应用日志上下文中。</p>
 *
 * @author xiweng.yy
 */
public interface NacosLoggingAdapter {
    
    /**
     * Whether current adapter is adapted for specified logger class.
     * <p>判断当前适配器是否匹配指定的 {@link org.slf4j.Logger} 实现类。</p>
     *
     * @param loggerClass {@link org.slf4j.Logger} implementation class
     * @return {@code true} if current adapter can adapt this {@link org.slf4j.Logger} implementation, otherwise {@code
     * false}
     */
    boolean isAdaptedLogger(Class<?> loggerClass);
    
    /**
     * Load Nacos logging configuration into log context.
     * <p>根据 {@link NacosLoggingProperties} 将 Nacos 日志配置注入日志框架上下文。</p>
     *
     * @param loggingProperties logging properties
     */
    void loadConfiguration(NacosLoggingProperties loggingProperties);
    
    /**
     * Whether need reload configuration into log context.
     * <p>当日志上下文尚未包含 Nacos 配置时返回 {@code true}，触发重新加载。</p>
     *
     * @return {@code true} when context don't contain nacos logging configuration. otherwise {@code false}
     */
    boolean isNeedReloadConfiguration();
    
    /**
     * Get current logging default config location.
     * <p>返回适配器内置的默认配置文件路径（如 classpath 下的 nacos-logback.xml）。</p>
     *
     * @return default config location
     */
    String getDefaultConfigLocation();
    
    /**
     * Whether current adapter enabled, design for users which want to log nacos client into app logs.
     * <p>是否启用该适配器；用户可将 Nacos 客户端日志合并到应用日志中，默认启用。</p>
     *
     * @return {@code true} when enabled, otherwise {@code false}, default {@code true}
     */
    default boolean isEnabled() {
        return true;
    }
}
