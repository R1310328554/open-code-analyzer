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

package com.alibaba.nacos.core.cluster.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 插件可用性查询 RPC 响应：单插件模式返回 {@link #available}，全量模式返回 {@link #pluginAvailabilityMap}。
 * Plugin availability response for cluster RPC.
 * Contains availability status of a plugin on a specific node.
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class PluginAvailabilityResponse extends Response {
    
    /** 单插件查询时的目标插件 ID。 */
    private String pluginId;
    
    /** 单插件查询时该插件是否已启用可用。 */
    private boolean available;
    
    /** 全量查询时各 pluginId 到启用状态的映射。 */
    private java.util.Map<String, Boolean> pluginAvailabilityMap;
    
    /** 无参构造，供 RPC 反序列化使用。 */
    public PluginAvailabilityResponse() {
    }
    
    /** 返回单插件查询的插件 ID。 */
    public String getPluginId() {
        return pluginId;
    }
    
    /** 设置单插件查询的插件 ID。 */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    
    /** 返回单插件是否可用。 */
    public boolean isAvailable() {
        return available;
    }
    
    /** 设置单插件是否可用。 */
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    /** 返回全量插件可用性映射。 */
    public java.util.Map<String, Boolean> getPluginAvailabilityMap() {
        return pluginAvailabilityMap;
    }
    
    /** 设置全量插件可用性映射。 */
    public void setPluginAvailabilityMap(java.util.Map<String, Boolean> pluginAvailabilityMap) {
        this.pluginAvailabilityMap = pluginAvailabilityMap;
    }
}
