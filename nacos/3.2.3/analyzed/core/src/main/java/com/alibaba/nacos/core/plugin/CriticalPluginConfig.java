/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 关键插件配置：定义不可禁用的插件 ID 集合（如数据源方言）。
 * Critical plugin configuration.
 * Defines plugins that cannot be disabled.
 *
 * @author WangzJi
 * @since 3.2.0
 */
public final class CriticalPluginConfig {
    
    private static final Set<String> CRITICAL_PLUGINS;
    
    static {
        // 仅数据源方言为关键插件——Nacos 至少需要一种数据库后端
        // Only datasource dialects are critical - Nacos requires at least one database
        // backend.
        // 认证插件非关键——用户可禁用默认认证以使用自定义插件
        // Auth plugins are NOT critical - users can disable default auth to use custom
        // plugins.
        // TODO 应从插件定义自动加载，而非硬编码
        // TODO should be auto-loaded from Plugin defined, not bind by implementation
        Set<String> plugins = new HashSet<>();
        plugins.add("datasource-dialect:mysql");
        plugins.add("datasource-dialect:derby");
        plugins.add("datasource-dialect:postgresql");
        plugins.add("ai-storage:nacos_config");
        CRITICAL_PLUGINS = Collections.unmodifiableSet(plugins);
    }
    
    private CriticalPluginConfig() {
    }
    
    /**
     * 判断插件是否为关键插件（不可禁用）。
     *
     * @param pluginId plugin ID in format "{type}:{name}"
     * @return true if the plugin is critical
     */
    public static boolean isCritical(String pluginId) {
        return CRITICAL_PLUGINS.contains(pluginId);
    }
    
    /**
     * 获取全部关键插件 ID 不可变集合。
     *
     * @return unmodifiable set of critical plugin IDs
     */
    public static Set<String> getCriticalPlugins() {
        return CRITICAL_PLUGINS;
    }
}
