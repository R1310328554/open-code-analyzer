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
package org.apache.rocketmq.broker.client;

import io.netty.channel.Channel;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.ChannelEventListener;

/**
 * 客户端连接 housekeeping：定时扫描非活跃通道，并在 Netty 连接生命周期事件中清理生产者/消费者状态。
 */
public class ClientHousekeepingService implements ChannelEventListener {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    private final BrokerController brokerController;

    private ScheduledExecutorService scheduledExecutorService;

    /** 绑定 {@link BrokerController} 并创建定时扫描线程池。 */
    public ClientHousekeepingService(final BrokerController brokerController) {
        this.brokerController = brokerController;
        scheduledExecutorService = ThreadUtils.newScheduledThreadPool(1,
            new ThreadFactoryImpl("ClientHousekeepingScheduledThread", brokerController.getBrokerIdentity()));
    }

    /** 启动定时任务，每 10 秒扫描一次异常/非活跃客户端通道。 */
    public void start() {

        this.scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                ClientHousekeepingService.this.scanExceptionChannel();
            } catch (Throwable e) {
                log.error("Error occurred when scan not active client channels.", e);
            }
        }, 1000 * 10, 1000 * 10, TimeUnit.MILLISECONDS);
    }

    /** 委托生产者与消费者管理器扫描并清理非活跃通道。 */
    private void scanExceptionChannel() {
        this.brokerController.getProducerManager().scanNotActiveChannel();
        this.brokerController.getConsumerManager().scanNotActiveChannel();
    }

    /** 关闭定时线程池。 */
    public void shutdown() {
        this.scheduledExecutorService.shutdown();
    }

    /** 新连接建立时递增连接统计。 */
    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
        this.brokerController.getBrokerStatsManager().incChannelConnectNum();
    }

    /** 连接正常关闭时清理客户端注册并更新统计。 */
    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        this.brokerController.getProducerManager().doChannelCloseEvent(remoteAddr, channel);
        this.brokerController.getConsumerManager().doChannelCloseEvent(remoteAddr, channel);
        this.brokerController.getBrokerStatsManager().incChannelCloseNum();
    }

    /** 连接异常时按关闭流程清理客户端状态。 */
    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
        this.brokerController.getProducerManager().doChannelCloseEvent(remoteAddr, channel);
        this.brokerController.getConsumerManager().doChannelCloseEvent(remoteAddr, channel);
        this.brokerController.getBrokerStatsManager().incChannelExceptionNum();
    }

    /** 连接空闲超时时清理客户端状态。 */
    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
        this.brokerController.getProducerManager().doChannelCloseEvent(remoteAddr, channel);
        this.brokerController.getConsumerManager().doChannelCloseEvent(remoteAddr, channel);
        this.brokerController.getBrokerStatsManager().incChannelIdleNum();
    }

    /** 通道变为 active 时的占位回调（当前无额外逻辑）。 */
    @Override
    public void onChannelActive(String remoteAddr, Channel channel) {

    }
}
