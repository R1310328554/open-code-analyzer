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
package org.apache.rocketmq.store.queue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.DefaultMessageStore;
import org.apache.rocketmq.store.DispatchRequest;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.exception.ConsumeQueueException;
import org.rocksdb.RocksDBException;

/**
 * 消费队列存储抽象基类：维护 topic-queue 表与队列偏移操作器。
 */
public abstract class AbstractConsumeQueueStore implements ConsumeQueueStoreInterface {
    protected static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /** 所属 DefaultMessageStore 实例。 */
    protected final DefaultMessageStore messageStore;
    /** 消息存储配置。 */
    protected final MessageStoreConfig messageStoreConfig;
    /** 队列逻辑偏移操作器。 */
    protected final QueueOffsetOperator queueOffsetOperator = new QueueOffsetOperator();
    /** topic → queueId → 消费队列 的二级映射表。 */
    protected final ConcurrentMap<String/* topic */, ConcurrentMap<Integer/* queueId */, ConsumeQueueInterface>> consumeQueueTable;

    /** 绑定 MessageStore，LMQ 模式下预分配更大哈希表。 */
    public AbstractConsumeQueueStore(DefaultMessageStore messageStore) {
        this.messageStore = messageStore;
        this.messageStoreConfig = messageStore.getMessageStoreConfig();
        if (messageStoreConfig.isEnableLmq()) {
            this.consumeQueueTable = new ConcurrentHashMap<>(32_768);
        } else {
            this.consumeQueueTable = new ConcurrentHashMap<>(32);
        }
    }

    /** 将 dispatch 请求写入指定消费队列。 */
    public void putMessagePositionInfoWrapper(ConsumeQueueInterface consumeQueue, DispatchRequest request) {
        consumeQueue.putMessagePositionInfoWrapper(request);
    }

    /** 从偏移表查询 topic-queue 的当前最大逻辑偏移。 */
    @Override
    public Long getMaxOffset(String topic, int queueId) throws ConsumeQueueException {
        return this.queueOffsetOperator.currentQueueOffset(topic + "-" + queueId);
    }

    public void setTopicQueueTable(ConcurrentMap<String, Long> topicQueueTable) {
        this.queueOffsetOperator.setTopicQueueTable(topicQueueTable);
        this.queueOffsetOperator.setLmqTopicQueueTable(topicQueueTable);
    }

    /** 为消息分配队列逻辑偏移（find-or-create 消费队列）。 */
    @Override
    public void assignQueueOffset(MessageExtBrokerInner msg) throws RocksDBException {
        ConsumeQueueInterface consumeQueue = findOrCreateConsumeQueue(msg.getTopic(), msg.getQueueId());
        consumeQueue.assignQueueOffset(this.queueOffsetOperator, msg);
    }

    /** 按消息条数递增队列逻辑偏移。 */
    @Override
    public void increaseQueueOffset(MessageExtBrokerInner msg, short messageNum) {
        ConsumeQueueInterface consumeQueue = findOrCreateConsumeQueue(msg.getTopic(), msg.getQueueId());
        consumeQueue.increaseQueueOffset(this.queueOffsetOperator, msg, messageNum);
    }

    @Override
    public void increaseLmqOffset(String topic, int queueId, short delta) throws ConsumeQueueException {
        queueOffsetOperator.increaseLmqOffset(topic, queueId, delta);
    }

    @Override
    public long getLmqQueueOffset(String topic, int queueId) throws ConsumeQueueException {
        return queueOffsetOperator.getLmqOffset(topic, queueId, (t, q) -> 0L);
    }

    public void removeTopicQueueTable(String topic, Integer queueId) {
        this.queueOffsetOperator.remove(topic, queueId);
    }

    @Override
    public ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueueInterface>> getConsumeQueueTable() {
        return this.consumeQueueTable;
    }

    /** 根据 CqUnit 物理位置从 CommitLog 提取存储时间戳。 */
    public long getStoreTime(CqUnit cqUnit) {
        if (cqUnit != null) {
            try {
                final long phyOffset = cqUnit.getPos();
                final int size = cqUnit.getSize();
                return this.messageStore.getCommitLog().pickupStoreTimestamp(phyOffset, size);
            } catch (Exception e) {
                log.error("Failed to getStoreTime", e);
            }
        }
        return -1;
    }

    /**
     * 获取消费队列中已 dispatch 的最大物理偏移。
     *
     * @return 消费队列最大物理偏移
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    public abstract long getMaxPhyOffsetInConsumeQueue() throws RocksDBException;

    /**
     * 销毁指定消费队列并释放资源。
     *
     * @param consumeQueue 待销毁的消费队列
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    protected abstract void destroy(ConsumeQueueInterface consumeQueue) throws RocksDBException;

    /** 删除主题下全部消费队列并清理偏移表项。 */
    @Override
    public boolean deleteTopic(String topic) {
        ConcurrentMap<Integer, ConsumeQueueInterface> queueTable = this.consumeQueueTable.get(topic);

        if (queueTable == null || queueTable.isEmpty()) {
            return false;
        }

        for (ConsumeQueueInterface cq : queueTable.values()) {
            try {
                destroy(cq);
            } catch (RocksDBException e) {
                log.error("DeleteTopic: ConsumeQueue cleans error!, topic={}, queueId={}", cq.getTopic(), cq.getQueueId(), e);
            }
            log.info("DeleteTopic: ConsumeQueue has been cleaned, topic={}, queueId={}", cq.getTopic(), cq.getQueueId());
            removeTopicQueueTable(cq.getTopic(), cq.getQueueId());
        }

        // 从消费队列表移除该 topic
        this.consumeQueueTable.remove(topic);
        return true;
    }
}
