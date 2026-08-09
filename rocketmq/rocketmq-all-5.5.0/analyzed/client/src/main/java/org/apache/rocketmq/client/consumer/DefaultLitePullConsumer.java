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
package org.apache.rocketmq.client.consumer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.consumer.store.OffsetStore;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.TraceDispatcher;
import org.apache.rocketmq.client.trace.hook.ConsumeMessageTraceHookImpl;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;
import org.apache.rocketmq.remoting.protocol.filter.FilterAPI;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

import static org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData.SUB_ALL;

/**
 * Lite Pull 消费者：轻量级主动拉取 API，支持 subscribe/assign、自动/手动提交位点，
 * 适用于需要精确控制拉取节奏的场景。底层由 {@link DefaultLitePullConsumerImpl} 实现。
 */
public class DefaultLitePullConsumer extends ClientConfig implements LitePullConsumer {

    private static final Logger log = LoggerFactory.getLogger(DefaultLitePullConsumer.class);

    private final DefaultLitePullConsumerImpl defaultLitePullConsumerImpl;

    /** 消费组 ID：同组内队列唯一分配，实现负载均衡；多组间为发布订阅模型。 */
    private String consumerGroup;

    /** 长轮询 Broker 挂起最长时间（毫秒），不建议修改。 */
    private long brokerSuspendMaxTimeMillis = 1000 * 20;

    /** 长轮询连接超时（须大于 brokerSuspendMaxTimeMillis），不建议修改。 */
    private long consumerTimeoutMillisWhenSuspend = 1000 * 30;

    /** Pull Socket 超时（毫秒）。 */
    private long consumerPullTimeoutMillis = 1000 * 10;

    /** 消费模式，默认 CLUSTERING。 */
    private MessageModel messageModel = MessageModel.CLUSTERING;
    /** 队列分配变更监听器。 */
    private MessageQueueListener messageQueueListener;
    /** 消费位点存储。 */
    private OffsetStore offsetStore;

    /** 队列分配策略。 */
    private AllocateMessageQueueStrategy allocateMessageQueueStrategy = new AllocateMessageQueueAveragely();
    /** 是否为单元化订阅组。 */
    private boolean unitMode = false;

    /** 是否自动提交消费位点。 */
    private boolean autoCommit = true;

    /** Pull 工作线程数。 */
    private int pullThreadNums = 20;

    /** 自动提交位点的最小间隔（毫秒）。 */
    private static final long MIN_AUTOCOMMIT_INTERVAL_MILLIS = 1000;

    /** 自动提交位点的最大间隔（毫秒）。 */
    private long autoCommitIntervalMillis = 5 * 1000;

    /** 单次拉取最大消息条数。 */
    private int pullBatchSize = 10;

    /** 全局消费请求缓存流控阈值（默认 10000 条）。 */
    private long pullThresholdForAll = 10000;

    /** 消费位点最大跨度。 */
    private int consumeMaxSpan = 2000;

    /** 单队列本地缓存消息条数流控阈值。 */
    private int pullThresholdForQueue = 1000;

    /** 单队列缓存消息体大小上限（MiB，仅统计 body）。 */
    private int pullThresholdSizeForQueue = 100;

    /** poll() 阻塞超时（毫秒）。 */
    private long pollTimeoutMillis = 1000 * 5;

    /** Topic 元数据变更检测间隔（毫秒）。 */
    private long topicMetadataCheckIntervalMillis = 30 * 1000;

    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

    /** 按时间戳回溯的起始时刻，格式 yyyyMMddHHmmss；默认半小时前。 */
    private String consumeTimestamp = UtilAll.timeMillisToHumanString3(System.currentTimeMillis() - (1000 * 60 * 30));

    /** 消息轨迹分发器。 */
    private TraceDispatcher traceDispatcher = null;

    private RPCHook rpcHook;

    private final Set<SubscriptionData> subscriptionsForHeartbeat = new HashSet<>();

    /** 默认构造。 */
    public DefaultLitePullConsumer() {
        this(MixAll.DEFAULT_CONSUMER_GROUP, null);
    }

    /**
     * 指定消费组构造。
     *
     * @param consumerGroup 消费组名
     */
    public DefaultLitePullConsumer(final String consumerGroup) {
        this(consumerGroup, null);
    }

    /**
     * 指定 RPC Hook 构造。
     *
     * @param rpcHook Remoting 请求 Hook
     */
    public DefaultLitePullConsumer(RPCHook rpcHook) {
        this(MixAll.DEFAULT_CONSUMER_GROUP, rpcHook);
    }

