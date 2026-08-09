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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.client.ConsumerManager;
import org.apache.rocketmq.broker.client.ProducerManager;
import org.apache.rocketmq.client.common.NameserverAccessConfig;
import org.apache.rocketmq.client.impl.mqclient.DoNothingClientRemotingProcessor;
import org.apache.rocketmq.client.impl.mqclient.MQClientAPIFactory;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.utils.AbstractStartAndShutdown;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.config.ProxyConfig;
import org.apache.rocketmq.proxy.service.admin.AdminService;
import org.apache.rocketmq.proxy.service.admin.DefaultAdminService;
import org.apache.rocketmq.proxy.service.channel.ChannelManager;
import org.apache.rocketmq.proxy.service.lite.LiteSubscriptionService;
import org.apache.rocketmq.proxy.service.message.LocalMessageService;
import org.apache.rocketmq.proxy.service.message.MessageService;
import org.apache.rocketmq.proxy.service.metadata.LocalMetadataService;
import org.apache.rocketmq.proxy.service.metadata.MetadataService;
import org.apache.rocketmq.proxy.service.relay.LocalProxyRelayService;
import org.apache.rocketmq.proxy.service.relay.ProxyRelayService;
import org.apache.rocketmq.proxy.service.route.LocalTopicRouteService;
import org.apache.rocketmq.proxy.service.route.TopicRouteService;
import org.apache.rocketmq.proxy.service.transaction.LocalTransactionService;
import org.apache.rocketmq.proxy.service.transaction.TransactionService;
import org.apache.rocketmq.remoting.RPCHook;

/**
 * 本地模式 {@link ServiceManager}：与内嵌 {@link BrokerController} 同进程协作。
 */
public class LocalServiceManager extends AbstractStartAndShutdown implements ServiceManager {

    /** 内嵌 Broker 控制器，提供本地生产/消费管理。 */
    private final BrokerController brokerController;
    private final TopicRouteService topicRouteService;
    private final MessageService messageService;
    private final TransactionService transactionService;
    private final ProxyRelayService proxyRelayService;
    private final MetadataService metadataService;
    private final AdminService adminService;

    private final MQClientAPIFactory mqClientAPIFactory;
    /** 本地虚拟通道管理器。 */
    private final ChannelManager channelManager;

    private final ScheduledExecutorService scheduledExecutorService = ThreadUtils.newSingleThreadScheduledExecutor(
        new ThreadFactoryImpl("LocalServiceManagerScheduledThread"));

    /** 基于 Broker 实例构建本地消息、路由、事务等子服务。 */
    public LocalServiceManager(BrokerController brokerController, RPCHook rpcHook) {
        this.brokerController = brokerController;
        this.channelManager = new ChannelManager();
        this.messageService = new LocalMessageService(brokerController, channelManager, rpcHook);
        ProxyConfig proxyConfig = ConfigurationManager.getProxyConfig();
        NameserverAccessConfig nameserverAccessConfig = new NameserverAccessConfig(proxyConfig.getNamesrvAddr(),
            proxyConfig.getNamesrvDomain(), proxyConfig.getNamesrvDomainSubgroup());
        this.mqClientAPIFactory = new MQClientAPIFactory(
            nameserverAccessConfig,
            "LocalMQClient_",
            1,
            new DoNothingClientRemotingProcessor(null),
            rpcHook,
            scheduledExecutorService
        );
        this.topicRouteService = new LocalTopicRouteService(brokerController, mqClientAPIFactory);
        this.transactionService = new LocalTransactionService(brokerController.getBrokerConfig());
        this.proxyRelayService = new LocalProxyRelayService(brokerController, this.transactionService);
        this.metadataService = new LocalMetadataService(brokerController);
        this.adminService = new DefaultAdminService(this.mqClientAPIFactory);
        this.init();
    }

    /** 注册 MQ 客户端工厂、路由服务与定时通道清理任务。 */
    protected void init() {
        this.appendStartAndShutdown(this.mqClientAPIFactory);
        this.appendStartAndShutdown(this.topicRouteService);
        this.appendStartAndShutdown(new LocalServiceManagerStartAndShutdown());
    }

    @Override
    public MessageService getMessageService() {
        return this.messageService;
    }

    @Override
    public TopicRouteService getTopicRouteService() {
        return this.topicRouteService;
    }

    @Override
    public ProducerManager getProducerManager() {
        return this.brokerController.getProducerManager();
    }

    @Override
    public ConsumerManager getConsumerManager() {
        return this.brokerController.getConsumerManager();
    }

    @Override
    public TransactionService getTransactionService() {
        return this.transactionService;
    }

    @Override
    public ProxyRelayService getProxyRelayService() {
        return this.proxyRelayService;
    }

    @Override
    public MetadataService getMetadataService() {
        return this.metadataService;
    }

    @Override
    public AdminService getAdminService() {
        return this.adminService;
    }

    @Override
    /** 本地模式不支持 Lite 订阅，返回 null。 */
    public LiteSubscriptionService getLiteSubscriptionService() {
        return null;
    }

    /** 启动定时扫描 {@link ChannelManager} 并管理调度线程池生命周期。 */
    private class LocalServiceManagerStartAndShutdown implements StartAndShutdown {
        @Override
        public void start() throws Exception {
            LocalServiceManager.this.scheduledExecutorService.scheduleWithFixedDelay(channelManager::scanAndCleanChannels, 5, 5, TimeUnit.MINUTES);
        }

        @Override
        public void shutdown() throws Exception {
            LocalServiceManager.this.scheduledExecutorService.shutdown();
        }
    }
}
