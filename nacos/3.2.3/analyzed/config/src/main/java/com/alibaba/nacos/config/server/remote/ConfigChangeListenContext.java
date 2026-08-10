/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.remote;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.config.server.model.ConfigListenState;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置变更监听上下文（v2 gRPC）：维护 groupKey↔connectionId 双向索引及客户端 MD5，
 * 供批量监听处理器注册/注销连接，并在配置变更时查找待推送的长连接。
 * config change listen context.
 *
 * @author liuzunfei
 * @version $Id: ConfigChangeListenContext.java, v 0.1 2020年07月20日 1:37 PM liuzunfei Exp $
 */
@Component
public class ConfigChangeListenContext {
    
    /**
     * groupKey → 监听该配置的 connectionId 集合。
     */
    private ConcurrentHashMap<String, HashSet<String>> groupKeyContext = new ConcurrentHashMap<>();
    
    /**
     * connectionId → 该连接下各 groupKey 的 {@link ConfigListenState}（含 MD5）。
     */
    private ConcurrentHashMap<String, HashMap<String, ConfigListenState>> connectionIdContext =
        new ConcurrentHashMap<>();
    
    /**
     * 注册一条监听：双向索引同时写入 groupKey 与 connectionId。
     *
     * @param groupKey     groupKey.
     * @param connectionId connectionId.
     */
    public synchronized void addListen(String groupKey, String md5, String connectionId,
        boolean isNamespaceTransfer) {
        // 1. 更新 groupKey → 连接集合
        groupKeyContext.computeIfAbsent(groupKey, k -> new HashSet<>()).add(connectionId);
        // 2. 更新 connectionId → 监听状态
        ConfigListenState listenState = new ConfigListenState(md5);
        listenState.setNamespaceTransfer(isNamespaceTransfer);
        connectionIdContext.computeIfAbsent(connectionId, k -> new HashMap<>(16)).put(groupKey,
            listenState);
    }
    
    /**
     * 取消指定连接对某 groupKey 的监听。
     *
     * @param groupKey     groupKey.
     * @param connectionId connection id.
     */
    public synchronized void removeListen(String groupKey, String connectionId) {
        
        // 1. 从 groupKey 索引移除连接
        Set<String> connectionIds = groupKeyContext.get(groupKey);
        if (connectionIds != null) {
            connectionIds.remove(connectionId);
            if (connectionIds.isEmpty()) {
                groupKeyContext.remove(groupKey);
            }
        }
        
        // 2. 从连接索引移除 groupKey
        HashMap<String, ConfigListenState> groupKeys = connectionIdContext.get(connectionId);
        if (groupKeys != null) {
            groupKeys.remove(groupKey);
        }
    }
    
    /**
     * 获取监听某 groupKey 的所有 connectionId（返回副本，避免并发修改）。
     *
     * @param groupKey groupKey.
     * @return the copy of listeners, may be return null.
     */
    public synchronized Set<String> getListeners(String groupKey) {
        
        HashSet<String> strings = groupKeyContext.get(groupKey);
        if (CollectionUtils.isNotEmpty(strings)) {
            Set<String> listenConnections = new HashSet<>();
            safeCopy(strings, listenConnections);
            return listenConnections;
        }
        return null;
    }
    
    /**
     * 安全拷贝集合并发迭代时的元素。
     *
     * @param src  may be modified concurrently
     * @param dest dest collection
     */
    private void safeCopy(Collection src, Collection dest) {
        Iterator iterator = src.iterator();
        while (iterator.hasNext()) {
            dest.add(iterator.next());
        }
    }
    
    /**
     * 连接断开时清理该 connectionId 相关的全部监听索引。
     *
     * @param connectionId connectionId.
     */
    public synchronized void clearContextForConnectionId(final String connectionId) {
        
        Map<String, String> listenKeys = getListenKeys(connectionId);
        
        if (listenKeys == null) {
            connectionIdContext.remove(connectionId);
            return;
        }
        for (Map.Entry<String, String> groupKey : listenKeys.entrySet()) {
            
            Set<String> connectionIds = groupKeyContext.get(groupKey.getKey());
            if (CollectionUtils.isNotEmpty(connectionIds)) {
                connectionIds.remove(connectionId);
                if (connectionIds.isEmpty()) {
                    groupKeyContext.remove(groupKey.getKey());
                }
            } else {
                groupKeyContext.remove(groupKey.getKey());
            }
            
        }
        connectionIdContext.remove(connectionId);
    }
    
    /**
     * 获取连接下所有 groupKey 及其 MD5 快照。
     *
     * @param connectionId connection id.
     * @return listen group keys of the connection id, key:group key,value:md5
     */
    public synchronized Map<String, String> getListenKeys(String connectionId) {
        HashMap<String, ConfigListenState> stringStringHashMap =
            connectionIdContext.get(connectionId);
        if (stringStringHashMap != null) {
            HashMap<String, String> md5Map = new HashMap<>(stringStringHashMap.size());
            for (Map.Entry<String, ConfigListenState> entry : stringStringHashMap.entrySet()) {
                md5Map.put(entry.getKey(), entry.getValue().getMd5());
            }
            return md5Map;
        } else {
            return null;
        }
    }
    
    /**
     * 获取指定连接对某 groupKey 记录的 MD5。
     *
     * @param connectionId connection id.
     * @return md5 of the listen group key.
     */
    public String getListenKeyMd5(String connectionId, String groupKey) {
        Map<String, ConfigListenState> groupKeyContexts = connectionIdContext.get(connectionId);
        return groupKeyContexts == null ? null : groupKeyContexts.get(groupKey).getMd5();
    }
    
    /** 返回连接的完整 {@link ConfigListenState}（含命名空间迁移标志）。 */
    public ConfigListenState getConfigListenState(String connectionId, String groupKey) {
        Map<String, ConfigListenState> groupKeyContexts = connectionIdContext.get(connectionId);
        return groupKeyContexts == null ? null : groupKeyContexts.get(groupKey);
    }
    
    /** 返回连接下全部监听状态的防御性拷贝。 */
    public synchronized HashMap<String, ConfigListenState> getConfigListenStates(
        String connectionId) {
        HashMap<String, ConfigListenState> configListenStateHashMap =
            connectionIdContext.get(connectionId);
        return configListenStateHashMap == null ? null : new HashMap<>(configListenStateHashMap);
    }
    
    /**
     * 当前活跃 gRPC 监听连接数（connectionId 条目数）。
     *
     * @return count of long connections.
     */
    public int getConnectionCount() {
        return connectionIdContext.size();
    }
    
}
