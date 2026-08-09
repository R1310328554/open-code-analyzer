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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.consumer.store.OffsetStore;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;
import org.apache.rocketmq.remoting.protocol.filter.FilterAPI;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * @deprecated 传统 Pull 消费者（2022 年后移除），主动拉取场景请改用 {@link DefaultLitePullConsumer}。
 */
@Deprecated
public class DefaultMQPullConsumer extends ClientConfig implements MQPullConsumer {

    protected final transient DefaultMQPullConsumerImpl defaultMQPullConsumerImpl;

    /** 消费组名，同组须一致且全局唯一。 */
    private String consumerGroup;
    /** 长轮询模式下 Broker 挂起连接的最长时间（毫秒），不建议修改。 */
    private long brokerSuspendMaxTimeMillis = 1000 * 20;
    /** 长轮询模式下消费者连接超时（须大于 brokerSuspendMaxTimeMillis），不建议修改。 */
    private long consumerTimeoutMillisWhenSuspend = 1000 * 30;
    /** Pull 请求 Socket 超时（毫秒）。 */
    private long consumerPullTimeoutMillis = 1000 * 10;
    /** 消费模式，默认 CLUSTERING 集群消费。 */
    private MessageModel messageModel = MessageModel.CLUSTERING;
    /** 队列分配变更监听器。 */
    private MessageQueueListener messageQueueListener;
    /** 消费位点存储。 */
    private OffsetStore offsetStore;
    /** 待注册订阅的 Topic 集合。 */
    private Set<String> registerTopics = new HashSet<>();

    private final Set<SubscriptionData> registerSubscriptions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /** 队列分配算法。 */
    private AllocateMessageQueueStrategy allocateMessageQueueStrategy = new AllocateMessageQueueAveragely();
    /** 是否为单元化订阅组。 */
    private boolean unitMode = false;

    private int maxReconsumeTimes = 16;

    private boolean enableRebalance = true;

    public DefaultMQPullConsumer() {
        this(MixAll.DEFAULT_CONSUMER_GROUP, null);
    }

    public DefaultMQPullConsumer(final String consumerGroup) {
        this(consumerGroup, null);
    }

    public DefaultMQPullConsumer(RPCHook rpcHook) {
        this(MixAll.DEFAULT_CONSUMER_GROUP, rpcHook);
    }

    public DefaultMQPullConsumer(final String consumerGroup, RPCHook rpcHook) {
        this.consumerGroup = consumerGroup;
        this.enableStreamRequestType = true;
        defaultMQPullConsumerImpl = new DefaultMQPullConsumerImpl(this, rpcHook);
    }

    /**
     * 指定命名空间、消费组与 RPC Hook 构造。
     *
     * @param consumerGroup 消费组名
     * @param rpcHook 每次 Remoting 请求前执行的 Hook
     */
    public DefaultMQPullConsumer(final String namespace, final String consumerGroup, RPCHook rpcHook) {
        this.namespace = namespace;
        this.consumerGroup = consumerGroup;
        this.enableStreamRequestType = true;
        defaultMQPullConsumerImpl = new DefaultMQPullConsumerImpl(this, rpcHook);
    }

