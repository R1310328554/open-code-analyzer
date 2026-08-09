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

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.consumer.store.OffsetStore;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.TraceDispatcher;
import org.apache.rocketmq.client.trace.hook.ConsumeMessageTraceHookImpl;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Push 模式消费者（推荐）：底层仍为 Pull，拉取到消息后回调 {@link MessageListener}。
 * 示例见 example 模块 quickstart/Consumer。
 * <p><strong>线程安全：</strong>初始化完成后可视为线程安全。
 */
public class DefaultMQPushConsumer extends ClientConfig implements MQPushConsumer {

    private final Logger log = LoggerFactory.getLogger(DefaultMQPushConsumer.class);

    /** 内部实现，大部分逻辑委托给 {@link DefaultMQPushConsumerImpl}。 */
    protected final transient DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;

    /**
     * 消费组名：同组消费者须订阅一致以实现负载均衡，全局唯一。
     * 详见 <a href="https://rocketmq.apache.org/docs/introduction/02concepts">概念文档</a>。
     */
    private String consumerGroup;

    /**
     * 消息模型：CLUSTERING（集群，同组分摊队列）或 BROADCASTING（广播，每实例消费全量）。
     * 默认 CLUSTERING。
     */
    private MessageModel messageModel = MessageModel.CLUSTERING;

    /**
     * 启动时的消费位点策略：
     * <ul>
     * <li>CONSUME_FROM_LAST_OFFSET：从上次位点继续；新组视消息是否过期决定从头或最新</li>
     * <li>CONSUME_FROM_FIRST_OFFSET：从最早可用消息开始</li>
     * <li>CONSUME_FROM_TIMESTAMP：从 {@link #consumeTimestamp} 指定时刻开始</li>
     * </ul>
     */
    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

    /** 按时间戳回溯消费的起始时刻，格式 yyyyMMddHHmmss；默认半小时前。 */
    private String consumeTimestamp = UtilAll.timeMillisToHumanString3(System.currentTimeMillis() - (1000 * 60 * 30));

    /** 队列分配策略，决定 Topic 下各队列如何分配给同组消费者。 */
    private AllocateMessageQueueStrategy allocateMessageQueueStrategy;

    /** 订阅关系：topic → 订阅表达式（Tag/SQL）。 */
    private Map<String /* topic */, String /* sub expression */> subscription = new HashMap<>();

    /** 消息监听器（并发或顺序）。 */
    private MessageListener messageListener;

    /** Rebalance 导致队列分配变更时的回调。 */
    private MessageQueueListener messageQueueListener;

    /** 消费位点存储（本地/远程）。 */
    private OffsetStore offsetStore;

    /** 消费线程池最小线程数。 */
    private int consumeThreadMin = 20;

    /** 消费线程池最大线程数。 */
    private int consumeThreadMax = 20;

    /** 动态扩缩消费线程池的队列堆积阈值。 */
    private long adjustThreadPoolNumsThreshold = 100000;

    /** 并发消费时单队列最大位点跨度（顺序消费无效）。 */
    private int consumeConcurrentlyMaxSpan = 2000;

    /** 单队列本地缓存消息条数流控阈值（默认 1000；瞬时可能因 pullBatchSize 超限）。 */
    private int pullThresholdForQueue = 1000;

    /** POP 模式单队列待 ACK 消息数流控阈值（POP 即视为开始消费）。 */
    private int popThresholdForQueue = 96;

    /** 单队列本地缓存消息体大小上限（MiB，默认 100；仅统计 body 大小）。 */
    private int pullThresholdSizeForQueue = 100;

    /** Topic 级消息条数流控（-1 不限）；非 -1 时按分配队列数均摊到 pullThresholdForQueue。 */
    private int pullThresholdForTopic = -1;

    /** Topic 级缓存大小流控（MiB，-1 不限）；非 -1 时均摊到各队列。 */
    private int pullThresholdSizeForTopic = -1;

    /** 拉取间隔（毫秒），>0 时限流拉取频率。 */
    private long pullInterval = 0;

    /** 单次回调消费的最大消息条数。 */
    private int consumeMessageBatchMaxSize = 1;

    /** 单次从 Broker 拉取的最大消息条数。 */
    private int pullBatchSize = 32;

    private int pullBatchSizeInBytes = 256 * 1024;

