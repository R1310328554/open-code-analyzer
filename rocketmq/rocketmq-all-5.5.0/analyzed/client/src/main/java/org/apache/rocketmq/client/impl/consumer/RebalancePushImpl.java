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
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.MessageQueueListener;
import org.apache.rocketmq.client.consumer.store.OffsetStore;
import org.apache.rocketmq.client.consumer.store.ReadOffsetType;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.ConsumeInitMode;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * Push 消费者 rebalance 实现：被动消费，负责计算 pull 起点、派发 pull/pop 请求及顺序消费解锁。
 */
public class RebalancePushImpl extends RebalanceImpl {
    /** 顺序消费解锁延迟（毫秒），兼容旧行为。 */
    private final static long UNLOCK_DELAY_TIME_MILLS = Long.parseLong(System.getProperty("rocketmq.client.unlockDelayTimeMills", "20000"));
    /** 关联的 Push 消费者实现。 */
    private final DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;


    /** 以 Push 消费者构造 rebalance 实现。 */
    public RebalancePushImpl(DefaultMQPushConsumerImpl defaultMQPushConsumerImpl) {
        this(null, null, null, null, defaultMQPushConsumerImpl);
    }

    public RebalancePushImpl(String consumerGroup, MessageModel messageModel,
        AllocateMessageQueueStrategy allocateMessageQueueStrategy,
        MQClientInstance mQClientFactory, DefaultMQPushConsumerImpl defaultMQPushConsumerImpl) {
        super(consumerGroup, messageModel, allocateMessageQueueStrategy, mQClientFactory);
        this.defaultMQPushConsumerImpl = defaultMQPushConsumerImpl;
    }

    @Override
    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {
        /*
         * rebalance 结果变化时更新订阅 version 通知 Broker，
         * 避免订阅不一致导致消费者漏消息。
         */
        SubscriptionData subscriptionData = this.subscriptionInner.get(topic);
        long newVersion = System.currentTimeMillis();
        log.info("{} Rebalance changed, also update version: {}, {}", topic, subscriptionData.getSubVersion(), newVersion);
        subscriptionData.setSubVersion(newVersion);

        int currentQueueCount = this.processQueueTable.size();
        if (currentQueueCount != 0) {
            int pullThresholdForTopic = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdForTopic();
            if (pullThresholdForTopic != -1) {
                int newVal = Math.max(1, pullThresholdForTopic / currentQueueCount);
                log.info("The pullThresholdForQueue is changed from {} to {}",
                    this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdForQueue(), newVal);
                this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().setPullThresholdForQueue(newVal);
            }

            int pullThresholdSizeForTopic = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdSizeForTopic();
            if (pullThresholdSizeForTopic != -1) {
                int newVal = Math.max(1, pullThresholdSizeForTopic / currentQueueCount);
                log.info("The pullThresholdSizeForQueue is changed from {} to {}",
                    this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdSizeForQueue(), newVal);
                this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().setPullThresholdSizeForQueue(newVal);
            }
        }

        // 通知 Broker 心跳
        this.getmQClientFactory().sendHeartbeatToAllBrokerWithLockV2(true);

        MessageQueueListener messageQueueListener = defaultMQPushConsumerImpl.getMessageQueueListener();
        if (null != messageQueueListener) {
            messageQueueListener.messageQueueChanged(topic, mqAll, mqDivided);
        }
    }

    @Override
    /** 移除多余队列：顺序消费需解锁，否则持久化 offset 后删除。 */
    public boolean removeUnnecessaryMessageQueue(final MessageQueue mq, final ProcessQueue pq) {
        if (this.defaultMQPushConsumerImpl.isConsumeOrderly()
            && MessageModel.CLUSTERING.equals(this.defaultMQPushConsumerImpl.messageModel())) {

            // 立即提交 offset
            this.defaultMQPushConsumerImpl.getOffsetStore().persist(mq);

            // 移除顺序消费队列：解锁并删除
            return tryRemoveOrderMessageQueue(mq, pq);
        } else {
            this.defaultMQPushConsumerImpl.getOffsetStore().persist(mq);
            this.defaultMQPushConsumerImpl.getOffsetStore().removeOffset(mq);
            return true;
        }
    }

    /** 尝试移除顺序消费队列：无消费或超时后解锁并删除 offset。 */
    private boolean tryRemoveOrderMessageQueue(final MessageQueue mq, final ProcessQueue pq) {
        try {
            // 无消息消费中或超过 UNLOCK_DELAY_TIME_MILLS 时强制解锁并移除（向后兼容）
            boolean forceUnlock = pq.isDropped() && System.currentTimeMillis() > pq.getLastLockTimestamp() + UNLOCK_DELAY_TIME_MILLS;
            if (forceUnlock || pq.getConsumeLock().writeLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                try {
                    RebalancePushImpl.this.defaultMQPushConsumerImpl.getOffsetStore().persist(mq);
                    RebalancePushImpl.this.defaultMQPushConsumerImpl.getOffsetStore().removeOffset(mq);

                    pq.setLocked(false);
                    RebalancePushImpl.this.unlock(mq, true);
                    return true;
                } finally {
                    if (!forceUnlock) {
                        pq.getConsumeLock().writeLock().unlock();
                    }
                }
            } else {
                pq.incTryUnlockTimes();
            }
        } catch (Exception e) {
            pq.incTryUnlockTimes();
        }

        return false;
    }

    @Override
    /** 判断是否由客户端执行 rebalance（客户端 rebalance/顺序/广播模式）。 */
    public boolean clientRebalance(String topic) {
        // POPTODO：顺序 POP 消费尚未实现
        return defaultMQPushConsumerImpl.getDefaultMQPushConsumer().isClientRebalance() || defaultMQPushConsumerImpl.isConsumeOrderly() || MessageModel.BROADCASTING.equals(messageModel);
    }

