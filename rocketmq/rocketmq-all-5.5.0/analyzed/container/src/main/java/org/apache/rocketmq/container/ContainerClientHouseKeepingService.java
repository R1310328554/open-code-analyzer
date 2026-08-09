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

import io.netty.channel.Channel;
import java.util.Collection;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.remoting.ChannelEventListener;

/**
 * 容器级客户端连接 housekeeping：将 Netty 通道事件
 * 广播给容器内所有主从 Broker，维护生产者/消费者连接统计。
 */
public class ContainerClientHouseKeepingService implements ChannelEventListener {
    private final IBrokerContainer brokerContainer;

    /** @param brokerContainer 托管多个 Broker 的容器实例 */
    public ContainerClientHouseKeepingService(final IBrokerContainer brokerContainer) {
        this.brokerContainer = brokerContainer;
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
        onChannelOperation(CallbackCode.CONNECT, remoteAddr, channel);
    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        onChannelOperation(CallbackCode.CLOSE, remoteAddr, channel);
    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
        onChannelOperation(CallbackCode.EXCEPTION, remoteAddr, channel);
    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
        onChannelOperation(CallbackCode.IDLE, remoteAddr, channel);
    }

    @Override
    public void onChannelActive(String remoteAddr, Channel channel) {
        onChannelOperation(CallbackCode.ACTIVE, remoteAddr, channel);
    }

    /** 遍历主从 Broker 并统一分发通道事件。 */
    private void onChannelOperation(CallbackCode callbackCode, String remoteAddr, Channel channel) {
        Collection<InnerBrokerController> masterBrokers = this.brokerContainer.getMasterBrokers();
        Collection<InnerSalveBrokerController> slaveBrokers = this.brokerContainer.getSlaveBrokers();

        for (BrokerController masterBroker : masterBrokers) {
            brokerOperation(masterBroker, callbackCode, remoteAddr, channel);
        }

        for (InnerSalveBrokerController slaveBroker : slaveBrokers) {
            brokerOperation(slaveBroker, callbackCode, remoteAddr, channel);
        }
    }

    /** 更新连接计数或清理生产者/消费者上的失效通道。 */
    private void brokerOperation(BrokerController brokerController, CallbackCode callbackCode, String remoteAddr,
        Channel channel) {
        if (callbackCode == CallbackCode.CONNECT) {
            brokerController.getBrokerStatsManager().incChannelConnectNum();
            return;
        }
        boolean removed = brokerController.getProducerManager().doChannelCloseEvent(remoteAddr, channel);
        removed &= brokerController.getConsumerManager().doChannelCloseEvent(remoteAddr, channel);
        if (removed) {
            switch (callbackCode) {
                case CLOSE:
                    brokerController.getBrokerStatsManager().incChannelCloseNum();
                    break;
                case EXCEPTION:
                    brokerController.getBrokerStatsManager().incChannelExceptionNum();
                    break;
                case IDLE:
                    brokerController.getBrokerStatsManager().incChannelIdleNum();
                    break;
                default:
                    break;
            }
        }
    }

    /** 通道事件类型枚举。 */
    public enum CallbackCode {
        /** 新连接建立。 */
        CONNECT,
        /** 连接正常关闭。 */
        CLOSE,
        /** 连接发生异常。 */
        EXCEPTION,
        /** 连接空闲超时。 */
        IDLE,
        /** 通道变为活跃状态。 */
        ACTIVE
    }
}