    /**
     * 指定消费组与 RPC Hook 构造。
     *
     * @param consumerGroup 消费组名
     * @param rpcHook Remoting 请求 Hook
     */
    public DefaultLitePullConsumer(final String consumerGroup, RPCHook rpcHook) {
        this.consumerGroup = consumerGroup;
        this.rpcHook = rpcHook;
        this.enableStreamRequestType = true;
        defaultLitePullConsumerImpl = new DefaultLitePullConsumerImpl(this, rpcHook);
    }

    /**
     * Constructor specifying namespace, consumer group and RPC hook.
     *
     * @param consumerGroup Consumer group.
     * @param rpcHook       RPC hook to execute before each remoting command.
     */
    @Deprecated
    public DefaultLitePullConsumer(final String namespace, final String consumerGroup, RPCHook rpcHook) {
        this.namespace = namespace;
        this.consumerGroup = consumerGroup;
        this.rpcHook = rpcHook;
        this.enableStreamRequestType = true;
        defaultLitePullConsumerImpl = new DefaultLitePullConsumerImpl(this, rpcHook);
    }

    @Override
    public void start() throws MQClientException {
        setTraceDispatcher();
        setConsumerGroup(NamespaceUtil.wrapNamespace(this.getNamespace(), this.consumerGroup));
        this.defaultLitePullConsumerImpl.start();
        if (null != traceDispatcher) {
            try {
                traceDispatcher.start(this.getNamesrvAddr(), this.getAccessChannel());
            } catch (MQClientException e) {
                log.warn("trace dispatcher start failed ", e);
            }
        }
    }

