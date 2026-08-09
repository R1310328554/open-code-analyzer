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
package org.apache.rocketmq.controller;

import io.netty.channel.Channel;
import org.apache.rocketmq.common.ControllerConfig;
import org.apache.rocketmq.controller.helper.BrokerLifecycleListener;
import org.apache.rocketmq.controller.impl.heartbeat.BrokerLiveInfo;
import org.apache.rocketmq.controller.impl.heartbeat.DefaultBrokerHeartbeatManager;
import org.apache.rocketmq.controller.impl.heartbeat.RaftBrokerHeartBeatManager;

import java.util.Map;

/**
 * Broker 心跳管理器：维护 Broker 存活状态、处理心跳与通道关闭，
 * 支持 DLedger 与 JRaft 两种 Controller 实现。
 */
public interface BrokerHeartbeatManager {
    long DEFAULT_BROKER_CHANNEL_EXPIRED_TIME = 1000 * 10;

    /** 按 Controller 类型创建对应的心跳管理器实例。 */
    static BrokerHeartbeatManager newBrokerHeartbeatManager(ControllerConfig controllerConfig) {
        if (controllerConfig.getControllerType().equals(ControllerConfig.JRAFT_CONTROLLER)) {
            return new RaftBrokerHeartBeatManager(controllerConfig);
        } else {
            return new DefaultBrokerHeartbeatManager(controllerConfig);
        }
    }

    /** 初始化心跳检测所需资源。 */
    void initialize();

    /** 处理 Broker 上报的心跳并更新存活信息。 */
    void onBrokerHeartbeat(final String clusterName, final String brokerName, final String brokerAddr,
        final Long brokerId, final Long timeoutMillis, final Channel channel, final Integer epoch,
        final Long maxOffset, final Long confirmOffset, final Integer electionPriority);

    /** 启动心跳超时扫描等后台任务。 */
    void start();

    /** 关闭心跳管理器并释放资源。 */
    void shutdown();

    /** 注册 Broker 下线等生命周期监听器。 */
    void registerBrokerLifecycleListener(final BrokerLifecycleListener listener);

    /** 通道关闭时清理对应 Broker 存活记录。 */
    void onBrokerChannelClose(final Channel channel);

    /**
     * 按集群与 Broker 身份查询存活详情。
     *
     * @return 存活信息；未找到时返回 null
     */
    BrokerLiveInfo getBrokerLiveInfo(String clusterName, String brokerName, Long brokerId);

    /** 判断指定 Broker 副本当前是否存活。 */
    boolean isBrokerActive(final String clusterName, final String brokerName, final Long brokerId);

    /**
     * 统计各集群各 Broker 组内活跃副本数量。
     *
     * @return 嵌套映射：集群 → Broker 组 → 活跃数
     */
    Map<String/*cluster*/, Map<String/*broker-set*/, Integer/*active broker num*/>> getActiveBrokersNum();
}
