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

package com.alibaba.nacos.core.plugin.sync;

import com.alibaba.nacos.api.exception.api.NacosApiException;

import java.util.Map;

/**
 * 插件状态同步器接口，支持 Raft 集群同步与单机本地持久化等策略。
 * Plugin state synchronizer interface.
 * Supports different synchronization strategies (Raft, standalone, etc.)
 *
 * @author WangzJi
 * @since 3.2.0
 */
public interface PluginStateSynchronizer {
    
    /**
     * 将插件启用/禁用变更同步到集群或本地存储。
     * Synchronize plugin state change to cluster.
     *
     * @param pluginId plugin ID
     * @param enabled whether enabled
     * @throws NacosApiException if sync fails
     */
    void syncStateChange(String pluginId, boolean enabled) throws NacosApiException;
    
    /**
     * 将插件配置变更同步到集群或本地存储。
     * Synchronize plugin config change to cluster.
     *
     * @param pluginId plugin ID
     * @param config configuration map
     * @throws NacosApiException if sync fails
     */
    void syncConfigChange(String pluginId, Map<String, String> config) throws NacosApiException;
}