    @Override
    public void shutdown() {
        this.defaultLitePullConsumerImpl.shutdown();
        if (null != traceDispatcher) {
            traceDispatcher.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return this.defaultLitePullConsumerImpl.isRunning();
    }

    @Override
    public void subscribe(String topic) throws MQClientException {
        this.subscribe(topic, SUB_ALL);
    }

    @Override
    public void subscribe(String topic, String subExpression) throws MQClientException {
        this.defaultLitePullConsumerImpl.subscribe(withNamespace(topic), subExpression);
    }

    @Override
    public void subscribe(String topic, MessageSelector messageSelector) throws MQClientException {
        this.defaultLitePullConsumerImpl.subscribe(withNamespace(topic), messageSelector);
    }

    @Override
    public void unsubscribe(String topic) {
        this.defaultLitePullConsumerImpl.unsubscribe(withNamespace(topic));
    }

    @Override
    public void assign(Collection<MessageQueue> messageQueues) {
        defaultLitePullConsumerImpl.assign(queuesWithNamespace(messageQueues));
    }

    @Override
    public void setSubExpressionForAssign(final String topic, final String subExpresion) {
        defaultLitePullConsumerImpl.setSubExpressionForAssign(withNamespace(topic), subExpresion);
    }

    @Override
    public List<MessageExt> poll() {
        return defaultLitePullConsumerImpl.poll(this.getPollTimeoutMillis());
    }

    @Override
    public List<MessageExt> poll(long timeout) {
        return defaultLitePullConsumerImpl.poll(timeout);
    }

    @Override
    public void seek(MessageQueue messageQueue, long offset) throws MQClientException {
        this.defaultLitePullConsumerImpl.seek(queueWithNamespace(messageQueue), offset);
    }

    @Override
    public void pause(Collection<MessageQueue> messageQueues) {
        this.defaultLitePullConsumerImpl.pause(queuesWithNamespace(messageQueues));
    }

    @Override
    public void resume(Collection<MessageQueue> messageQueues) {
        this.defaultLitePullConsumerImpl.resume(queuesWithNamespace(messageQueues));
    }

    @Override
    public Collection<MessageQueue> fetchMessageQueues(String topic) throws MQClientException {
        return this.defaultLitePullConsumerImpl.fetchMessageQueues(withNamespace(topic));
    }

    @Override
    public Long offsetForTimestamp(MessageQueue messageQueue, Long timestamp) throws MQClientException {
        return this.defaultLitePullConsumerImpl.searchOffset(queueWithNamespace(messageQueue), timestamp);
    }

    @Override
    public void registerTopicMessageQueueChangeListener(String topic,
        TopicMessageQueueChangeListener topicMessageQueueChangeListener) throws MQClientException {
        this.defaultLitePullConsumerImpl.registerTopicMessageQueueChangeListener(withNamespace(topic), topicMessageQueueChangeListener);
    }

    @Deprecated
    @Override
    public void commitSync() {
        this.defaultLitePullConsumerImpl.commitAll();
    }

    @Deprecated
    @Override
    public void commitSync(Map<MessageQueue, Long> offsetMap, boolean persist) {
        this.defaultLitePullConsumerImpl.commit(offsetMap, persist);
    }

    @Override
    public void commit() {
        this.defaultLitePullConsumerImpl.commitAll();
    }

    @Override
    public void commit(Map<MessageQueue, Long> offsetMap, boolean persist) {
        this.defaultLitePullConsumerImpl.commit(offsetMap, persist);
    }

    /**
     * Get the MessageQueue assigned in subscribe mode
     *
     * @return
     * @throws MQClientException
     */
    @Override
    public Set<MessageQueue> assignment() throws MQClientException {
        return this.defaultLitePullConsumerImpl.assignment();
    }

    /**
     * Subscribe some topic with subExpression and messageQueueListener
     *
     * @param topic
     * @param subExpression
     * @param messageQueueListener
     */
    @Override
    public void subscribe(String topic, String subExpression,
        MessageQueueListener messageQueueListener) throws MQClientException {
        this.defaultLitePullConsumerImpl.subscribe(withNamespace(topic), subExpression, messageQueueListener);
    }

    @Override
    public void commit(final Set<MessageQueue> messageQueues, boolean persist) {
        this.defaultLitePullConsumerImpl.commit(messageQueues, persist);
    }

    @Override
    public Long committed(MessageQueue messageQueue) throws MQClientException {
        return this.defaultLitePullConsumerImpl.committed(queueWithNamespace(messageQueue));
    }

    @Override
    public void updateNameServerAddress(String nameServerAddress) {
        this.defaultLitePullConsumerImpl.updateNameServerAddr(nameServerAddress);
    }

    @Override
    public void seekToBegin(MessageQueue messageQueue) throws MQClientException {
        this.defaultLitePullConsumerImpl.seekToBegin(queueWithNamespace(messageQueue));
    }

    @Override
    public void seekToEnd(MessageQueue messageQueue) throws MQClientException {
        this.defaultLitePullConsumerImpl.seekToEnd(queueWithNamespace(messageQueue));
    }

    @Override
    public boolean isAutoCommit() {
        return autoCommit;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean isConnectBrokerByUser() {
        return this.defaultLitePullConsumerImpl.getPullAPIWrapper().isConnectBrokerByUser();
    }

    public void setConnectBrokerByUser(boolean connectBrokerByUser) {
        this.defaultLitePullConsumerImpl.getPullAPIWrapper().setConnectBrokerByUser(connectBrokerByUser);
    }

    public long getDefaultBrokerId() {
        return this.defaultLitePullConsumerImpl.getPullAPIWrapper().getDefaultBrokerId();
    }

    public void setDefaultBrokerId(long defaultBrokerId) {
        this.defaultLitePullConsumerImpl.getPullAPIWrapper().setDefaultBrokerId(defaultBrokerId);
    }

    public int getPullThreadNums() {
        return pullThreadNums;
    }

    public void setPullThreadNums(int pullThreadNums) {
        this.pullThreadNums = pullThreadNums;
    }

    public long getAutoCommitIntervalMillis() {
        return autoCommitIntervalMillis;
    }

    public void setAutoCommitIntervalMillis(long autoCommitIntervalMillis) {
        if (autoCommitIntervalMillis >= MIN_AUTOCOMMIT_INTERVAL_MILLIS) {
            this.autoCommitIntervalMillis = autoCommitIntervalMillis;
        }
    }

    public int getPullBatchSize() {
        return pullBatchSize;
    }

    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }

    public long getPullThresholdForAll() {
        return pullThresholdForAll;
    }

    public void setPullThresholdForAll(long pullThresholdForAll) {
        this.pullThresholdForAll = pullThresholdForAll;
    }

    public int getConsumeMaxSpan() {
        return consumeMaxSpan;
    }

    public void setConsumeMaxSpan(int consumeMaxSpan) {
        this.consumeMaxSpan = consumeMaxSpan;
    }

    public int getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }

    public void setPullThresholdForQueue(int pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }

    public int getPullThresholdSizeForQueue() {
        return pullThresholdSizeForQueue;
    }

    public void setPullThresholdSizeForQueue(int pullThresholdSizeForQueue) {
        this.pullThresholdSizeForQueue = pullThresholdSizeForQueue;
    }

