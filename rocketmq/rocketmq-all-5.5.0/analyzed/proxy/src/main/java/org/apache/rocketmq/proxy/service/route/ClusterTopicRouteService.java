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
package org.apache.rocketmq.proxy.service.route;

import java.util.List;
import org.apache.rocketmq.client.impl.mqclient.MQClientAPIFactory;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.proxy.common.Address;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;

/**
 * 集群模式 Topic 路由服务：通过 NameServer 查询全量路由。
 */
public class ClusterTopicRouteService extends TopicRouteService {

    /** 注入 MQ 客户端 API 工厂。 */
    public ClusterTopicRouteService(MQClientAPIFactory mqClientAPIFactory) {
        super(mqClientAPIFactory);
    }

    @Override
    /** 返回 Topic 当前全量消息队列视图。 */
    public MessageQueueView getCurrentMessageQueueView(ProxyContext ctx, String topicName) throws Exception {
        return getAllMessageQueueView(ctx, topicName);
    }

    @Override
    /** 构建面向客户端的 Proxy 路由数据。 */
    public ProxyTopicRouteData getTopicRouteForProxy(ProxyContext ctx, List<Address> requestHostAndPortList,
        String topicName) throws Exception {
        TopicRouteData topicRouteData = getAllMessageQueueView(ctx, topicName).getTopicRouteData();
        return new ProxyTopicRouteData(topicRouteData, requestHostAndPortList);
    }

    @Override
    /** 按 Broker 名称解析 Master 地址。 */
    public String getBrokerAddr(ProxyContext ctx, String brokerName) throws Exception {
        TopicRouteWrapper topicRouteWrapper = getAllMessageQueueView(ctx, brokerName).getTopicRouteWrapper();
        return topicRouteWrapper.getMasterAddr(brokerName);
    }

    @Override
    /** 为逻辑队列填充 Broker 物理地址。 */
    public AddressableMessageQueue buildAddressableMessageQueue(ProxyContext ctx, MessageQueue messageQueue) throws Exception {
        String brokerAddress = getBrokerAddr(ctx, messageQueue.getBrokerName());
        return new AddressableMessageQueue(messageQueue, brokerAddress);
    }
}
