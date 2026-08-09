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

import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.store.CommitLogDispatchStore;
import org.apache.rocketmq.store.DispatchRequest;
import org.apache.rocketmq.store.exception.ConsumeQueueException;
import org.apache.rocketmq.store.exception.StoreException;
import org.rocksdb.RocksDBException;

/**
 * 消费队列存储接口：管理全部 topic-queue 的生命周期与 dispatch。
 */
public interface ConsumeQueueStoreInterface extends CommitLogDispatchStore {

    /** 从磁盘加载消费队列数据，成功返回 true。 */
    boolean load();

    /**
     * 从文件恢复消费队列索引。
     *
     * @param concurrently 是否并发恢复
     */
    void recover(boolean concurrently) throws RocksDBException;

    /** 启动消费队列存储服务。 */
    void start();

    /** 关闭消费队列存储，成功返回 true。 */
    boolean shutdown();

    /**
     * 销毁全部消费队列。
     *
     * @param loadAfterDestroy 销毁后是否重新加载（仅 RocksDB 模式）
     */
    void destroy(boolean loadAfterDestroy);

    /** 删除指定主题的全部消费队列。 */
    boolean deleteTopic(String topic);

    /**
     * 将全部消费队列刷盘。
     *
     * @throws StoreException 刷盘失败时抛出
     */
    void flush() throws StoreException;

    /** 从 minCommitLogOffset 起清理过期消费队列数据。 */
    void cleanExpired(long minCommitLogOffset);

    /** 自检消费队列文件完整性。 */
    void checkSelf();

    /**
     * 截断脏数据。
     *
     * @param offsetToTruncate 截断边界偏移
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    void truncateDirty(long offsetToTruncate) throws RocksDBException;

    /**
     * 应用 dispatch 请求（幂等）。
     *
     * @param request dispatch 请求
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    void putMessagePositionInfoWrapper(DispatchRequest request) throws RocksDBException;

    /** 返回 topic → queueId → 消费队列 的映射表。 */
    ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueueInterface>> getConsumeQueueTable();

    /**
     * 为消息分配队列逻辑偏移。
     *
     * @param msg 消息体
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    void assignQueueOffset(MessageExtBrokerInner msg) throws RocksDBException;

    /** 按 messageNum 递增队列逻辑偏移。 */
    void increaseQueueOffset(MessageExtBrokerInner msg, short messageNum);

    /**
     * 递增 LMQ 逻辑偏移。
     *
     * @param topic 主题或 LMQ 名
     * @param queueId 队列 ID
     * @param delta 递增量
     */
    void increaseLmqOffset(String topic, int queueId, short delta) throws ConsumeQueueException;

    /**
     * 查询 LMQ 当前逻辑偏移。
     *
     * @param topic 主题名
     * @param queueId 队列 ID
     * @return 当前偏移
     */
    long getLmqQueueOffset(String topic, int queueId) throws ConsumeQueueException;

    /** 根据 minPhyOffset 恢复 topicQueue 偏移表。 */
    void recoverOffsetTable(long minPhyOffset);

    /**
     * 查询 topic-queue 在偏移表中的最大逻辑偏移。
     *
     * @param topic 主题名
     * @param queueId 队列 ID
     * @return QueueOffsetOperator 中的最大偏移
     * @throws ConsumeQueueException 查询失败时抛出
     */
    Long getMaxOffset(String topic, int queueId) throws ConsumeQueueException;

    /**
     * 查询指定 topic-queue 的最小逻辑偏移。
     *
     * @param topic 主题名
     * @param queueId 队列 ID
     * @return 最小逻辑偏移
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    long getMinOffsetInQueue(final String topic, final int queueId) throws RocksDBException;

    /**
     * 按时间戳在指定 topic-queue 中查找逻辑偏移。
     *
     * @param timestamp 目标时间戳
     * @param boundaryType 边界类型
     * @return 逻辑偏移
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    long getOffsetInQueueByTime(String topic, int queueId, long timestamp, BoundaryType boundaryType) throws RocksDBException;

    /**
     * 查找或创建消费队列。
     *
     * @param topic 主题名
     * @param queueId 队列 ID
     * @return 消费队列实例
     */
    ConsumeQueueInterface findOrCreateConsumeQueue(String topic, int queueId);

    /**
     * 仅查找消费队列，不存在时不创建。
     *
     * @param topic 主题名
     * @param queueId 队列 ID
     * @return 消费队列实例或 null
     */
    ConsumeQueueInterface getConsumeQueue(String topic, int queueId);

    /** 返回全部消费队列占用的磁盘总字节数。 */
    long getTotalSize();

    /** 返回 LMQ 消费队列数量。 */
    int getLmqNum();

    /**
     * 判断 LMQ 是否存在（语义不同于 getConsumeQueue）。
     *
     * @param lmqTopic LMQ 主题名
     * @return 是否存在
     */
    boolean isLmqExist(String lmqTopic);

}
