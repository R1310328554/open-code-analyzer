/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.consistency.ephemeral.distro.v2;

import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.cluster.remote.ClusterRpcClientProxy;
import com.alibaba.nacos.core.distributed.distro.DistroProtocol;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.task.DistroTaskEngineHolder;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManagerDelegate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Naming v2 客户端 Distro 组件注册器。
 *
 * <p>在启动时将 {@link DistroClientDataProcessor}、{@link DistroClientTransportAgent} 与 {@link DistroClientTaskFailedHandler} 注册到 {@link DistroComponentHolder}。</p>
 *
 * @author xiweng.yy
 */
@Component
public class DistroClientComponentRegistry {
    
    /** 集群成员管理器。 */
    private final ServerMemberManager serverMemberManager;
    
    /** Distro 协议主入口。 */
    private final DistroProtocol distroProtocol;
    
    /** Distro 组件（处理器/传输/存储）持有者。 */
    private final DistroComponentHolder componentHolder;
    
    /** Distro 延迟/重试任务引擎持有者。 */
    private final DistroTaskEngineHolder taskEngineHolder;
    
    /** v2 客户端连接管理器。 */
    private final ClientManager clientManager;
    
    /** 集群 RPC 客户端代理。 */
    private final ClusterRpcClientProxy clusterRpcClientProxy;
    
    public DistroClientComponentRegistry(ServerMemberManager serverMemberManager,
        DistroProtocol distroProtocol,
        DistroComponentHolder componentHolder, DistroTaskEngineHolder taskEngineHolder,
        ClientManagerDelegate clientManager, ClusterRpcClientProxy clusterRpcClientProxy) {
        this.serverMemberManager = serverMemberManager;
        this.distroProtocol = distroProtocol;
        this.componentHolder = componentHolder;
        this.taskEngineHolder = taskEngineHolder;
        this.clientManager = clientManager;
        this.clusterRpcClientProxy = clusterRpcClientProxy;
    }
    
    /**
     * 向 Distro 协议注册 v2 {@link com.alibaba.nacos.naming.core.v2.client.Client} 相关组件。
     */
    @PostConstruct
    public void doRegister() {
        DistroClientDataProcessor dataProcessor =
            new DistroClientDataProcessor(clientManager, distroProtocol);
        DistroTransportAgent transportAgent = new DistroClientTransportAgent(clusterRpcClientProxy,
            serverMemberManager);
        DistroClientTaskFailedHandler taskFailedHandler =
            new DistroClientTaskFailedHandler(taskEngineHolder);
        componentHolder.registerDataStorage(DistroClientDataProcessor.TYPE, dataProcessor);
        componentHolder.registerDataProcessor(dataProcessor);
        componentHolder.registerTransportAgent(DistroClientDataProcessor.TYPE, transportAgent);
        componentHolder.registerFailedTaskHandler(DistroClientDataProcessor.TYPE,
            taskFailedHandler);
    }
}
