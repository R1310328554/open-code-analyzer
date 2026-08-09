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
package org.apache.rocketmq.client.impl.consumer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.client.consumer.PopCallback;
import org.apache.rocketmq.client.consumer.PullCallback;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.hook.FilterMessageContext;
import org.apache.rocketmq.client.hook.FilterMessageHook;
import org.apache.rocketmq.client.impl.CommunicationMode;
import org.apache.rocketmq.client.impl.FindBrokerResult;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.filter.ExpressionType;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
import org.apache.rocketmq.common.sysflag.PullSysFlag;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.header.PopMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.PullMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * Pull/POP API 封装：向 Broker 发起 pull/pop 请求，解码消息并执行过滤钩子。
 */
public class PullAPIWrapper {
    private static final Logger log = LoggerFactory.getLogger(PullAPIWrapper.class);
    /** 客户端实例。 */
    private final MQClientInstance mQClientFactory;
    /** 消费组名。 */
    private final String consumerGroup;
    /** 是否单元化模式。 */
    private final boolean unitMode;
    /** 队列 -> 建议拉取 Broker 节点 ID 映射。 */
    private ConcurrentMap<MessageQueue, AtomicLong/* brokerId */> pullFromWhichNodeTable =
        new ConcurrentHashMap<>(32);
    /** 是否由用户指定连接 Broker。 */
    private volatile boolean connectBrokerByUser = false;
    /** 用户指定时的默认 Broker ID。 */
    private volatile long defaultBrokerId = MixAll.MASTER_ID;
    private Random random = new Random(System.nanoTime());
    /** 消息过滤钩子列表。 */
    private ArrayList<FilterMessageHook> filterMessageHookList = new ArrayList<>();

    /** 构造 Pull API 封装。 */
    public PullAPIWrapper(MQClientInstance mQClientFactory, String consumerGroup, boolean unitMode) {
        this.mQClientFactory = mQClientFactory;
        this.consumerGroup = consumerGroup;
        this.unitMode = unitMode;
    }

    /**
     * 处理 pull 结果：解码二进制、标签过滤、执行钩子并填充消息属性。
     *
     * @param mq 消息队列
     * @param pullResult pull 结果（须为 {@link PullResultExt}）
     * @param subscriptionData 订阅数据
     */
    public PullResult processPullResult(final MessageQueue mq, final PullResult pullResult,
        final SubscriptionData subscriptionData) {
        PullResultExt pullResultExt = (PullResultExt) pullResult;

        this.updatePullFromWhichNode(mq, pullResultExt.getSuggestWhichBrokerId());
        if (PullStatus.FOUND == pullResult.getPullStatus()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(pullResultExt.getMessageBinary());
            List<MessageExt> msgList = MessageDecoder.decodesBatch(
                byteBuffer,
                this.mQClientFactory.getClientConfig().isDecodeReadBody(),
                this.mQClientFactory.getClientConfig().isDecodeDecompressBody(),
                true
            );

            boolean needDecodeInnerMessage = false;
            for (MessageExt messageExt: msgList) {
                if (MessageSysFlag.check(messageExt.getSysFlag(), MessageSysFlag.INNER_BATCH_FLAG)
                    && MessageSysFlag.check(messageExt.getSysFlag(), MessageSysFlag.NEED_UNWRAP_FLAG)) {
                    needDecodeInnerMessage = true;
                    break;
                }
            }
            if (needDecodeInnerMessage) {
                List<MessageExt> innerMsgList = new ArrayList<>();
                try {
                    for (MessageExt messageExt: msgList) {
                        if (MessageSysFlag.check(messageExt.getSysFlag(), MessageSysFlag.INNER_BATCH_FLAG)
                            && MessageSysFlag.check(messageExt.getSysFlag(), MessageSysFlag.NEED_UNWRAP_FLAG)) {
                            MessageDecoder.decodeMessage(messageExt, innerMsgList);
                        } else {
                            innerMsgList.add(messageExt);
                        }
                    }
                    msgList = innerMsgList;
                } catch (Throwable t) {
                    log.error("Try to decode the inner batch failed for {}", pullResult.toString(), t);
                }
            }

            List<MessageExt> msgListFilterAgain = msgList;
            if (!subscriptionData.getTagsSet().isEmpty() && !subscriptionData.isClassFilterMode()) {
                msgListFilterAgain = new ArrayList<>(msgList.size());
                for (MessageExt msg : msgList) {
                    if (msg.getTags() != null) {
                        if (subscriptionData.getTagsSet().contains(msg.getTags())) {
                            msgListFilterAgain.add(msg);
                        }
                    }
                }
            }

            if (this.hasHook()) {
                FilterMessageContext filterMessageContext = new FilterMessageContext();
                filterMessageContext.setUnitMode(unitMode);
                filterMessageContext.setMsgList(msgListFilterAgain);
                this.executeHook(filterMessageContext);
            }

            for (MessageExt msg : msgListFilterAgain) {
                String traFlag = msg.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED);
                if (Boolean.parseBoolean(traFlag)) {
                    msg.setTransactionId(msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));
                }
                MessageAccessor.putProperty(msg, MessageConst.PROPERTY_MIN_OFFSET,
                    Long.toString(pullResult.getMinOffset()));
                MessageAccessor.putProperty(msg, MessageConst.PROPERTY_MAX_OFFSET,
                    Long.toString(pullResult.getMaxOffset()));
                msg.setBrokerName(mq.getBrokerName());
                msg.setQueueId(mq.getQueueId());
                if (pullResultExt.getOffsetDelta() != null) {
                    msg.setQueueOffset(pullResultExt.getOffsetDelta() + msg.getQueueOffset());
                }
            }

