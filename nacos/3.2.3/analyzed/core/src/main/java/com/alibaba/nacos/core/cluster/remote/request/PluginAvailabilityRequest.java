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

package com.alibaba.nacos.core.cluster.remote.request;

/**
 * 插件可用性查询 RPC 请求：向目标节点查询指定插件或全部插件是否已加载并启用。
 * Plugin availability request for cluster RPC.
 * Used to query if a plugin is available on a specific node.
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class PluginAvailabilityRequest extends AbstractClusterRequest {
    
    /** 目标插件 ID；{@link #queryAll} 为 true 时可忽略。 */
    private String pluginId;
    
    /** 是否查询本节点全部插件的可用性映射。 */
    private boolean queryAll;
    
    /** 无参构造，供 RPC 反序列化使用。 */
    public PluginAvailabilityRequest() {
    }
    
    /** 返回待查询的插件 ID。 */
    public String getPluginId() {
        return pluginId;
    }
    
    /** 设置待查询的插件 ID。 */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    
    /** 是否批量查询全部插件。 */
    public boolean isQueryAll() {
        return queryAll;
    }
    
    /** 设置是否批量查询全部插件。 */
    public void setQueryAll(boolean queryAll) {
        this.queryAll = queryAll;
    }
}
