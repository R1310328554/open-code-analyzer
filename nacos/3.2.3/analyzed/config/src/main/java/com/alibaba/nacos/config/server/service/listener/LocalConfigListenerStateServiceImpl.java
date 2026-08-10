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

package com.alibaba.nacos.config.server.service.listener;

import com.alibaba.nacos.api.config.model.ConfigListenerInfo;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.config.server.model.SampleResult;
import com.alibaba.nacos.config.server.remote.ConfigChangeListenContext;
import com.alibaba.nacos.config.server.service.LongPollingService;
import com.alibaba.nacos.config.server.utils.GroupKey2;
import com.alibaba.nacos.core.remote.Connection;
import com.alibaba.nacos.core.remote.ConnectionManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 本节点配置监听状态实现：合并 1.x 长轮询采样与 2.x+ gRPC 连接监听上下文，
 * 返回客户端 IP 与 MD5 映射。
 * Local implementation for Config listener state service.
 *
 * @author xiweng.yy
 */
@Service
public class LocalConfigListenerStateServiceImpl implements ConfigListenerStateService {
    
    private final LongPollingService longPollingService;
    
    private final ConfigChangeListenContext configChangeListenContext;
    
    private final ConnectionManager connectionManager;
    
    public LocalConfigListenerStateServiceImpl(LongPollingService longPollingService,
        ConfigChangeListenContext configChangeListenContext, ConnectionManager connectionManager) {
        this.longPollingService = longPollingService;
        this.configChangeListenContext = configChangeListenContext;
        this.connectionManager = connectionManager;
    }
    
    @Override
    public ConfigListenerInfo getListenerState(String dataId, String groupName,
        String namespaceId) {
        // 1.x 客户端走长轮询采样（3.x 移除 1.x 支持后待删除）
        SampleResult result =
            longPollingService.getCollectSubscribleInfo(dataId, groupName, namespaceId);
        // 2.x 及以上客户端通过 gRPC 连接与 ConfigChangeListenContext 维护监听
        // 组装 groupKey 以便从监听上下文查找订阅连接
        Set<String> listenersClients = configChangeListenContext.getListeners(groupKey);
        if (CollectionUtils.isEmpty(listenersClients)) {
            return buildActualResult(result, ConfigListenerInfo.QUERY_TYPE_CONFIG);
        }
        Map<String, String> listenersGroupkeyStatus = new HashMap<>(listenersClients.size(), 1);
        for (String connectionId : listenersClients) {
            Connection client = connectionManager.getConnection(connectionId);
            if (client != null) {
                String md5 = configChangeListenContext.getListenKeyMd5(connectionId, groupKey);
                if (md5 != null) {
                    listenersGroupkeyStatus.put(client.getMetaInfo().getClientIp(), md5);
                }
            }
        }
        result.getLisentersGroupkeyStatus().putAll(listenersGroupkeyStatus);
        return buildActualResult(result, ConfigListenerInfo.QUERY_TYPE_CONFIG);
    }
    
    @Override
    public ConfigListenerInfo getListenerStateByIp(String ip) {
        // long polling listeners for 1.x client TODO removed after 3.x not support 1.x client.
        SampleResult result = longPollingService.getCollectSubscribleInfoByIp(ip);
        // rpc listeners for upper 2.x client.
        List<Connection> connectionsByIp = connectionManager.getConnectionByIp(ip);
        for (Connection connectionByIp : connectionsByIp) {
            Map<String, String> listenKeys = configChangeListenContext.getListenKeys(
                connectionByIp.getMetaInfo().getConnectionId());
            if (listenKeys != null) {
                result.getLisentersGroupkeyStatus().putAll(listenKeys);
            }
        }
        return buildActualResult(result, ConfigListenerInfo.QUERY_TYPE_IP);
    }
    
    /**
     * 将内部 {@link SampleResult} 转为 API 层 {@link ConfigListenerInfo}。
     *
     * @param sampleResult 长轮询与 RPC 合并后的采样结果
     * @param type         查询类型（按配置或按 IP）
     * @return 对外暴露的监听状态对象
     */
    private ConfigListenerInfo buildActualResult(SampleResult sampleResult, String type) {
        ConfigListenerInfo result = new ConfigListenerInfo();
        result.setQueryType(type);
        result.setListenersStatus(sampleResult.getLisentersGroupkeyStatus());
        return result;
    }
}
