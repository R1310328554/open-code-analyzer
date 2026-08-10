/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.handler.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.core.plugin.model.vo.PluginDetailVO;
import com.alibaba.nacos.core.plugin.model.vo.PluginInfoVO;

import java.util.List;
import java.util.Map;

/**
 * 插件管理控制台处理器接口：列举、详情、启停状态、配置更新及集群可用性查询。
 * Interface for handling plugin-related operations.
 *
 * @author WangzJi
 */
public interface PluginHandler {
    
    /**
      * 获取插件列表。
     * Get a list of plugins.
     *
     * @param pluginType 可选的插件类型过滤
     * @return list of plugin info VOs
     * @throws NacosException if there is an issue fetching the plugins
     */
    List<PluginInfoVO> listPlugins(String pluginType) throws NacosException;
    
    /**
      * 获取插件详情。
     * Get plugin detail.
     *
     * @param pluginType 插件类型
     * @param pluginName 插件名称
     * @return plugin detail VO
     * @throws NacosException if there is an issue fetching the plugin detail
     */
    PluginDetailVO getPluginDetail(String pluginType, String pluginName) throws NacosException;
    
    /**
      * 更新插件启用/禁用状态。
     * Update plugin enabled/disabled status.
     *
     * @param pluginType 插件类型
     * @param pluginName 插件名称
     * @param enabled    是否启用
     * @param localOnly  是否仅作用于本节点
     * @throws NacosException if there is an issue updating the plugin status
     */
    void updatePluginStatus(String pluginType, String pluginName, boolean enabled,
        boolean localOnly)
        throws NacosException;
    
    /**
      * 更新插件配置项。
     * Update plugin configuration.
     *
     * @param pluginType 插件类型
     * @param pluginName 插件名称
     * @param config     配置项映射
     * @param localOnly  是否仅作用于本节点
     * @throws NacosException if there is an issue updating the plugin config
     */
    void updatePluginConfig(String pluginType, String pluginName, Map<String, String> config,
        boolean localOnly)
        throws NacosException;
    
    /**
      * 查询插件在各集群节点的可用性。
     * Get plugin availability across cluster nodes.
     *
     * @param pluginType 插件类型
     * @param pluginName 插件名称
     * @return node availability map
     * @throws NacosException if there is an issue fetching the availability
     */
    Map<String, Boolean> getPluginAvailability(String pluginType, String pluginName)
        throws NacosException;
}