    public AllocateMessageQueueStrategy getAllocateMessageQueueStrategy() {
        return allocateMessageQueueStrategy;
    }

    public void setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
    }

    public long getBrokerSuspendMaxTimeMillis() {
        return brokerSuspendMaxTimeMillis;
    }

    public long getPollTimeoutMillis() {
        return pollTimeoutMillis;
    }

    public void setPollTimeoutMillis(long pollTimeoutMillis) {
        this.pollTimeoutMillis = pollTimeoutMillis;
    }

    public OffsetStore getOffsetStore() {
        return offsetStore;
    }

    public void setOffsetStore(OffsetStore offsetStore) {
        this.offsetStore = offsetStore;
    }

    @Override
    public boolean isUnitMode() {
        return unitMode;
    }

    @Override
    public void setUnitMode(boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public MessageQueueListener getMessageQueueListener() {
        return messageQueueListener;
    }

    public void setMessageQueueListener(MessageQueueListener messageQueueListener) {
        this.messageQueueListener = messageQueueListener;
    }

    public long getConsumerPullTimeoutMillis() {
        return consumerPullTimeoutMillis;
    }

    public void setConsumerPullTimeoutMillis(long consumerPullTimeoutMillis) {
        this.consumerPullTimeoutMillis = consumerPullTimeoutMillis;
    }

    public long getConsumerTimeoutMillisWhenSuspend() {
        return consumerTimeoutMillisWhenSuspend;
    }

    public void setConsumerTimeoutMillisWhenSuspend(long consumerTimeoutMillisWhenSuspend) {
        this.consumerTimeoutMillisWhenSuspend = consumerTimeoutMillisWhenSuspend;
    }

    public long getTopicMetadataCheckIntervalMillis() {
        return topicMetadataCheckIntervalMillis;
    }

    public void setTopicMetadataCheckIntervalMillis(long topicMetadataCheckIntervalMillis) {
        this.topicMetadataCheckIntervalMillis = topicMetadataCheckIntervalMillis;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        if (consumeFromWhere != ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET
            && consumeFromWhere != ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET
            && consumeFromWhere != ConsumeFromWhere.CONSUME_FROM_TIMESTAMP) {
            throw new RuntimeException("Invalid ConsumeFromWhere Value", null);
        }
        this.consumeFromWhere = consumeFromWhere;
    }

    public String getConsumeTimestamp() {
        return consumeTimestamp;
    }

    public void setConsumeTimestamp(String consumeTimestamp) {
        this.consumeTimestamp = consumeTimestamp;
    }

    public TraceDispatcher getTraceDispatcher() {
        return traceDispatcher;
    }

    private void setTraceDispatcher() {
        if (enableTrace) {
            try {
                AsyncTraceDispatcher traceDispatcher = new AsyncTraceDispatcher(consumerGroup, TraceDispatcher.Type.CONSUME, getTraceMsgBatchNum(), traceTopic, rpcHook);
                traceDispatcher.getTraceProducer().setUseTLS(this.isUseTLS());
                traceDispatcher.setNamespaceV2(namespaceV2);
                this.traceDispatcher = traceDispatcher;
                this.defaultLitePullConsumerImpl.registerConsumeMessageHook(
                    new ConsumeMessageTraceHookImpl(traceDispatcher));
            } catch (Throwable e) {
                log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
            }
        }
    }

    public String getCustomizedTraceTopic() {
        return traceTopic;
    }

    public void setCustomizedTraceTopic(String customizedTraceTopic) {
        this.traceTopic = customizedTraceTopic;
    }

    public boolean isEnableMsgTrace() {
        return enableTrace;
    }

    public void setEnableMsgTrace(boolean enableMsgTrace) {
        this.enableTrace = enableMsgTrace;
    }

    public Set<SubscriptionData> getSubscriptionsForHeartbeat() {
        return this.subscriptionsForHeartbeat;
    }

    public synchronized void buildSubscriptionsForHeartbeat(Map<String, MessageSelector> messageSelectorMap) throws Exception {
        this.subscriptionsForHeartbeat.clear();
        for (Map.Entry<String, MessageSelector> entry : messageSelectorMap.entrySet()) {
            SubscriptionData subscriptionData = FilterAPI.build(entry.getKey(),
                entry.getValue().getExpression(), entry.getValue().getExpressionType());
            this.subscriptionsForHeartbeat.add(subscriptionData);
        }
    }
}
