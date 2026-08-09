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
package org.apache.rocketmq.proxy.processor;

import io.netty.channel.Channel;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.broker.client.ClientChannelInfo;
import org.apache.rocketmq.broker.client.ConsumerGroupInfo;
import org.apache.rocketmq.broker.client.ConsumerIdsChangeListener;
import org.apache.rocketmq.broker.client.ProducerChangeListener;
import org.apache.rocketmq.client.consumer.AckResult;
import org.apache.rocketmq.client.consumer.PopResult;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.consumer.ReceiptHandle;
import org.apache.rocketmq.common.lite.LiteSubscriptionDTO;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.proxy.common.Address;
import org.apache.rocketmq.proxy.common.MessageReceiptHandle;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.service.message.ReceiptHandleMessage;
import org.apache.rocketmq.proxy.service.metadata.MetadataService;
import org.apache.rocketmq.proxy.service.relay.ProxyRelayService;
import org.apache.rocketmq.proxy.service.route.ProxyTopicRouteData;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * Proxy 消息处理门面接口：定义生产、消费、POP、事务、路由及客户端注册等异步 API。
 * gRPC/Remoting 层 Activity 均通过此接口与 Broker 或集群后端交互。
 */
public interface MessagingProcessor extends StartAndShutdown {

    /** 默认 RPC 超时（毫秒）。 */
    long DEFAULT_TIMEOUT_MILLS = Duration.ofSeconds(2).toMillis();

    /** POP 消息默认不可见时间下限（毫秒）。 */
    long INVISIBLE_TIME_MS = Duration.ofSeconds(1).toMillis();

    /** 查询消费组订阅配置。 */
    SubscriptionGroupConfig getSubscriptionGroupConfig(
        ProxyContext ctx,
        String consumerGroupName
    );

    /** 为 Proxy 客户端返回 Topic 路由（含 Broker 地址与队列分布）。 */
    ProxyTopicRouteData getTopicRouteDataForProxy(
        ProxyContext ctx,
        List<Address> requestHostAndPortList,
        String topicName
    ) throws Exception;

    default CompletableFuture<List<SendResult>> sendMessage(
        ProxyContext ctx,
        QueueSelector queueSelector,
        String producerGroup,
        int sysFlag,
        List<Message> msg
    ) {
        return sendMessage(ctx, queueSelector, producerGroup, sysFlag, msg, DEFAULT_TIMEOUT_MILLS);
    }

    /** 异步发送消息到选定队列。 */
    CompletableFuture<List<SendResult>> sendMessage(
        ProxyContext ctx,
        QueueSelector queueSelector,
        String producerGroup,
        int sysFlag,
        List<Message> msg,
        long timeoutMillis
    );