    @Override
    /** 返回被动消费类型 {@link ConsumeType#CONSUME_PASSIVELY}。 */
    public ConsumeType consumeType() {
        return ConsumeType.CONSUME_PASSIVELY;
    }

    @Override
    public void removeDirtyOffset(final MessageQueue mq) {
        this.defaultMQPushConsumerImpl.getOffsetStore().removeOffset(mq);
    }

    @Deprecated
    @Override
    public long computePullFromWhere(MessageQueue mq) {
        long result = -1L;
        try {
            result = computePullFromWhereWithException(mq);
        } catch (MQClientException e) {
            log.warn("Compute consume offset exception, mq={}", mq);
        }
        return result;
    }

    @Override
    /** 按 ConsumeFromWhere 与 offsetStore 计算 push 模式 pull 起始 offset。 */
    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {
        long result = -1;
        final ConsumeFromWhere consumeFromWhere = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getConsumeFromWhere();
        final OffsetStore offsetStore = this.defaultMQPushConsumerImpl.getOffsetStore();
        switch (consumeFromWhere) {
            case CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST:
            case CONSUME_FROM_MIN_OFFSET:
            case CONSUME_FROM_MAX_OFFSET:
            case CONSUME_FROM_LAST_OFFSET: {
                long lastOffset = offsetStore.readOffset(mq, ReadOffsetType.READ_FROM_STORE);
                if (lastOffset >= 0) {
                    result = lastOffset;
                }
                // 首次启动，无 offset
                else if (-1 == lastOffset) {
                    if (mq.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                        result = 0L;
                    } else {
                        try {
                            result = this.mQClientFactory.getMQAdminImpl().maxOffset(mq);
                        } catch (MQClientException e) {
                            log.warn("Compute consume offset from last offset exception, mq={}, exception={}", mq, e);
                            throw e;
                        }
                    }
                } else {
                    throw new MQClientException(ResponseCode.QUERY_NOT_FOUND, "Failed to query consume offset from " +
                            "offset store");
                }
                break;
            }
            case CONSUME_FROM_FIRST_OFFSET: {
                long lastOffset = offsetStore.readOffset(mq, ReadOffsetType.READ_FROM_STORE);
                if (lastOffset >= 0) {
                    result = lastOffset;
                } else if (-1 == lastOffset) {
                    // offset 将由 OFFSET_ILLEGAL 流程修正
                    result = 0L;
                } else {
                    throw new MQClientException(ResponseCode.QUERY_NOT_FOUND, "Failed to query offset from offset " +
                            "store");
                }
                break;
            }
            case CONSUME_FROM_TIMESTAMP: {
                long lastOffset = offsetStore.readOffset(mq, ReadOffsetType.READ_FROM_STORE);
                if (lastOffset >= 0) {
                    result = lastOffset;
                } else if (-1 == lastOffset) {
                    if (mq.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                        try {
                            result = this.mQClientFactory.getMQAdminImpl().maxOffset(mq);
                        } catch (MQClientException e) {
                            log.warn("Compute consume offset from last offset exception, mq={}, exception={}", mq, e);
                            throw e;
                        }
                    } else {
                        try {
                            long timestamp = UtilAll.parseDate(this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getConsumeTimestamp(),
                                UtilAll.YYYYMMDDHHMMSS).getTime();
                            result = this.mQClientFactory.getMQAdminImpl().searchOffset(mq, timestamp);
                        } catch (MQClientException e) {
                            log.warn("Compute consume offset from last offset exception, mq={}, exception={}", mq, e);
                            throw e;
                        }
                    }
                } else {
                    throw new MQClientException(ResponseCode.QUERY_NOT_FOUND, "Failed to query offset from offset " +
                            "store");
                }
                break;
            }

            default:
                break;
        }

        if (result < 0) {
            throw new MQClientException(ResponseCode.SYSTEM_ERROR, "Found unexpected result " + result);
        }

        return result;
    }

    @Override
    /** 根据 ConsumeFromWhere 返回 POP initMode（MIN 或 MAX）。 */
    public int getConsumeInitMode() {
        final ConsumeFromWhere consumeFromWhere = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getConsumeFromWhere();
        if (ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET == consumeFromWhere) {
            return ConsumeInitMode.MIN;
        } else {
            return ConsumeInitMode.MAX;
        }
    }

    @Override
    /** 将 pull 请求立即或延迟提交到 {@link PullMessageService}。 */
    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {
        for (PullRequest pullRequest : pullRequestList) {
            if (delay <= 0) {
                this.defaultMQPushConsumerImpl.executePullRequestImmediately(pullRequest);
            } else {
                this.defaultMQPushConsumerImpl.executePullRequestLater(pullRequest, delay);
            }
        }
    }

    @Override
    /** 将 POP 请求立即或延迟提交到 PullMessageService。 */
    public void dispatchPopPullRequest(final List<PopRequest> pullRequestList, final long delay) {
        for (PopRequest pullRequest : pullRequestList) {
            if (delay <= 0) {
                this.defaultMQPushConsumerImpl.executePopPullRequestImmediately(pullRequest);
            } else {
                this.defaultMQPushConsumerImpl.executePopPullRequestLater(pullRequest, delay);
            }
        }
    }

    @Override
    /** 创建标准 ProcessQueue。 */
    public ProcessQueue createProcessQueue() {
        return new ProcessQueue();
    }

    @Override
    /** 创建 PopProcessQueue。 */
    public PopProcessQueue createPopProcessQueue() {
        return new PopProcessQueue();
    }
}
