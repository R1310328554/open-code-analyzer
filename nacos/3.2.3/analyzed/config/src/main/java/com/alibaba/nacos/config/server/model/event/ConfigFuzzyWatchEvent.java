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

package com.alibaba.nacos.config.server.model.event;

import com.alibaba.nacos.common.notify.Event;

import java.util.Set;

/**
 * 配置模糊监听批量事件：客户端通过 groupKey 模式批量订阅配置变更时发布，
 * 携带连接 ID、已有 groupKey 集合、匹配模式及是否处于初始化阶段。
 * This event represents a batch fuzzy listening event for configurations. It is used to notify the server about a batch
 * of fuzzy listening requests from clients. Each request contains a client ID, a set of existing group keys associated
 * with the client, a key group pattern, and a flag indicating whether the client is initializing.
 *
 * @author stone-98
 * @date 2024/3/5
 */
public class ConfigFuzzyWatchEvent extends Event {
    
    private static final long serialVersionUID = 1953965691384930209L;
    
    /** 发起模糊监听请求的客户端连接 ID */
    /**
     * ID of the client making the request.
      * <p>配置模糊监听事件；详见类级说明。</p>
     */
    private String connectionId;
    
    /** 用于匹配 groupKey 的 Ant 风格或通配模式 */
    /**
     * Pattern for matching group keys.
      * <p>配置模糊监听事件；详见类级说明。</p>
     */
    private String groupKeyPattern;
    
    /** 客户端当前已订阅的 groupKey 集合，用于增量比对 */
    /**
     * Set of existing group keys associated with the client.
      * <p>配置模糊监听事件；详见类级说明。</p>
     */
    private Set<String> clientExistingGroupKeys;
    
    /** 客户端是否处于首次初始化订阅阶段 */
    /**
     * Flag indicating whether the client is initializing.
      * <p>配置模糊监听事件；详见类级说明。</p>
     */
    private boolean isInitializing;
    
    /**
     * 构造模糊监听批量事件。
     * Constructs a new ConfigBatchFuzzyListenEvent with the specified parameters.
     *
     * @param connectionId                ID of the client making the request
     * @param clientExistingGroupKeys Set of existing group keys associated with the client
     * @param groupKeyPattern         Pattern for matching group keys
     * @param isInitializing          Flag indicating whether the client is initializing
     */
    public ConfigFuzzyWatchEvent(String connectionId, Set<String> clientExistingGroupKeys,
        String groupKeyPattern,
        boolean isInitializing) {
        this.connectionId = connectionId;
        this.clientExistingGroupKeys = clientExistingGroupKeys;
        this.groupKeyPattern = groupKeyPattern;
        this.isInitializing = isInitializing;
    }
    
    /**
     * 获取发起请求的客户端连接 ID。
     * Get the ID of the client making the request.
     *
     * @return The client ID
     */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * 设置客户端连接 ID。
     * Set the ID of the client making the request.
     *
     * @param connectionId The client ID to be set
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /**
     * 获取 groupKey 匹配模式。
     * Get the pattern for matching group keys.
     *
     * @return The key group pattern
     */
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /**
     * 设置 groupKey 匹配模式。
     * Set the pattern for matching group keys.
     *
     * @param groupKeyPattern The key group pattern to be set
     */
    public void setGroupKeyPattern(String groupKeyPattern) {
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /**
     * 获取客户端已有 groupKey 集合。
     * Get the set of existing group keys associated with the client.
     *
     * @return The set of existing group keys
     */
    public Set<String> getClientExistingGroupKeys() {
        return clientExistingGroupKeys;
    }
    
    /**
     * 设置客户端已有 groupKey 集合。
     * Set the set of existing group keys associated with the client.
     *
     * @param clientExistingGroupKeys The set of existing group keys to be set
     */
    public void setClientExistingGroupKeys(Set<String> clientExistingGroupKeys) {
        this.clientExistingGroupKeys = clientExistingGroupKeys;
    }
    
    /**
     * 判断客户端是否处于初始化阶段。
     * Check whether the client is initializing.
     *
     * @return True if the client is initializing, otherwise false
     */
    public boolean isInitializing() {
        return isInitializing;
    }
    
    /**
     * 设置客户端初始化标志。
     * Set the flag indicating whether the client is initializing.
     *
     * @param initializing True if the client is initializing, otherwise false
     */
    public void setInitializing(boolean initializing) {
        isInitializing = initializing;
    }
}
