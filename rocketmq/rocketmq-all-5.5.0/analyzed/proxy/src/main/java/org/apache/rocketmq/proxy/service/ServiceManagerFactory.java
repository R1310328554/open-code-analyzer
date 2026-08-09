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
package org.apache.rocketmq.proxy.service;

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ObjectCreator;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.RemotingClient;

/**
 * {@link ServiceManager} 工厂：按部署模式创建本地或集群实现。
 */
public class ServiceManagerFactory {
    /** 创建无 RPCHook 的本地模式服务管理器。 */
    public static ServiceManager createForLocalMode(BrokerController brokerController) {
        return createForLocalMode(brokerController, null);
    }

    /** 创建带鉴权钩子的本地模式 {@link LocalServiceManager}。 */
    public static ServiceManager createForLocalMode(BrokerController brokerController, RPCHook rpcHook) {
        return new LocalServiceManager(brokerController, rpcHook);
    }

    /** 创建默认配置的集群模式服务管理器。 */
    public static ServiceManager createForClusterMode() {
        return createForClusterMode(null, null);
    }

    /** 创建带 RPCHook 的集群模式服务管理器。 */
    public static ServiceManager createForClusterMode(RPCHook rpcHook) {
        return createForClusterMode(rpcHook, null);
    }

    /** 创建可定制 Remoting 客户端的 {@link ClusterServiceManager}。 */
    public static ServiceManager createForClusterMode(RPCHook rpcHook, ObjectCreator<RemotingClient> remotingClientCreator) {
        return new ClusterServiceManager(rpcHook, remotingClientCreator);
    }
}
