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

import org.apache.rocketmq.broker.client.ConsumerManager;
import org.apache.rocketmq.broker.client.ProducerManager;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.proxy.service.admin.AdminService;
import org.apache.rocketmq.proxy.service.lite.LiteSubscriptionService;
import org.apache.rocketmq.proxy.service.message.MessageService;
import org.apache.rocketmq.proxy.service.metadata.MetadataService;
import org.apache.rocketmq.proxy.service.relay.ProxyRelayService;
import org.apache.rocketmq.proxy.service.route.TopicRouteService;
import org.apache.rocketmq.proxy.service.transaction.TransactionService;

/**
 * Proxy 核心服务管理接口：聚合消息、路由、事务等子服务。
 */
public interface ServiceManager extends StartAndShutdown {
    /** 返回消息收发服务。 */
    MessageService getMessageService();

    /** 返回 Topic 路由服务。 */
    TopicRouteService getTopicRouteService();

    /** 返回生产者管理器。 */
    ProducerManager getProducerManager();

    /** 返回消费者管理器。 */
    ConsumerManager getConsumerManager();

    /** 返回事务消息服务。 */
    TransactionService getTransactionService();

    /** 返回跨 Proxy 转发中继服务。 */
    ProxyRelayService getProxyRelayService();

    /** 返回元数据查询服务。 */
    MetadataService getMetadataService();

    /** 返回 Topic 管理等运维服务。 */
    AdminService getAdminService();

    /** 返回 Lite 推送订阅服务（本地模式可能为 null）。 */
    LiteSubscriptionService getLiteSubscriptionService();
}
