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

package org.apache.rocketmq.proxy.service.lite;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.client.impl.mqclient.MQClientAPIFactory;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.lite.LiteSubscriptionDTO;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.service.route.AddressableMessageQueue;
import org.apache.rocketmq.proxy.service.route.MessageQueueView;
import org.apache.rocketmq.proxy.service.route.TopicRouteService;

/**
 * Lite 订阅同步服务：将 LiteSubscriptionDTO 推送到 Topic 可读 Broker。
 */
public class LiteSubscriptionService {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    /** Topic 路由服务，用于解析可读队列。 */
    protected final TopicRouteService topicRouteService;
    /** MQ 客户端工厂，发起异步订阅同步 RPC。 */
    protected final MQClientAPIFactory mqClientAPIFactory;

    /** 注入路由服务与 MQ 客户端工厂。 */
    public LiteSubscriptionService(TopicRouteService topicRouteService, MQClientAPIFactory mqClientAPIFactory) {
        this.topicRouteService = topicRouteService;
        this.mqClientAPIFactory = mqClientAPIFactory;
    }

    /** 向 Topic 所有可读 Broker 并行同步 Lite 订阅信息。 */
    public CompletableFuture<Void> syncLiteSubscription(ProxyContext ctx,
        LiteSubscriptionDTO liteSubscriptionDTO, long timeoutMillis) {
        final String topic = liteSubscriptionDTO.getTopic();
        List<AddressableMessageQueue> readQueues;
        try {
            MessageQueueView messageQueueView = topicRouteService.getAllMessageQueueView(ctx, topic);
            // 获取 Topic 可读队列对应的 Broker 列表
            // 选取承担读职责的 Broker 队列
            readQueues = messageQueueView.getReadSelector().getBrokerActingQueues();
        } catch (Exception e) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }

        // 并行等待所有 Broker 订阅同步完成
        return CompletableFuture.allOf(
            readQueues
                .stream()
                .map(writeQ ->
                    mqClientAPIFactory.getClient().syncLiteSubscriptionAsync(
                        writeQ.getBrokerAddr(),
                        liteSubscriptionDTO,
                        timeoutMillis
                    ))
                .toArray(CompletableFuture[]::new)
        );
    }

}