    /** @deprecated 2020-04-05 后将移除，请勿使用。 */
    @Deprecated
    @Override
    public void createTopic(String key, String newTopic, int queueNum,
        Map<String, String> attributes) throws MQClientException {
        createTopic(key, withNamespace(newTopic), queueNum, 0, null);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public void createTopic(String key, String newTopic, int queueNum, int topicSysFlag,
        Map<String, String> attributes) throws MQClientException {
        this.defaultMQPullConsumerImpl.createTopic(key, withNamespace(newTopic), queueNum, topicSysFlag);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return this.defaultMQPullConsumerImpl.searchOffset(queueWithNamespace(mq), timestamp);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long maxOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPullConsumerImpl.maxOffset(queueWithNamespace(mq));
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long minOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPullConsumerImpl.minOffset(queueWithNamespace(mq));
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long earliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        return this.defaultMQPullConsumerImpl.earliestMsgStoreTime(queueWithNamespace(mq));
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
        throws MQClientException, InterruptedException {
        return this.defaultMQPullConsumerImpl.queryMessage(withNamespace(topic), key, maxNum, begin, end);
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

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    public void setBrokerSuspendMaxTimeMillis(long brokerSuspendMaxTimeMillis) {
        this.brokerSuspendMaxTimeMillis = brokerSuspendMaxTimeMillis;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
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

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public MessageQueueListener getMessageQueueListener() {
        return messageQueueListener;
    }

    public void setMessageQueueListener(MessageQueueListener messageQueueListener) {
        this.messageQueueListener = messageQueueListener;
    }

    public Set<String> getRegisterTopics() {
        return registerTopics;
    }

    public void setRegisterTopics(Set<String> registerTopics) {
        this.registerTopics = withNamespace(registerTopics);
    }

    public Set<SubscriptionData> getRegisterSubscriptions() {
        return registerSubscriptions;
    }

    public void addRegisterSubscriptions(String topic, MessageSelector messageSelector) throws MQClientException {
        try {
            if (messageSelector == null) {
                messageSelector = MessageSelector.byTag(SubscriptionData.SUB_ALL);
            }

            SubscriptionData subscriptionData = FilterAPI.build(withNamespace(topic),
                messageSelector.getExpression(), messageSelector.getExpressionType());

            this.registerSubscriptions.add(subscriptionData);
        } catch (Exception e) {
            throw new MQClientException("add subscription exception", e);
        }
    }

    public void clearRegisterSubscriptions() {
        this.registerSubscriptions.clear();
    }

    /**
     * This method will be removed or it's visibility will be changed in a certain version after April 5, 2020, so
     * please do not use this method.
     */
    @Deprecated
    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        msg.setTopic(withNamespace(msg.getTopic()));
        this.defaultMQPullConsumerImpl.sendMessageBack(msg, delayLevel, msg.getBrokerName());
    }

    /**
     * This method will be removed or it's visibility will be changed in a certain version after April 5, 2020, so
     * please do not use this method.
     */
    @Deprecated
    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel, String brokerName)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        msg.setTopic(withNamespace(msg.getTopic()));
        this.defaultMQPullConsumerImpl.sendMessageBack(msg, delayLevel, brokerName);
    }

    @Override
    public Set<MessageQueue> fetchSubscribeMessageQueues(String topic) throws MQClientException {
        return this.defaultMQPullConsumerImpl.fetchSubscribeMessageQueues(withNamespace(topic));
    }

    @Override
    public void start() throws MQClientException {
        this.setConsumerGroup(NamespaceUtil.wrapNamespace(this.getNamespace(), this.consumerGroup));
        this.defaultMQPullConsumerImpl.start();
    }

    @Override
    public void shutdown() {
        this.defaultMQPullConsumerImpl.shutdown();
    }

    @Override
    public void registerMessageQueueListener(String topic, MessageQueueListener listener) {
        synchronized (this.registerTopics) {
            this.registerTopics.add(withNamespace(topic));
            if (listener != null) {
                this.messageQueueListener = listener;
            }
        }
    }

    @Override
    public PullResult pull(MessageQueue mq, String subExpression, long offset, int maxNums)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), subExpression, offset, maxNums);
    }

    @Override
    public PullResult pull(MessageQueue mq, String subExpression, long offset, int maxNums, long timeout)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), subExpression, offset, maxNums, timeout);
    }

    @Override
    public PullResult pull(MessageQueue mq, MessageSelector messageSelector, long offset, int maxNums)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), messageSelector, offset, maxNums);
    }

    @Override
    public PullResult pull(MessageQueue mq, MessageSelector messageSelector, long offset, int maxNums, long timeout)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), messageSelector, offset, maxNums, timeout);
    }

    @Override
    public void pull(MessageQueue mq, String subExpression, long offset, int maxNums, PullCallback pullCallback)
        throws MQClientException, RemotingException, InterruptedException {
        this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), subExpression, offset, maxNums, pullCallback);
    }

    @Override
    public void pull(MessageQueue mq, String subExpression, long offset, int maxNums, PullCallback pullCallback,
        long timeout)
        throws MQClientException, RemotingException, InterruptedException {
        this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), subExpression, offset, maxNums, pullCallback, timeout);
    }

    @Override
    public void pull(MessageQueue mq, String subExpression, long offset, int maxNums, int maxSize,
        PullCallback pullCallback,
        long timeout)
        throws MQClientException, RemotingException, InterruptedException {
        this.defaultMQPullConsumerImpl.pull(mq, subExpression, offset, maxNums, maxSize, pullCallback, timeout);
    }

    @Override
    public void pull(MessageQueue mq, MessageSelector messageSelector, long offset, int maxNums,
        PullCallback pullCallback)
        throws MQClientException, RemotingException, InterruptedException {
        this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), messageSelector, offset, maxNums, pullCallback);
    }

    @Override
    public void pull(MessageQueue mq, MessageSelector messageSelector, long offset, int maxNums,
        PullCallback pullCallback, long timeout)
        throws MQClientException, RemotingException, InterruptedException {
        this.defaultMQPullConsumerImpl.pull(queueWithNamespace(mq), messageSelector, offset, maxNums, pullCallback, timeout);
    }

    @Override
    public PullResult pullBlockIfNotFound(MessageQueue mq, String subExpression, long offset, int maxNums)
        throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return this.defaultMQPullConsumerImpl.pullBlockIfNotFound(queueWithNamespace(mq), subExpression, offset, maxNums);
    }

    @Override
    public void pullBlockIfNotFound(MessageQueue mq, String subExpression, long offset, int maxNums,
        PullCallback pullCallback)
        throws MQClientException, RemotingException, InterruptedException {
        this.defaultMQPullConsumerImpl.pullBlockIfNotFound(queueWithNamespace(mq), subExpression, offset, maxNums, pullCallback);
    }

    @Override
    public void pullBlockIfNotFoundWithMessageSelector(MessageQueue mq, MessageSelector selector,
        long offset, int maxNums,
        PullCallback pullCallback) throws MQClientException, RemotingException, InterruptedException {
        this.defaultMQPullConsumerImpl.pullBlockIfNotFoundWithMessageSelector(mq, selector, offset, maxNums, pullCallback);
    }

    @Override
    public PullResult pullBlockIfNotFoundWithMessageSelector(MessageQueue mq, MessageSelector selector,
        long offset,
        int maxNums) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return this.defaultMQPullConsumerImpl.pullBlockIfNotFoundWithMessageSelector(mq, selector, offset, maxNums);
    }

    @Override
    public void updateConsumeOffset(MessageQueue mq, long offset) throws MQClientException {
        this.defaultMQPullConsumerImpl.updateConsumeOffset(queueWithNamespace(mq), offset);
    }

    @Override
    public long fetchConsumeOffset(MessageQueue mq, boolean fromStore) throws MQClientException {
        return this.defaultMQPullConsumerImpl.fetchConsumeOffset(queueWithNamespace(mq), fromStore);
    }

    @Override
    public Set<MessageQueue> fetchMessageQueuesInBalance(String topic) throws MQClientException {
        return this.defaultMQPullConsumerImpl.fetchMessageQueuesInBalance(withNamespace(topic));
    }

    @Override
    public MessageExt viewMessage(String topic,
        String uniqKey) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        try {
            MessageDecoder.decodeMessageId(uniqKey);
            return this.defaultMQPullConsumerImpl.viewMessage(topic, uniqKey);
        } catch (Exception e) {
            // Ignore
        }
        return this.defaultMQPullConsumerImpl.queryMessageByUniqKey(withNamespace(topic), uniqKey);
    }

    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel, String brokerName, String consumerGroup)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        msg.setTopic(withNamespace(msg.getTopic()));
        this.defaultMQPullConsumerImpl.sendMessageBack(msg, delayLevel, brokerName, consumerGroup);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    public OffsetStore getOffsetStore() {
        return offsetStore;
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    public void setOffsetStore(OffsetStore offsetStore) {
        this.offsetStore = offsetStore;
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    public DefaultMQPullConsumerImpl getDefaultMQPullConsumerImpl() {
        return defaultMQPullConsumerImpl;
    }

    @Override
    public boolean isUnitMode() {
        return unitMode;
    }

    @Override
    public void setUnitMode(boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }

    public int getMaxReconsumeTimes() {
        return maxReconsumeTimes;
    }

    public void setMaxReconsumeTimes(final int maxReconsumeTimes) {
        this.maxReconsumeTimes = maxReconsumeTimes;
    }

    public void persist(MessageQueue mq) {
        this.getOffsetStore().persist(queueWithNamespace(mq));
    }

    public boolean isEnableRebalance() {
        return enableRebalance;
    }

    public void setEnableRebalance(boolean enableRebalance) {
        this.enableRebalance = enableRebalance;
    }
}
