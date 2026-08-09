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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.store.queue;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.ConcurrentHashMapUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.exception.ConsumeQueueException;

/**
 * 队列偏移操作器：维护普通/批量/LMQ 三类 topic-queue 逻辑偏移表。
 */
public class QueueOffsetOperator {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /** 普通 topic-queue 下一逻辑偏移表。 */
    private ConcurrentMap<String, Long> topicQueueTable = new ConcurrentHashMap<>(1024);
    /** 批量 topic-queue 下一逻辑偏移表。 */
    private ConcurrentMap<String, Long> batchTopicQueueTable = new ConcurrentHashMap<>(1024);

    /** {TOPIC}-{QUEUE_ID} → 下一消费队列逻辑偏移（LMQ 专用）。 */
    /** LMQ topic-queue 最大逻辑偏移缓存。 */
    private ConcurrentMap<String/* topic-queue-id */, Long/* offset */> lmqTopicQueueTable = new ConcurrentHashMap<>(1024);

    /** 获取或初始化 topic-queue 当前逻辑偏移（默认 0）。 */
    public long getQueueOffset(String topicQueueKey) {
        return ConcurrentHashMapUtils.computeIfAbsent(this.topicQueueTable, topicQueueKey, k -> 0L);
    }

    public Long getTopicQueueNextOffset(String topicQueueKey) {
        return this.topicQueueTable.get(topicQueueKey);
    }

    /** 按 messageNum 递增普通 topic-queue 偏移。 */
    public void increaseQueueOffset(String topicQueueKey, short messageNum) {
        Long queueOffset = ConcurrentHashMapUtils.computeIfAbsent(this.topicQueueTable, topicQueueKey, k -> 0L);
        topicQueueTable.put(topicQueueKey, queueOffset + messageNum);
    }

    /** 直接设置 topic-queue 逻辑偏移。 */
    public void updateQueueOffset(String topicQueueKey, long offset) {
        this.topicQueueTable.put(topicQueueKey, offset);
    }

    /** 获取或初始化批量 topic-queue 偏移。 */
    public long getBatchQueueOffset(String topicQueueKey) {
        return ConcurrentHashMapUtils.computeIfAbsent(this.batchTopicQueueTable, topicQueueKey, k -> 0L);
    }

    /** 递增批量 topic-queue 偏移。 */
    public void increaseBatchQueueOffset(String topicQueueKey, short messageNum) {
        Long batchQueueOffset = ConcurrentHashMapUtils.computeIfAbsent(this.batchTopicQueueTable, topicQueueKey, k -> 0L);
        this.batchTopicQueueTable.put(topicQueueKey, batchQueueOffset + messageNum);
    }

    /** 查询 LMQ 偏移；缓存未命中时通过 callback 从持久层加载。 */
    public long getLmqOffset(String topic, int queueId, OffsetInitializer callback) throws ConsumeQueueException {
        Preconditions.checkNotNull(callback, "ConsumeQueueOffsetCallback cannot be null");
        String topicQueue = topic + "-" + queueId;
        if (!lmqTopicQueueTable.containsKey(topicQueue)) {
            // 缓存未命中时从 RocksDB 加载
            Long prev = lmqTopicQueueTable.putIfAbsent(topicQueue, callback.maxConsumeQueueOffset(topic, queueId));
            if (null != prev) {
                log.error("[BUG] Data racing, lmqTopicQueueTable should NOT contain key={}", topicQueue);
            }
        }
        return lmqTopicQueueTable.get(topicQueue);
    }

    /** 递增 LMQ 最大逻辑偏移（须已存在于缓存）。 */
    public void increaseLmqOffset(String topic, int queueId, short delta) throws ConsumeQueueException {
        String topicQueue = topic + "-" + queueId;
        if (!this.lmqTopicQueueTable.containsKey(topicQueue)) {
            throw new ConsumeQueueException(String.format("Max offset of Queue[name=%s, id=%d] should have existed", topic, queueId));
        }
        long prev = lmqTopicQueueTable.get(topicQueue);
        this.lmqTopicQueueTable.compute(topicQueue, (k, offset) -> offset + delta);
        long current = lmqTopicQueueTable.get(topicQueue);
        log.debug("Max offset of LMQ[{}:{}] increased: {} --> {}", topic, queueId, prev, current);
    }

    /** 返回 topic-queue 当前逻辑偏移，不存在时返回 0。 */
    public long currentQueueOffset(String topicQueueKey) {
        Long currentQueueOffset = this.topicQueueTable.get(topicQueueKey);
        return currentQueueOffset == null ? 0L : currentQueueOffset;
    }

    /** 从三张偏移表中移除指定 topic-queue 条目。 */
    public synchronized void remove(String topic, Integer queueId) {
        String topicQueueKey = topic + "-" + queueId;
        // 注意线程安全
        this.topicQueueTable.remove(topicQueueKey);
        this.batchTopicQueueTable.remove(topicQueueKey);
        this.lmqTopicQueueTable.remove(topicQueueKey);

        log.info("removeQueueFromTopicQueueTable OK Topic: {} QueueId: {}", topic, queueId);
    }

    public void setTopicQueueTable(ConcurrentMap<String, Long> topicQueueTable) {
        this.topicQueueTable = topicQueueTable;
    }

    /** 从 topicQueueTable 中提取 LMQ 条目填充 LMQ 偏移表。 */
    public void setLmqTopicQueueTable(ConcurrentMap<String, Long> lmqTopicQueueTable) {
        ConcurrentMap<String, Long> table = new ConcurrentHashMap<String, Long>(1024);
        for (Map.Entry<String, Long> entry : lmqTopicQueueTable.entrySet()) {
            if (MixAll.isLmq(entry.getKey())) {
                table.put(entry.getKey(), entry.getValue());
            }
        }
        this.lmqTopicQueueTable = table;
    }

    public ConcurrentMap<String, Long> getTopicQueueTable() {
        return topicQueueTable;
    }

    public void setBatchTopicQueueTable(ConcurrentMap<String, Long> batchTopicQueueTable) {
        this.batchTopicQueueTable = batchTopicQueueTable;
    }
}