            pullResultExt.setMsgFoundList(msgListFilterAgain);
        }

        pullResultExt.setMessageBinary(null);

        return pullResult;
    }

    /** 更新队列建议拉取的 Broker 节点 ID。 */
    public void updatePullFromWhichNode(final MessageQueue mq, final long brokerId) {
        AtomicLong suggest = this.pullFromWhichNodeTable.get(mq);
        if (null == suggest) {
            this.pullFromWhichNodeTable.put(mq, new AtomicLong(brokerId));
        } else {
            suggest.set(brokerId);
        }
    }

    /** 是否注册了过滤钩子。 */
    public boolean hasHook() {
        return !this.filterMessageHookList.isEmpty();
    }

    /** 依次执行所有 {@link FilterMessageHook}。 */
    public void executeHook(final FilterMessageContext context) {
        if (!this.filterMessageHookList.isEmpty()) {
            for (FilterMessageHook hook : this.filterMessageHookList) {
                try {
                    hook.filterMessage(context);
                } catch (Throwable e) {
                    log.error("execute hook error. hookName={}", hook.hookName());
                }
            }
        }
    }

    /**
     * 向 Broker 内核发起 pull 请求（含 maxSizeInBytes）。
     */
    public PullResult pullKernelImpl(
        final MessageQueue mq,
        final String subExpression,
        final String expressionType,
        final long subVersion,
        final long offset,
        final int maxNums,
        final int maxSizeInBytes,
        final int sysFlag,
        final long commitOffset,
        final long brokerSuspendMaxTimeMillis,
        final long timeoutMillis,
        final CommunicationMode communicationMode,
        final PullCallback pullCallback
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        FindBrokerResult findBrokerResult =
            this.mQClientFactory.findBrokerAddressInSubscribe(this.mQClientFactory.getBrokerNameFromMessageQueue(mq),
                this.recalculatePullFromWhichNode(mq), false);
        if (null == findBrokerResult) {
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(mq.getTopic());
            findBrokerResult =
                this.mQClientFactory.findBrokerAddressInSubscribe(this.mQClientFactory.getBrokerNameFromMessageQueue(mq),
                    this.recalculatePullFromWhichNode(mq), false);
        }


        if (findBrokerResult != null) {
            {
                // 校验 Broker 版本是否支持当前表达式类型
                if (!ExpressionType.isTagType(expressionType)
                    && findBrokerResult.getBrokerVersion() < MQVersion.Version.V4_1_0_SNAPSHOT.ordinal()) {
                    throw new MQClientException("The broker[" + mq.getBrokerName() + ", "
                        + findBrokerResult.getBrokerVersion() + "] does not upgrade to support for filter message by " + expressionType, null);
                }
            }
            int sysFlagInner = sysFlag;

            if (findBrokerResult.isSlave()) {
                sysFlagInner = PullSysFlag.clearCommitOffsetFlag(sysFlagInner);
            }

            PullMessageRequestHeader requestHeader = new PullMessageRequestHeader();
            requestHeader.setConsumerGroup(this.consumerGroup);
            requestHeader.setTopic(mq.getTopic());
            requestHeader.setQueueId(mq.getQueueId());
            requestHeader.setQueueOffset(offset);
            requestHeader.setMaxMsgNums(maxNums);
            requestHeader.setSysFlag(sysFlagInner);
            requestHeader.setCommitOffset(commitOffset);
            requestHeader.setSuspendTimeoutMillis(brokerSuspendMaxTimeMillis);
            requestHeader.setSubscription(subExpression);
            requestHeader.setSubVersion(subVersion);
            requestHeader.setMaxMsgBytes(maxSizeInBytes);
            requestHeader.setExpressionType(expressionType);
            requestHeader.setBrokerName(mq.getBrokerName());

            String brokerAddr = findBrokerResult.getBrokerAddr();
            if (PullSysFlag.hasClassFilterFlag(sysFlagInner)) {
                brokerAddr = computePullFromWhichFilterServer(mq.getTopic(), brokerAddr);
            }

            PullResult pullResult = this.mQClientFactory.getMQClientAPIImpl().pullMessage(
                brokerAddr,
                requestHeader,
                timeoutMillis,
                communicationMode,
                pullCallback);

            return pullResult;
        }

        throw new MQClientException("The broker[" + mq.getBrokerName() + "] not exist", null);
    }

    public PullResult pullKernelImpl(
        MessageQueue mq,
        final String subExpression,
        final String expressionType,
        final long subVersion,
        long offset,
        final int maxNums,
        final int sysFlag,
        long commitOffset,
        final long brokerSuspendMaxTimeMillis,
        final long timeoutMillis,
        final CommunicationMode communicationMode,
        PullCallback pullCallback
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return pullKernelImpl(
                mq,
                subExpression,
                expressionType,
                subVersion, offset,
                maxNums,
                Integer.MAX_VALUE,
                sysFlag,
                commitOffset,
                brokerSuspendMaxTimeMillis,
                timeoutMillis,
                communicationMode,
                pullCallback
        );
    }

    /** 重新计算应从哪个 Broker 节点拉取。 */
    public long recalculatePullFromWhichNode(final MessageQueue mq) {
        if (this.isConnectBrokerByUser()) {
            return this.defaultBrokerId;
        }

        AtomicLong suggest = this.pullFromWhichNodeTable.get(mq);
        if (suggest != null) {
            return suggest.get();
        }

        return MixAll.MASTER_ID;
    }

    /** 计算类过滤模式下应连接的 Filter Server 地址。 */
    private String computePullFromWhichFilterServer(final String topic, final String brokerAddr)
        throws MQClientException {
        ConcurrentMap<String, TopicRouteData> topicRouteTable = this.mQClientFactory.getTopicRouteTable();
        if (topicRouteTable != null) {
            TopicRouteData topicRouteData = topicRouteTable.get(topic);
            List<String> list = topicRouteData.getFilterServerTable().get(brokerAddr);

            if (list != null && !list.isEmpty()) {
                return list.get(randomNum() % list.size());
            }
        }

        throw new MQClientException("Find Filter Server Failed, Broker Addr: " + brokerAddr + " topic: "
            + topic, null);
    }

    /** 是否用户指定 Broker 连接。 */
    public boolean isConnectBrokerByUser() {
        return connectBrokerByUser;
    }

    /** 设置是否用户指定 Broker。 */
    public void setConnectBrokerByUser(boolean connectBrokerByUser) {
        this.connectBrokerByUser = connectBrokerByUser;

    }

    public int randomNum() {
        int value = random.nextInt();
        if (value < 0) {
            value = Math.abs(value);
            if (value < 0)
                value = 0;
        }
        return value;
    }

    /** 注册消息过滤钩子列表。 */
    public void registerFilterMessageHook(ArrayList<FilterMessageHook> filterMessageHookList) {
        this.filterMessageHookList = filterMessageHookList;
    }

    /** 返回默认 Broker ID。 */
    public long getDefaultBrokerId() {
        return defaultBrokerId;
    }

    /** 设置默认 Broker ID。 */
    public void setDefaultBrokerId(long defaultBrokerId) {
        this.defaultBrokerId = defaultBrokerId;
    }


    /**
     * 异步向 Broker 发起 POP 请求。
     *
     * @param mq 消息队列
     * @param invisibleTime 消息不可见时长
     * @param maxNums 最大消息数
     * @param consumerGroup 消费组
     * @param timeout 超时（毫秒）
     * @param popCallback POP 回调
     * @param poll 是否长轮询
     * @param initMode 初始 offset 模式
     * @param order 是否顺序消费
     * @param expressionType 过滤表达式类型
     * @param expression 过滤表达式
     * @throws MQClientException
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void popAsync(MessageQueue mq, long invisibleTime, int maxNums, String consumerGroup,
                         long timeout, PopCallback popCallback, boolean poll, int initMode, boolean order, String expressionType, String expression)
        throws MQClientException, RemotingException, InterruptedException {
        FindBrokerResult findBrokerResult = this.mQClientFactory.findBrokerAddressInSubscribe(mq.getBrokerName(), MixAll.MASTER_ID, true);
        if (null == findBrokerResult) {
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(mq.getTopic());
            findBrokerResult = this.mQClientFactory.findBrokerAddressInSubscribe(mq.getBrokerName(), MixAll.MASTER_ID, true);
        }
        if (findBrokerResult != null) {
            PopMessageRequestHeader requestHeader = new PopMessageRequestHeader();
            requestHeader.setConsumerGroup(consumerGroup);
            requestHeader.setTopic(mq.getTopic());
            requestHeader.setQueueId(mq.getQueueId());
            requestHeader.setMaxMsgNums(maxNums);
            requestHeader.setInvisibleTime(invisibleTime);
            requestHeader.setInitMode(initMode);
            requestHeader.setExpType(expressionType);
            requestHeader.setExp(expression);
            requestHeader.setOrder(order);
            requestHeader.setBrokerName(mq.getBrokerName());
            // 长轮询时为服务端响应预留时间
            if (poll) {
                requestHeader.setPollTime(timeout);
                requestHeader.setBornTime(System.currentTimeMillis());
                // 长轮询时 timeout 加 10s，避免客户端过早超时
                timeout += 10 * 1000;
            }
            String brokerAddr = findBrokerResult.getBrokerAddr();
            this.mQClientFactory.getMQClientAPIImpl().popMessageAsync(mq.getBrokerName(), brokerAddr, requestHeader, timeout, popCallback);
            return;
        }
        throw new MQClientException("The broker[" + mq.getBrokerName() + "] not exist", null);
    }

}
