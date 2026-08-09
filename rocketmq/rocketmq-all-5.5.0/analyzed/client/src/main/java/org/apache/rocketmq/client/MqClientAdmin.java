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

package org.apache.rocketmq.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.body.QueueTimeSpan;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.header.ConsumeMessageDirectlyResultRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.CreateTopicRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.DeleteSubscriptionGroupRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.DeleteTopicRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetConsumeStatsRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerConnectionListRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerRunningInfoRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetTopicStatsInfoRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.QueryConsumeTimeSpanRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.QueryMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.QuerySubscriptionByConsumerRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.QueryTopicConsumeByWhoRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.QueryTopicsByConsumerRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.ResetOffsetRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.ViewMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.namesrv.DeleteKVConfigRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.namesrv.DeleteTopicFromNamesrvRequestHeader;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * 客户端管理 RPC 门面：以 {@link CompletableFuture} 异步调用 Broker/NameServer
 * 的管理接口（Topic、订阅组、消费统计、直接消费等）。
 */
public interface MqClientAdmin {
    /** 异步按索引查询消息。 */
    CompletableFuture<List<MessageExt>> queryMessage(String address, boolean uniqueKeyFlag, boolean decompressBody,
        QueryMessageRequestHeader requestHeader, long timeoutMillis);

    /** 异步获取 Topic 收发统计。 */
    CompletableFuture<TopicStatsTable> getTopicStatsInfo(String address,
        GetTopicStatsInfoRequestHeader requestHeader, long timeoutMillis);

    /** 异步查询各队列消息时间跨度。 */
    CompletableFuture<List<QueueTimeSpan>> queryConsumeTimeSpan(String address,
        QueryConsumeTimeSpanRequestHeader requestHeader, long timeoutMillis);

    /** 异步创建或更新 Topic。 */
    CompletableFuture<Void> updateOrCreateTopic(String address, CreateTopicRequestHeader requestHeader,
        long timeoutMillis);

    /** 异步创建或更新订阅组配置。 */
    CompletableFuture<Void> updateOrCreateSubscriptionGroup(String address, SubscriptionGroupConfig config,
        long timeoutMillis);

    /** 异步在 Broker 上删除 Topic。 */
    CompletableFuture<Void> deleteTopicInBroker(String address, DeleteTopicRequestHeader requestHeader,
        long timeoutMillis);

    /** 异步在 NameServer 路由表中删除 Topic。 */
    CompletableFuture<Void> deleteTopicInNameserver(String address, DeleteTopicFromNamesrvRequestHeader requestHeader,
        long timeoutMillis);

    /** 异步删除 NameServer KV 配置。 */
    CompletableFuture<Void> deleteKvConfig(String address, DeleteKVConfigRequestHeader requestHeader,
        long timeoutMillis);

    /** 异步删除订阅组。 */
    CompletableFuture<Void> deleteSubscriptionGroup(String address, DeleteSubscriptionGroupRequestHeader requestHeader,
        long timeoutMillis);

    /** 异步请求 Broker 重置消费位点。 */
    CompletableFuture<Map<MessageQueue, Long>> invokeBrokerToResetOffset(String address,
        ResetOffsetRequestHeader requestHeader, long timeoutMillis);

    /** 异步按物理 offset 查看单条消息。 */
    CompletableFuture<MessageExt> viewMessage(String address, ViewMessageRequestHeader requestHeader,
        long timeoutMillis);

    /** 异步获取 Broker 集群信息。 */
    CompletableFuture<ClusterInfo> getBrokerClusterInfo(String address, long timeoutMillis);

    /** 异步查询消费组在线连接。 */
    CompletableFuture<ConsumerConnection> getConsumerConnectionList(String address,
        GetConsumerConnectionListRequestHeader requestHeader, long timeoutMillis);

    /** 异步查询消费组订阅的 Topic 列表。 */
    CompletableFuture<TopicList> queryTopicsByConsumer(String address,
        QueryTopicsByConsumerRequestHeader requestHeader, long timeoutMillis);

    /** 异步查询消费组对指定 Topic 的订阅详情。 */
    CompletableFuture<SubscriptionData> querySubscriptionByConsumer(String address,
        QuerySubscriptionByConsumerRequestHeader requestHeader, long timeoutMillis);

    /** 异步获取消费进度统计（TPS、堆积等）。 */
    CompletableFuture<ConsumeStats> getConsumeStats(String address, GetConsumeStatsRequestHeader requestHeader,
        long timeoutMillis);

    /** 异步查询订阅指定 Topic 的消费组列表。 */
    CompletableFuture<GroupList> queryTopicConsumeByWho(String address,
        QueryTopicConsumeByWhoRequestHeader requestHeader, long timeoutMillis);

    /** 异步获取消费端运行时信息（订阅、位点、JStack 等）。 */
    CompletableFuture<ConsumerRunningInfo> getConsumerRunningInfo(String address,
        GetConsumerRunningInfoRequestHeader requestHeader, long timeoutMillis);

    /** 异步在 Broker 侧直接触发一次消费（运维调试）。 */
    CompletableFuture<ConsumeMessageDirectlyResult> consumeMessageDirectly(String address,
        ConsumeMessageDirectlyResultRequestHeader requestHeader, long timeoutMillis);
}
