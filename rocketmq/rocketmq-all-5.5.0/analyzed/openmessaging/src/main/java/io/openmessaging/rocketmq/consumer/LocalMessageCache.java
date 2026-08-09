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
package io.openmessaging.rocketmq.consumer;

import io.openmessaging.KeyValue;
import io.openmessaging.Message;
import io.openmessaging.ServiceLifecycle;
import io.openmessaging.rocketmq.config.ClientConfig;
import io.openmessaging.rocketmq.domain.ConsumeRequest;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.ProcessQueue;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * Pull 消费者本地消息缓存：缓冲已拉取待 ack 的消息并维护拉取偏移与过期清理。
 */
class LocalMessageCache implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(LocalMessageCache.class);

    /** 待消费请求阻塞队列。 */
    private final BlockingQueue<ConsumeRequest> consumeRequestCache;
    /** 已 poll 出但未 ack 的消息，按 msgId 索引。 */
    private final Map<String, ConsumeRequest> consumedRequest;
    /** 各 MessageQueue 的下一次拉取偏移。 */
    private final ConcurrentHashMap<MessageQueue, Long> pullOffsetTable;
    /** 底层 RocketMQ Pull 消费者。 */
    private final DefaultMQPullConsumer rocketmqPullConsumer;
    /** OMS 客户端配置。 */
    private final ClientConfig clientConfig;
    /** 定时清理过期未 ack 消息的调度器。 */
    private final ScheduledExecutorService cleanExpireMsgExecutors;

    /** 初始化缓存、偏移表与过期清理线程池。 */
    LocalMessageCache(final DefaultMQPullConsumer rocketmqPullConsumer, final ClientConfig clientConfig) {
        consumeRequestCache = new LinkedBlockingQueue<>(clientConfig.getRmqPullMessageCacheCapacity());
        this.consumedRequest = new ConcurrentHashMap<>();
        this.pullOffsetTable = new ConcurrentHashMap<>();
        this.rocketmqPullConsumer = rocketmqPullConsumer;
        this.clientConfig = clientConfig;
        this.cleanExpireMsgExecutors = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
            "OMS_CleanExpireMsgScheduledThread_"));
    }

    /** 计算本次 Pull 批量大小（不超过缓存剩余容量）。 */
    int nextPullBatchNums() {
        return Math.min(clientConfig.getRmqPullMessageBatchNums(), consumeRequestCache.remainingCapacity());
    }

    /** 获取指定队列的下一次拉取偏移，必要时从 Broker 拉取已提交偏移。 */
    long nextPullOffset(MessageQueue remoteQueue) {
        if (!pullOffsetTable.containsKey(remoteQueue)) {
            try {
                pullOffsetTable.putIfAbsent(remoteQueue,
                    rocketmqPullConsumer.fetchConsumeOffset(remoteQueue, false));
            } catch (MQClientException e) {
                log.error("An error occurred in fetch consume offset process.", e);
            }
        }
        return pullOffsetTable.get(remoteQueue);
    }

    /** 更新队列拉取偏移。 */
    void updatePullOffset(MessageQueue remoteQueue, long nextPullOffset) {
        pullOffsetTable.put(remoteQueue, nextPullOffset);
    }

    /** 将拉取到的消费请求放入缓存（阻塞直至有空间）。 */
    void submitConsumeRequest(ConsumeRequest consumeRequest) {
        try {
            consumeRequestCache.put(consumeRequest);
        } catch (InterruptedException ignore) {
        }
    }

    MessageExt poll() {
        return poll(clientConfig.getOperationTimeout());
    }

    /** 支持通过属性覆盖 poll 超时时间。 */
    MessageExt poll(final KeyValue properties) {
        int currentPollTimeout = clientConfig.getOperationTimeout();
        if (properties.containsKey(Message.BuiltinKeys.TIMEOUT)) {
            currentPollTimeout = properties.getInt(Message.BuiltinKeys.TIMEOUT);
        }
        return poll(currentPollTimeout);
    }

    /** 从缓存取出一条消息并记录开始消费时间。 */
    private MessageExt poll(long timeout) {
        try {
            ConsumeRequest consumeRequest = consumeRequestCache.poll(timeout, TimeUnit.MILLISECONDS);
            if (consumeRequest != null) {
                MessageExt messageExt = consumeRequest.getMessageExt();
                consumeRequest.setStartConsumeTimeMillis(System.currentTimeMillis());
                MessageAccessor.setConsumeStartTimeStamp(messageExt, String.valueOf(consumeRequest.getStartConsumeTimeMillis()));
                consumedRequest.put(messageExt.getMsgId(), consumeRequest);
                return messageExt;
            }
        } catch (InterruptedException ignore) {
        }
        return null;
    }

    /** 按 msgId ack 并更新 Broker 消费进度。 */
    void ack(final String messageId) {
        ConsumeRequest consumeRequest = consumedRequest.remove(messageId);
        if (consumeRequest != null) {
            long offset = consumeRequest.getProcessQueue().removeMessage(Collections.singletonList(consumeRequest.getMessageExt()));
            try {
                rocketmqPullConsumer.updateConsumeOffset(consumeRequest.getMessageQueue(), offset);
            } catch (MQClientException e) {
                log.error("An error occurred in update consume offset process.", e);
            }
        }
    }

    /** 按队列与 ProcessQueue ack（用于过期消息回退场景）。 */
    void ack(final MessageQueue messageQueue, final ProcessQueue processQueue, final MessageExt messageExt) {
        consumedRequest.remove(messageExt.getMsgId());
        long offset = processQueue.removeMessage(Collections.singletonList(messageExt));
        try {
            rocketmqPullConsumer.updateConsumeOffset(messageQueue, offset);
        } catch (MQClientException e) {
            log.error("An error occurred in update consume offset process.", e);
        }
    }

    @Override
    /** 启动定时任务，周期性扫描并回退过期未 ack 消息。 */
    public void startup() {
        this.cleanExpireMsgExecutors.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                cleanExpireMsg();
            }
        }, clientConfig.getRmqMessageConsumeTimeout(), clientConfig.getRmqMessageConsumeTimeout(), TimeUnit.MINUTES);
    }

    @Override
    /** 优雅关闭过期清理调度器。 */
    public void shutdown() {
        ThreadUtils.shutdownGracefully(cleanExpireMsgExecutors, 5000, TimeUnit.MILLISECONDS);
    }

    /** 遍历各 ProcessQueue，将超时未 ack 的消息 sendMessageBack 并 ack。 */
    private void cleanExpireMsg() {
        for (final Map.Entry<MessageQueue, ProcessQueue> next : rocketmqPullConsumer.getDefaultMQPullConsumerImpl()
            .getRebalanceImpl().getProcessQueueTable().entrySet()) {
            ProcessQueue pq = next.getValue();
            MessageQueue mq = next.getKey();
            ReadWriteLock lockTreeMap = getLockInProcessQueue(pq);
            if (lockTreeMap == null) {
                log.error("Gets tree map lock in process queue error, may be has compatibility issue");
                return;
            }

            TreeMap<Long, MessageExt> msgTreeMap = pq.getMsgTreeMap();

            int loop = msgTreeMap.size();
            for (int i = 0; i < loop; i++) {
                MessageExt msg = null;
                try {
                    lockTreeMap.readLock().lockInterruptibly();
                    try {
                        if (!msgTreeMap.isEmpty()) {
                            msg = msgTreeMap.firstEntry().getValue();
                            if (System.currentTimeMillis() - Long.parseLong(MessageAccessor.getConsumeStartTimeStamp(msg))
                                > clientConfig.getRmqMessageConsumeTimeout() * 60 * 1000) {
                                // 已过期，后续 sendMessageBack 并 ack
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } finally {
                        lockTreeMap.readLock().unlock();
                    }
                } catch (InterruptedException e) {
                    log.error("Gets expired message exception", e);
                }

                try {
                    rocketmqPullConsumer.sendMessageBack(msg, 3);
                    log.info("Send expired msg back. topic={}, msgId={}, storeHost={}, queueId={}, queueOffset={}",
                        msg.getTopic(), msg.getMsgId(), msg.getStoreHost(), msg.getQueueId(), msg.getQueueOffset());
                    ack(mq, pq, msg);
                } catch (Exception e) {
                    log.error("Send back expired msg exception", e);
                }
            }
        }
    }

    /** 通过反射读取 ProcessQueue 内部 msgTreeMap 读写锁（兼容不同版本）。 */
    private ReadWriteLock getLockInProcessQueue(ProcessQueue pq) {
        try {
            return (ReadWriteLock) FieldUtils.readDeclaredField(pq, "lockTreeMap", true);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