    /** 是否每次 Pull 时更新订阅关系。 */
    private boolean postSubscriptionWhenPull = false;

    /** 是否为单元化订阅组。 */
    private boolean unitMode = false;

    /** 最大重试次数：并发模式 -1 表示 16，顺序模式 -1 表示 Integer.MAX_VALUE。 */
    private int maxReconsumeTimes = -1;

    /** 流控等场景下暂停拉取的时长（毫秒）。 */
    private long suspendCurrentQueueTimeMillis = 1000;

    /** 单条消息允许阻塞消费线程的最长时间（分钟）。 */
    private long consumeTimeout = 15;

    /** POP 消息不可见时长（毫秒），范围 [5000, 300000]。 */
    private long popInvisibleTime = 60000;

    /** POP 批量条数，范围 [1, 32]。 */
    private int popBatchNums = 32;

    /** shutdown 时等待在途消费完成的最长时间（毫秒），0 表示不等待。 */
    private long awaitTerminationMillisWhenShutdown = 0;

    /** 消息轨迹异步分发器。 */
    private TraceDispatcher traceDispatcher = null;

    // 强制使用客户端 Rebalance
    private boolean clientRebalance = true;

    private RPCHook rpcHook = null;

    /** 默认构造：默认消费组 + 平均分配策略。 */
    public DefaultMQPushConsumer() {
        this(MixAll.DEFAULT_CONSUMER_GROUP, null, new AllocateMessageQueueAveragely());
    }

    /**
     * 指定消费组构造。
     *
     * @param consumerGroup 消费组名
     */
    public DefaultMQPushConsumer(final String consumerGroup) {
        this(consumerGroup, null, new AllocateMessageQueueAveragely());
    }

    /**
     * 指定 RPC Hook 构造。
     *
     * @param rpcHook 每次 Remoting 请求前执行的 Hook
     */
    public DefaultMQPushConsumer(RPCHook rpcHook) {
        this(MixAll.DEFAULT_CONSUMER_GROUP, rpcHook, new AllocateMessageQueueAveragely());
    }

