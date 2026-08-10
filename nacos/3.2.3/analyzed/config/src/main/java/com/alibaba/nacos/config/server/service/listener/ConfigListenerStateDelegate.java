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
import org.springframework.stereotype.Service;

/**
 * 配置监听状态查询委托：聚合本节点 {@link LocalConfigListenerStateServiceImpl}
 * 与集群其他节点 {@link RemoteConfigListenerStateServiceImpl} 的监听快照，
 * 供 OpenAPI 与控制台展示客户端订阅分布。
 * Delegate for Config Listener State Service.
 *
 * @author xiweng.yy
 */
@Service
public class ConfigListenerStateDelegate {
    
    private final LocalConfigListenerStateServiceImpl localService;
    
    private final RemoteConfigListenerStateServiceImpl remoteService;
    
    /**
     * 注入本地与远程监听状态服务实现。
     *
     * @param localService  本节点长轮询与 gRPC 监听采样
     * @param remoteService 集群其他节点 HTTP 聚合查询
     */
    public ConfigListenerStateDelegate(LocalConfigListenerStateServiceImpl localService,
        RemoteConfigListenerStateServiceImpl remoteService) {
        this.localService = localService;
        this.remoteService = remoteService;
    }
    
    /**
     * 按 dataId/group/namespace 查询监听状态；{@code aggregation=true} 时合并远程节点结果。
     *
     * @param dataId       配置 dataId
     * @param groupName    配置 group
     * @param namespaceId  命名空间 ID
     * @param aggregation  是否聚合集群其他成员
     * @return 客户端 IP 与 MD5 映射
     */
    public ConfigListenerInfo getListenerState(String dataId, String groupName, String namespaceId,
        boolean aggregation) {
        ConfigListenerInfo result = localService.getListenerState(dataId, groupName, namespaceId);
        // aggregation 开启时合并各远程节点的 listenersStatus
            result.getListenersStatus()
                .putAll(remoteService.getListenerState(dataId, groupName, namespaceId)
                    .getListenersStatus());
        }
        return result;
    }
    
    /**
     * 按客户端 IP 反查其监听的 groupKey 与 MD5；可选聚合远程节点。
     *
     * @param ip          客户端 IP
     * @param aggregation 是否合并集群其他成员
     * @return 该 IP 的监听详情
     */
    public ConfigListenerInfo getListenerStateByIp(String ip, boolean aggregation) {
        ConfigListenerInfo result = localService.getListenerStateByIp(ip);
        if (aggregation) {
            result.getListenersStatus()
                .putAll(remoteService.getListenerStateByIp(ip).getListenersStatus());
        }
        return result;
    }
    
}
