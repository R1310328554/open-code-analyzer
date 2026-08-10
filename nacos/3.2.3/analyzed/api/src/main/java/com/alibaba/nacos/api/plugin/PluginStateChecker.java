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

package com.alibaba.nacos.api.plugin;

/**
 * 插件状态检查器接口。
 *
 * <p>解耦各插件 Manager 与 core 模块，由 {@link PluginStateCheckerHolder} 桥接 Spring 管理的实现。</p>
 *
 * @author WangzJi
 * @since 3.0.0
 */
public interface PluginStateChecker {
    
    /**
     * 检查指定插件是否已启用。
     *
     * @param pluginType 插件类型字符串
     * @param pluginName 插件名称
     * @return 已启用返回 true，否则 false
     */
    boolean isPluginEnabled(String pluginType, String pluginName);
}