    default CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName
    ) {
        return forwardMessageToDeadLetterQueue(ctx, handle, messageId, groupName, topicName, DEFAULT_TIMEOUT_MILLS);
    }

    /** 将 POP/Pull 失败消息转发到死信队列（DLQ）。 */
    CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long timeoutMillis
    );

    default CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        String liteTopic
    ) {
        return forwardMessageToDeadLetterQueue(ctx, handle, messageId, groupName, topicName, liteTopic, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        String liteTopic,
        long timeoutMillis
    );

    default CompletableFuture<Void> endTransaction(
        ProxyContext ctx,
        String topic,
        String transactionId,
        String messageId,
        String producerGroup,
        TransactionStatus transactionStatus,
        boolean fromTransactionCheck
    ) {
        return endTransaction(ctx, topic, transactionId, messageId, producerGroup, transactionStatus, fromTransactionCheck, DEFAULT_TIMEOUT_MILLS);
    }

    /** 提交或回滚半事务消息。 */
    CompletableFuture<Void> endTransaction(
        ProxyContext ctx,
        String topic,
        String transactionId,
        String messageId,
        String producerGroup,
        TransactionStatus transactionStatus,
        boolean fromTransactionCheck,
        long timeoutMillis
    );

    /** POP 模式拉取消息（支持长轮询与 FIFO）。 */
    CompletableFuture<PopResult> popMessage(
        ProxyContext ctx,
        QueueSelector queueSelector,
        String consumerGroup,
        String topic,
        int maxMsgNums,
        long invisibleTime,
        long pollTime,
        int initMode,
        SubscriptionData subscriptionData,
        boolean fifo,
        PopMessageResultFilter popMessageResultFilter,
        String attemptId,
        long timeoutMillis
    );

    CompletableFuture<PopResult> popLiteMessage(
        ProxyContext ctx,
        QueueSelector queueSelector,
        String consumerGroup,
        String topic,
        int maxMsgNums,
        long invisibleTime,
        long pollTime,
        SubscriptionData subscriptionData,
        PopMessageResultFilter popMessageResultFilter,
        String attemptId,
        long timeoutMillis
    );

    default CompletableFuture<AckResult> ackMessage(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String consumerGroup,
        String topic
    ) {
        return ackMessage(ctx, handle, messageId, consumerGroup, topic, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<AckResult> ackMessage(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String consumerGroup,
        String topic,
        long timeoutMillis
    );

    default CompletableFuture<AckResult> ackMessage(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String consumerGroup,
        String topic,
        String liteTopic
    ) {
        return ackMessage(ctx, handle, messageId, consumerGroup, topic, liteTopic, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<AckResult> ackMessage(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String consumerGroup,
        String topic,
        String liteTopic,
        long timeoutMillis
    );

    default CompletableFuture<List<BatchAckResult>> batchAckMessage(
        ProxyContext ctx,
        List<ReceiptHandleMessage> handleMessageList,
        String consumerGroup,
        String topic
    ) {
        return batchAckMessage(ctx, handleMessageList, consumerGroup, topic, DEFAULT_TIMEOUT_MILLS);
    }

    CompletableFuture<List<BatchAckResult>> batchAckMessage(
        ProxyContext ctx,
        List<ReceiptHandleMessage> handleMessageList,
        String consumerGroup,
        String topic,
        long timeoutMillis
    );

    default CompletableFuture<AckResult> changeInvisibleTime(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long invisibleTime
    ) {
        return changeInvisibleTime(ctx, handle, messageId, groupName, topicName, invisibleTime, DEFAULT_TIMEOUT_MILLS);
    }

    default CompletableFuture<AckResult> changeInvisibleTime(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long invisibleTime,
        long timeoutMillis
    ) {
        return changeInvisibleTime(ctx, handle, messageId, groupName, topicName, invisibleTime, null, timeoutMillis, false);
    }

    default CompletableFuture<AckResult> changeInvisibleTime(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long invisibleTime,
        String liteTopic
    ) {
        return changeInvisibleTime(ctx, handle, messageId, groupName, topicName, invisibleTime, liteTopic, DEFAULT_TIMEOUT_MILLS, false);
    }

    CompletableFuture<AckResult> changeInvisibleTime(
        ProxyContext ctx,
        ReceiptHandle handle,
        String messageId,
        String groupName,
        String topicName,
        long invisibleTime,
        String liteTopic,
        long timeoutMillis,
        boolean suspend
    );

    /** Pull 模式按 offset 拉取消息。 */
    CompletableFuture<PullResult> pullMessage(
        ProxyContext ctx,
        MessageQueue messageQueue,
        String consumerGroup,
        long queueOffset,
        int maxMsgNums,
        int sysFlag,
        long commitOffset,
        long suspendTimeoutMillis,
        SubscriptionData subscriptionData,
        long timeoutMillis
    );

    /** 同步更新消费位点。 */
    CompletableFuture<Void> updateConsumerOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        String consumerGroup,
        long commitOffset,
        long timeoutMillis
    );

    CompletableFuture<Void> updateConsumerOffsetAsync(
        ProxyContext ctx,
        MessageQueue messageQueue,
        String consumerGroup,
        long commitOffset,
        long timeoutMillis
    );

    CompletableFuture<Long> queryConsumerOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        String consumerGroup,
        long timeoutMillis
    );

    /** 批量锁定 MessageQueue（顺序消费 rebalance 用）。 */
    CompletableFuture<Set<MessageQueue>> lockBatchMQ(
        ProxyContext ctx,
        Set<MessageQueue> mqSet,
        String consumerGroup,
        String clientId,
        long timeoutMillis
    );

    CompletableFuture<Void> unlockBatchMQ(
        ProxyContext ctx,
        Set<MessageQueue> mqSet,
        String consumerGroup,
        String clientId,
        long timeoutMillis
    );

    CompletableFuture<Long> getMaxOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        long timeoutMillis
    );

    CompletableFuture<Long> getMinOffset(
        ProxyContext ctx,
        MessageQueue messageQueue,
        long timeoutMillis
    );

    /** 按 recallHandle 撤回已发送消息。 */
    CompletableFuture<String> recallMessage(
        ProxyContext ctx,
        String topic,
        String recallHandle,
        long timeoutMillis
    );

    CompletableFuture<Void> syncLiteSubscription(
        ProxyContext ctx,
        LiteSubscriptionDTO liteSubscriptionDTO,
        long timeoutMillis
    );

    CompletableFuture<RemotingCommand> request(ProxyContext ctx, String brokerName, RemotingCommand request,
        long timeoutMillis);

    CompletableFuture<Void> requestOneway(ProxyContext ctx, String brokerName, RemotingCommand request,
        long timeoutMillis);

    /** 注册生产者客户端连接。 */
    void registerProducer(
        ProxyContext ctx,
        String producerGroup,
        ClientChannelInfo clientChannelInfo
    );

    void unRegisterProducer(
        ProxyContext ctx,
        String producerGroup,
        ClientChannelInfo clientChannelInfo
    );

    Channel findProducerChannel(
        ProxyContext ctx,
        String producerGroup,
        String clientId
    );

    void registerProducerListener(
        ProducerChangeListener producerChangeListener
    );

    void registerConsumer(
        ProxyContext ctx,
        String consumerGroup,
        ClientChannelInfo clientChannelInfo,
        ConsumeType consumeType,
        MessageModel messageModel,
        ConsumeFromWhere consumeFromWhere,
        Set<SubscriptionData> subList,
        boolean updateSubscription
    );

    ClientChannelInfo findConsumerChannel(
        ProxyContext ctx,
        String consumerGroup,
        Channel channel
    );

    void unRegisterConsumer(
        ProxyContext ctx,
        String consumerGroup,
        ClientChannelInfo clientChannelInfo
    );

    void registerConsumerListener(
        ConsumerIdsChangeListener consumerIdsChangeListener
    );

    void doChannelCloseEvent(String remoteAddr, Channel channel);

    ConsumerGroupInfo getConsumerGroupInfo(ProxyContext ctx, String consumerGroup);

    void addTransactionSubscription(
        ProxyContext ctx,
        String producerGroup,
        String topic
    );

    ProxyRelayService getProxyRelayService();

    MetadataService getMetadataService();

    void addReceiptHandle(ProxyContext ctx, Channel channel, String group, String msgID,
        MessageReceiptHandle messageReceiptHandle);

    MessageReceiptHandle removeReceiptHandle(ProxyContext ctx, Channel channel, String group, String msgID,
        String receiptHandle);

    /** 返回通道上指定消费组未 ACK 的 POP 消息数。 */
    int getUnackedMessageCount(ProxyContext ctx, Channel channel, String group);
}
