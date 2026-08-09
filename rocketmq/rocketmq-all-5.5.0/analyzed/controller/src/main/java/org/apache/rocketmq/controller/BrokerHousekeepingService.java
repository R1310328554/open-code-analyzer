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
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.ChannelEventListener;

/**
 * Controller 侧 Broker 连接 housekeeping：在通道关闭、异常或空闲时
 * 通知 {@link BrokerHeartbeatManager} 清理失效 Broker 记录。
 */
public class BrokerHousekeepingService implements ChannelEventListener {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);
    private final ControllerManager controllerManager;

    /** @param controllerManager 持有心跳管理器的 Controller 管理器 */
    public BrokerHousekeepingService(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    /** 连接关闭时触发 Broker 通道清理。 */
    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        this.controllerManager.getHeartbeatManager().onBrokerChannelClose(channel);
    }

    /** 连接异常时同样清理 Broker 心跳状态。 */
    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
        this.controllerManager.getHeartbeatManager().onBrokerChannelClose(channel);
    }

    /** 空闲超时时清理对应 Broker 通道。 */
    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
        this.controllerManager.getHeartbeatManager().onBrokerChannelClose(channel);
    }

    @Override
    public void onChannelActive(String remoteAddr, Channel channel) {

    }
}