    /**
     * Constructor specifying consumer group, RPC hook.
     *
     * @param consumerGroup Consumer group.
     * @param rpcHook       RPC hook to execute before each remoting command.
     */
    public DefaultMQPushConsumer(final String consumerGroup, RPCHook rpcHook) {
        this(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
    }

    /**
     * Constructor specifying consumer group, enabled msg trace flag and customized trace topic name.
     *
     * @param consumerGroup        Consumer group.
     * @param enableMsgTrace       Switch flag instance for message trace.
     * @param customizedTraceTopic The name value of message trace topic.If you don't config,you can use the default trace topic name.
     */
    public DefaultMQPushConsumer(final String consumerGroup, boolean enableMsgTrace,
        final String customizedTraceTopic) {
        this(consumerGroup, null, new AllocateMessageQueueAveragely(), enableMsgTrace, customizedTraceTopic);
    }

    /**
     * Constructor specifying consumer group, RPC hook and message queue allocating algorithm.
     *
     * @param consumerGroup                Consumer group.
     * @param rpcHook                      RPC hook to execute before each remoting command.
     * @param allocateMessageQueueStrategy Message queue allocating algorithm.
     */
    public DefaultMQPushConsumer(final String consumerGroup, RPCHook rpcHook,
        AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this(consumerGroup, rpcHook, allocateMessageQueueStrategy, false, null);
    }

    /**
     * Constructor specifying consumer group, RPC hook, message queue allocating algorithm, enabled msg trace flag and customized trace topic name.
     *
     * @param consumerGroup                Consumer group.
     * @param rpcHook                      RPC hook to execute before each remoting command.
     * @param allocateMessageQueueStrategy message queue allocating algorithm.
     * @param enableMsgTrace               Switch flag instance for message trace.
     * @param customizedTraceTopic         The name value of message trace topic.If you don't config,you can use the default trace topic name.
     */
    public DefaultMQPushConsumer(final String consumerGroup, RPCHook rpcHook,
        AllocateMessageQueueStrategy allocateMessageQueueStrategy, boolean enableMsgTrace,
        final String customizedTraceTopic) {
        this.consumerGroup = consumerGroup;
        this.rpcHook = rpcHook;
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
        defaultMQPushConsumerImpl = new DefaultMQPushConsumerImpl(this, rpcHook);
        this.enableTrace = enableMsgTrace;
        this.traceTopic = customizedTraceTopic;
    }

    /**
     * Constructor specifying namespace and consumer group.
     *
     * @param namespace     Namespace for this MQ Producer instance.
     * @param consumerGroup Consumer group.
     */
    @Deprecated
    public DefaultMQPushConsumer(final String namespace, final String consumerGroup) {
        this(namespace, consumerGroup, null, new AllocateMessageQueueAveragely());
    }

    /**
     * Constructor specifying namespace, consumer group and RPC hook .
     *
     * @param namespace     Namespace for this MQ Producer instance.
     * @param consumerGroup Consumer group.
     * @param rpcHook       RPC hook to execute before each remoting command.
     */
    @Deprecated
    public DefaultMQPushConsumer(final String namespace, final String consumerGroup, RPCHook rpcHook) {
        this(namespace, consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
    }

    /**
     * Constructor specifying namespace, consumer group, RPC hook and message queue allocating algorithm.
     *
     * @param namespace                    Namespace for this MQ Producer instance.
     * @param consumerGroup                Consumer group.
     * @param rpcHook                      RPC hook to execute before each remoting command.
     * @param allocateMessageQueueStrategy Message queue allocating algorithm.
     */
    @Deprecated
    public DefaultMQPushConsumer(final String namespace, final String consumerGroup, RPCHook rpcHook,
        AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.consumerGroup = consumerGroup;
        this.namespace = namespace;
        this.rpcHook = rpcHook;
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
        defaultMQPushConsumerImpl = new DefaultMQPushConsumerImpl(this, rpcHook);
    }

    /**
     * Constructor specifying namespace, consumer group, RPC hook, message queue allocating algorithm, enabled msg trace flag and customized trace topic name.
     *
     * @param namespace                    Namespace for this MQ Producer instance.
     * @param consumerGroup                Consumer group.
     * @param rpcHook                      RPC hook to execute before each remoting command.
     * @param allocateMessageQueueStrategy message queue allocating algorithm.
     * @param enableMsgTrace               Switch flag instance for message trace.
     * @param customizedTraceTopic         The name value of message trace topic.If you don't config,you can use the default trace topic name.
     */
    @Deprecated
    public DefaultMQPushConsumer(final String namespace, final String consumerGroup, RPCHook rpcHook,
        AllocateMessageQueueStrategy allocateMessageQueueStrategy, boolean enableMsgTrace,
        final String customizedTraceTopic) {
        this.consumerGroup = consumerGroup;
        this.namespace = namespace;
        this.rpcHook = rpcHook;
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
        defaultMQPushConsumerImpl = new DefaultMQPushConsumerImpl(this, rpcHook);
        this.enableTrace = enableMsgTrace;
        this.traceTopic = customizedTraceTopic;
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public void createTopic(String key, String newTopic, int queueNum,
        Map<String, String> attributes) throws MQClientException {
        createTopic(key, withNamespace(newTopic), queueNum, 0, null);
    }

    @Override
    public void setUseTLS(boolean useTLS) {
        super.setUseTLS(useTLS);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public void createTopic(String key, String newTopic, int queueNum, int topicSysFlag,
        Map<String, String> attributes) throws MQClientException {
        this.defaultMQPushConsumerImpl.createTopic(key, withNamespace(newTopic), queueNum, topicSysFlag);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return this.defaultMQPushConsumerImpl.searchOffset(queueWithNamespace(mq), timestamp);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long maxOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.maxOffset(queueWithNamespace(mq));
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long minOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.minOffset(queueWithNamespace(mq));
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public long earliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.earliestMsgStoreTime(queueWithNamespace(mq));
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
        throws MQClientException, InterruptedException {
        return this.defaultMQPushConsumerImpl.queryMessage(withNamespace(topic), key, maxNum, begin, end);
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    @Override
    public MessageExt viewMessage(String topic,
        String msgId) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        try {
            MessageDecoder.decodeMessageId(msgId);
            return this.defaultMQPushConsumerImpl.viewMessage(withNamespace(topic), msgId);
        } catch (Exception e) {
            // Ignore
        }
        return this.defaultMQPushConsumerImpl.queryMessageByUniqKey(withNamespace(topic), msgId);
    }

    public AllocateMessageQueueStrategy getAllocateMessageQueueStrategy() {
        return allocateMessageQueueStrategy;
    }

    public void setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
    }

    public int getConsumeConcurrentlyMaxSpan() {
        return consumeConcurrentlyMaxSpan;
    }

    public void setConsumeConcurrentlyMaxSpan(int consumeConcurrentlyMaxSpan) {
        this.consumeConcurrentlyMaxSpan = consumeConcurrentlyMaxSpan;
    }

    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public int getConsumeMessageBatchMaxSize() {
        return consumeMessageBatchMaxSize;
    }

    public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
        this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }

    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public int getConsumeThreadMin() {
        return consumeThreadMin;
    }

    public void setConsumeThreadMin(int consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    public DefaultMQPushConsumerImpl getDefaultMQPushConsumerImpl() {
        return defaultMQPushConsumerImpl;
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public int getPullBatchSize() {
        return pullBatchSize;
    }

    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }

    public long getPullInterval() {
        return pullInterval;
    }

    public void setPullInterval(long pullInterval) {
        this.pullInterval = pullInterval;
    }

    public int getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }

    public void setPullThresholdForQueue(int pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }

    public int getPopThresholdForQueue() {
        return popThresholdForQueue;
    }

    public void setPopThresholdForQueue(int popThresholdForQueue) {
        this.popThresholdForQueue = popThresholdForQueue;
    }

    public int getPullThresholdForTopic() {
        return pullThresholdForTopic;
    }

    public void setPullThresholdForTopic(final int pullThresholdForTopic) {
        this.pullThresholdForTopic = pullThresholdForTopic;
    }

    public int getPullThresholdSizeForQueue() {
        return pullThresholdSizeForQueue;
    }

    public void setPullThresholdSizeForQueue(final int pullThresholdSizeForQueue) {
        this.pullThresholdSizeForQueue = pullThresholdSizeForQueue;
    }

    public int getPullThresholdSizeForTopic() {
        return pullThresholdSizeForTopic;
    }

    public void setPullThresholdSizeForTopic(final int pullThresholdSizeForTopic) {
        this.pullThresholdSizeForTopic = pullThresholdSizeForTopic;
    }

    public Map<String, String> getSubscription() {
        return subscription;
    }

    /**
     * This method will be removed in a certain version after April 5, 2020, so please do not use this method.
     */
    @Deprecated
    public void setSubscription(Map<String, String> subscription) {
        Map<String, String> subscriptionWithNamespace = new HashMap<>(subscription.size(), 1);
        for (Entry<String, String> topicEntry : subscription.entrySet()) {
            subscriptionWithNamespace.put(withNamespace(topicEntry.getKey()), topicEntry.getValue());
        }
        this.subscription = subscriptionWithNamespace;
    }

    /**
     * Send message back to broker which will be re-delivered in future.
     * <p>
     * This method will be removed or it's visibility will be changed in a certain version after April 5, 2020, so
     * please do not use this method.
     *
     * @param msg        Message to send back.
     * @param delayLevel delay level.
     * @throws RemotingException    if there is any network-tier error.
     * @throws MQBrokerException    if there is any broker error.
     * @throws InterruptedException if the thread is interrupted.
     * @throws MQClientException    if there is any client error.
     */
    @Deprecated
    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        msg.setTopic(withNamespace(msg.getTopic()));
        this.defaultMQPushConsumerImpl.sendMessageBack(msg, delayLevel, msg.getBrokerName());
    }

    /**
     * Send message back to the broker whose name is <code>brokerName</code> and the message will be re-delivered in
     * future.
     * <p>
     * This method will be removed or it's visibility will be changed in a certain version after April 5, 2020, so
     * please do not use this method.
     *
     * @param msg        Message to send back.
     * @param delayLevel delay level.
     * @param brokerName broker name.
     * @throws RemotingException    if there is any network-tier error.
     * @throws MQBrokerException    if there is any broker error.
     * @throws InterruptedException if the thread is interrupted.
     * @throws MQClientException    if there is any client error.
     */
    @Deprecated
    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel, String brokerName)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        msg.setTopic(withNamespace(msg.getTopic()));
        this.defaultMQPushConsumerImpl.sendMessageBack(msg, delayLevel, brokerName);
    }

