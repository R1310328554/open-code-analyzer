/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.container;

import java.util.Collection;
import java.util.List;

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.ConfigContext;
import org.apache.rocketmq.broker.out.BrokerOuterAPI;
import org.apache.rocketmq.common.BrokerIdentity;
import org.apache.rocketmq.remoting.RemotingServer;
import org.apache.rocketmq.remoting.netty.NettyClientConfig;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;

/**
 * Broker 容器接口：在同一 JVM 进程中托管多个主从 Broker，
 * 共享 Remoting 服务器与 {@link BrokerOuterAPI} 等基础设施。
 */
public interface IBrokerContainer {

    /** 启动容器及共享网络组件。 */
    void start() throws Exception;

    /** 关闭容器内全部 Broker 并释放共享资源。 */
    void shutdown();

    /**
     * 按配置上下文向容器动态添加 Broker。
     *
     * @param configContext 包含 Broker/Store/Auth 等配置
     * @return 新建的控制器；已存在时返回 null
     * @throws Exception 初始化 Broker 失败时抛出
     */
    BrokerController addBroker(ConfigContext configContext) throws Exception;

    /**
     * 按身份标识从容器移除 Broker。
     *
     * @param brokerIdentity 集群名、Broker 名与 ID 组合
     * @return 被移除的控制器；不存在时返回 null
     */
    BrokerController removeBroker(BrokerIdentity brokerIdentity) throws Exception;

    /**
     * 按身份查找已注册的 Broker 控制器。
     *
     * @param brokerIdentity 目标 Broker 身份
     * @return 匹配的控制器或 null
     */
    BrokerController getBroker(BrokerIdentity brokerIdentity);

    /** 返回容器内全部 Master Broker。 */
    Collection<InnerBrokerController> getMasterBrokers();

    /** 返回容器内全部 Slave Broker。 */
    Collection<InnerSalveBrokerController> getSlaveBrokers();

    /** 返回容器内所有 Broker 控制器列表。 */
    List<BrokerController> getBrokerControllers();

    /** 返回容器对外暴露的监听地址。 */
    String getBrokerContainerAddr();

    /** 获取容器中第一个 Master Broker（用于默认路由）。 */
    BrokerController peekMasterBroker();

    /** 返回容器级配置对象。 */
    BrokerContainerConfig getBrokerContainerConfig();

    /** 返回共享 Netty 服务端配置。 */
    NettyServerConfig getNettyServerConfig();

    /** 返回共享 Netty 客户端配置。 */
    NettyClientConfig getNettyClientConfig();

    /** 返回容器内 Broker 共用的对外 RPC 客户端。 */
    BrokerOuterAPI getBrokerOuterAPI();

    /** 返回容器共享的 Remoting 服务端。 */
    RemotingServer getRemotingServer();
}
