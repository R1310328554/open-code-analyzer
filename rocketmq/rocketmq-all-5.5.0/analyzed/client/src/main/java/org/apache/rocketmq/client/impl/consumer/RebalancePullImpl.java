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

import java.util.List;
import java.util.Set;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.MessageQueueListener;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

/**
 * Pull 消费者 rebalance 实现：主动拉取模式，不派发 pull 请求。
 */
public class RebalancePullImpl extends RebalanceImpl {
    /** 关联的 Pull 消费者实现。 */
    private final DefaultMQPullConsumerImpl defaultMQPullConsumerImpl;

    /** 以 Pull 消费者构造 rebalance 实现。 */
    public RebalancePullImpl(DefaultMQPullConsumerImpl defaultMQPullConsumerImpl) {
        this(null, null, null, null, defaultMQPullConsumerImpl);
    }

    public RebalancePullImpl(String consumerGroup, MessageModel messageModel,
        AllocateMessageQueueStrategy allocateMessageQueueStrategy,
        MQClientInstance mQClientFactory, DefaultMQPullConsumerImpl defaultMQPullConsumerImpl) {
        super(consumerGroup, messageModel, allocateMessageQueueStrategy, mQClientFactory);
        this.defaultMQPullConsumerImpl = defaultMQPullConsumerImpl;
    }

    @Override
    /** 队列变更时通知 {@link MessageQueueListener}。 */
    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {
        MessageQueueListener messageQueueListener = this.defaultMQPullConsumerImpl.getDefaultMQPullConsumer().getMessageQueueListener();
        if (messageQueueListener != null) {
            try {
                messageQueueListener.messageQueueChanged(topic, mqAll, mqDivided);
            } catch (Throwable e) {
                log.error("messageQueueChanged exception", e);
            }
        }
    }

    @Override
    /** 移除多余队列：持久化并删除 offset。 */
    public boolean removeUnnecessaryMessageQueue(MessageQueue mq, ProcessQueue pq) {
        this.defaultMQPullConsumerImpl.getOffsetStore().persist(mq);
        this.defaultMQPullConsumerImpl.getOffsetStore().removeOffset(mq);
        return true;
    }

    @Override
    /** 返回主动消费类型 {@link ConsumeType#CONSUME_ACTIVELY}。 */
    public ConsumeType consumeType() {
        return ConsumeType.CONSUME_ACTIVELY;
    }

    @Override
    /** 移除脏 offset 记录。 */
    public void removeDirtyOffset(final MessageQueue mq) {
        this.defaultMQPullConsumerImpl.getOffsetStore().removeOffset(mq);
    }

    @Deprecated
    @Override
    public long computePullFromWhere(MessageQueue mq) {
        return 0;
    }

    @Override
    /** Pull 模式不计算起始 offset，固定返回 0。 */
    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {
        return 0;
    }

    @Override
    /** Pull 模式不支持 initMode。 */
    public int getConsumeInitMode() {
        throw new UnsupportedOperationException("no initMode for Pull");
    }

    @Override
    /** Pull 模式由用户主动拉取，不派发请求。 */
    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {
    }

    @Override
    public void dispatchPopPullRequest(final List<PopRequest> pullRequestList, final long delay) {
    }

    @Override
    /** 创建标准 ProcessQueue。 */
    public ProcessQueue createProcessQueue() {
        return new ProcessQueue();
    }

    @Override
    /** Pull 模式不支持 POP 队列。 */
    public PopProcessQueue createPopProcessQueue() {
        return null;
    }

}