    @Override
    public Set<MessageQueue> fetchSubscribeMessageQueues(String topic) throws MQClientException {
        return this.defaultMQPushConsumerImpl.fetchSubscribeMessageQueues(withNamespace(topic));
    }

    /**
     * This method gets internal infrastructure readily to serve. Instances must call this method after configuration.
     *
     * @throws MQClientException if there is any client error.
     */
    @Override
    public void start() throws MQClientException {
        setConsumerGroup(NamespaceUtil.wrapNamespace(this.getNamespace(), this.consumerGroup));
        this.defaultMQPushConsumerImpl.start();
        if (enableTrace) {
            try {
                AsyncTraceDispatcher dispatcher = new AsyncTraceDispatcher(consumerGroup, TraceDispatcher.Type.CONSUME, getTraceMsgBatchNum(), traceTopic, rpcHook);
                dispatcher.setHostConsumer(this.defaultMQPushConsumerImpl);
                dispatcher.setNamespaceV2(namespaceV2);
                traceDispatcher = dispatcher;
                this.defaultMQPushConsumerImpl.registerConsumeMessageHook(new ConsumeMessageTraceHookImpl(traceDispatcher));
            } catch (Throwable e) {
                log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
            }
        }
        if (null != traceDispatcher) {
            if (traceDispatcher instanceof AsyncTraceDispatcher) {
                ((AsyncTraceDispatcher) traceDispatcher).getTraceProducer().setUseTLS(isUseTLS());
            }
            try {
                traceDispatcher.start(this.getNamesrvAddr(), this.getAccessChannel());
            } catch (MQClientException e) {
                log.warn("trace dispatcher start failed ", e);
            }
        }
    }

