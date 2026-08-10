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

import java.util.Map;

/**
 * 插件提供者 SPI 接口。
 *
 * <p>每种 {@link PluginType} 应有一个实现，通过 Java SPI 自动发现插件实例，
 * 无需在 UnifiedPluginManager 中手动注册各类型。</p>
 *
 * <p>示例实现：
 * <pre>{@code
 * public class AuthPluginProvider implements PluginProvider<AuthPluginService> {
 *     @Override
 *     public PluginType getPluginType() {
 *         return PluginType.AUTH;
 *     }
 *
 *     @Override
 *     public Map<String, AuthPluginService> getAllPlugins() {
 *         return AuthPluginManager.getInstance().getAllPlugins();
 *     }
 * }
 * }</pre>
 *
 * @param <T> 插件服务类型
 * @author WangzJi
 * @since 3.2.0
 */
public interface PluginProvider<T> {
    
    /**
     * 获取本提供者管理的插件类型。
     *
     * @return 插件类型
     */
    PluginType getPluginType();
    
    /**
     * 获取本提供者管理的全部插件实例。
     * 键为插件名称，值为插件实例。
     *
     * @return 插件名称到实例的映射
     */
    Map<String, T> getAllPlugins();
    
    /**
     * 获取提供者优先级，数值越小优先级越高。
     * 同类型存在多个提供者时用于排序。
     *
     * @return 排序值，默认为 0
     */
    default int getOrder() {
        return 0;
    }
}