    /**
     * Shut down this client and releasing underlying resources.
     */
    @Override
    public void shutdown() {
        this.defaultMQPushConsumerImpl.shutdown(awaitTerminationMillisWhenShutdown);
        if (null != traceDispatcher) {
            traceDispatcher.shutdown();
        }
    }

    @Override
    @Deprecated
    public void registerMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        this.defaultMQPushConsumerImpl.registerMessageListener(messageListener);
    }

    /**
     * Register a callback to execute on message arrival for concurrent consuming.
     *
     * @param messageListener message handling callback.
     */
    @Override
    public void registerMessageListener(MessageListenerConcurrently messageListener) {
        this.messageListener = messageListener;
        this.defaultMQPushConsumerImpl.registerMessageListener(messageListener);
    }

    /**
     * Register a callback to execute on message arrival for orderly consuming.
     *
     * @param messageListener message handling callback.
     */
    @Override
    public void registerMessageListener(MessageListenerOrderly messageListener) {
        this.messageListener = messageListener;
        this.defaultMQPushConsumerImpl.registerMessageListener(messageListener);
    }

    /**
     * Subscribe a topic to consuming subscription.
     *
     * @param topic         topic to subscribe.
     * @param subExpression subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br>
     *                      if null or * expression,meaning subscribe all
     * @throws MQClientException if there is any client error.
     */
    @Override
    public void subscribe(String topic, String subExpression) throws MQClientException {
        this.defaultMQPushConsumerImpl.subscribe(withNamespace(topic), subExpression);
    }

    /**
     * Subscribe a topic to consuming subscription.
     *
     * @param topic             topic to consume.
     * @param fullClassName     full class name,must extend org.apache.rocketmq.common.filter. MessageFilter
     * @param filterClassSource class source code,used UTF-8 file encoding,must be responsible for your code safety
     */
    @Override
    public void subscribe(String topic, String fullClassName, String filterClassSource) throws MQClientException {
        this.defaultMQPushConsumerImpl.subscribe(withNamespace(topic), fullClassName, filterClassSource);
    }

    /**
     * Subscribe a topic by message selector.
     *
     * @param topic           topic to consume.
     * @param messageSelector {@link org.apache.rocketmq.client.consumer.MessageSelector}
     * @see org.apache.rocketmq.client.consumer.MessageSelector#bySql
     * @see org.apache.rocketmq.client.consumer.MessageSelector#byTag
     */
    @Override
    public void subscribe(final String topic, final MessageSelector messageSelector) throws MQClientException {
        this.defaultMQPushConsumerImpl.subscribe(withNamespace(topic), messageSelector);
    }

    /**
     * Un-subscribe the specified topic from subscription.
     *
     * @param topic message topic
     */
    @Override
    public void unsubscribe(String topic) {
        this.defaultMQPushConsumerImpl.unsubscribe(topic);
    }

    /**
     * Update the message consuming thread core pool size.
     *
     * @param corePoolSize new core pool size.
     */
    @Override
    public void updateCorePoolSize(int corePoolSize) {
        this.defaultMQPushConsumerImpl.updateCorePoolSize(corePoolSize);
    }

    /**
     * Suspend pulling new messages.
     */
    @Override
    public void suspend() {
        this.defaultMQPushConsumerImpl.suspend();
    }

    /**
     * Resume pulling.
     */
    @Override
    public void resume() {
        this.defaultMQPushConsumerImpl.resume();
    }

    public boolean isPause() {
        return this.defaultMQPushConsumerImpl.isPause();
    }

    public boolean isConsumeOrderly() {
        return this.defaultMQPushConsumerImpl.isConsumeOrderly();
    }

    public void registerConsumeMessageHook(final ConsumeMessageHook hook) {
        this.defaultMQPushConsumerImpl.registerConsumeMessageHook(hook);
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

    public String getConsumeTimestamp() {
        return consumeTimestamp;
    }

    public void setConsumeTimestamp(String consumeTimestamp) {
        this.consumeTimestamp = consumeTimestamp;
    }

    public boolean isPostSubscriptionWhenPull() {
        return postSubscriptionWhenPull;
    }

    public void setPostSubscriptionWhenPull(boolean postSubscriptionWhenPull) {
        this.postSubscriptionWhenPull = postSubscriptionWhenPull;
    }

    @Override
    public boolean isUnitMode() {
        return unitMode;
    }

    @Override
    public void setUnitMode(boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }

    public long getAdjustThreadPoolNumsThreshold() {
        return adjustThreadPoolNumsThreshold;
    }

    public void setAdjustThreadPoolNumsThreshold(long adjustThreadPoolNumsThreshold) {
        this.adjustThreadPoolNumsThreshold = adjustThreadPoolNumsThreshold;
    }

    public int getMaxReconsumeTimes() {
        return maxReconsumeTimes;
    }

    public void setMaxReconsumeTimes(final int maxReconsumeTimes) {
        this.maxReconsumeTimes = maxReconsumeTimes;
    }

    public long getSuspendCurrentQueueTimeMillis() {
        return suspendCurrentQueueTimeMillis;
    }

    public void setSuspendCurrentQueueTimeMillis(final long suspendCurrentQueueTimeMillis) {
        this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    }

    public long getConsumeTimeout() {
        return consumeTimeout;
    }

    public void setConsumeTimeout(final long consumeTimeout) {
        this.consumeTimeout = consumeTimeout;
    }

    public long getPopInvisibleTime() {
        return popInvisibleTime;
    }

    public void setPopInvisibleTime(long popInvisibleTime) {
        this.popInvisibleTime = popInvisibleTime;
    }

    public long getAwaitTerminationMillisWhenShutdown() {
        return awaitTerminationMillisWhenShutdown;
    }

    public void setAwaitTerminationMillisWhenShutdown(long awaitTerminationMillisWhenShutdown) {
        this.awaitTerminationMillisWhenShutdown = awaitTerminationMillisWhenShutdown;
    }

    public int getPullBatchSizeInBytes() {
        return pullBatchSizeInBytes;
    }

    public void setPullBatchSizeInBytes(int pullBatchSizeInBytes) {
        this.pullBatchSizeInBytes = pullBatchSizeInBytes;
    }

    public TraceDispatcher getTraceDispatcher() {
        return traceDispatcher;
    }

    public int getPopBatchNums() {
        return popBatchNums;
    }

    public void setPopBatchNums(int popBatchNums) {
        this.popBatchNums = popBatchNums;
    }

    public boolean isClientRebalance() {
        return clientRebalance;
    }

    public void setClientRebalance(boolean clientRebalance) {
        this.clientRebalance = clientRebalance;
    }

    public MessageQueueListener getMessageQueueListener() {
        return messageQueueListener;
    }

    public void setMessageQueueListener(MessageQueueListener messageQueueListener) {
        this.messageQueueListener = messageQueueListener;
    }
}
