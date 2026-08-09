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

import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.attribute.CQType;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.store.DispatchRequest;
import org.apache.rocketmq.store.MessageFilter;
import org.rocksdb.RocksDBException;

/**
 * 消费队列接口：定义索引遍历、偏移查询与 dispatch 写入契约。
 */
public interface ConsumeQueueInterface extends FileQueueLifeCycle {
    /** 返回本消费队列所属主题名。 */
    String getTopic();

    /** 返回本消费队列的 queueId。 */
    int getQueueId();

    /**
     * 从指定逻辑索引起迭代 CqUnit。
     *
     * @param startIndex 起始索引
     * @return CqUnit 迭代器
     */
    ReferredIterator<CqUnit> iterateFrom(long startIndex);

    /**
     * 从指定索引起迭代至多 count 条 CqUnit。
     *
     * @param startIndex 起始索引
     * @param count 最多迭代条数
     * @return CqUnit 迭代器
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    ReferredIterator<CqUnit> iterateFrom(long startIndex, int count) throws RocksDBException;

    /** 按逻辑索引读取单条 CqUnit。 */
    CqUnit get(long index);

    /** 返回指定索引处的 CqUnit 及对应存储时间。 */
    Pair<CqUnit, Long> getCqUnitAndStoreTime(long index);

    /** 返回最早 CqUnit 及其存储时间。 */
    Pair<CqUnit, Long> getEarliestUnitAndStoreTime();

    /** 返回最早 CqUnit。 */
    CqUnit getEarliestUnit();

    /** 返回最新 CqUnit。 */
    CqUnit getLatestUnit();

    /** 返回最后一条消息对应的 CommitLog 物理偏移。 */
    long getLastOffset();

    /** 返回队列最小逻辑偏移（索引）。 */
    long getMinOffsetInQueue();

    /** 返回队列最大逻辑偏移（索引）。 */
    long getMaxOffsetInQueue();

    /** 返回队列中消息总条数。 */
    long getMessageTotalInQueue();

    /**
     * 查找存储时间 ≥ 给定时间戳的最小消息对应的逻辑偏移。
     *
     * @param timestamp 目标时间戳
     * @return 逻辑偏移（索引）
     */
    long getOffsetInQueueByTime(final long timestamp);

    /**
     * 按时间戳查找逻辑偏移；多条满足时由 boundaryType 决定取 Lower 或 Upper。
     *
     * @param timestamp 目标时间戳
     * @param boundaryType 边界类型（Lower/Upper）
     * @return 逻辑偏移（索引）
     */
    long getOffsetInQueueByTime(final long timestamp, final BoundaryType boundaryType);

    /**
     * 已 dispatch 到本队列的最大 CommitLog 物理偏移（不含该偏移）。
     *
     * @return 最大物理偏移
     */
    long getMaxPhysicOffset();

    /**
     * 消费队列文件与 CommitLog 可能不完全对齐，首文件或有冗余数据。
     *
     * @return 消费队列文件中最小有效物理位置
     */
    long getMinLogicOffset();

    /** 返回消费队列实现类型。 */
    CQType getCQType();

    /** 返回消费队列在磁盘上占用的总字节数。 */
    long getTotalSize();

    /** 返回单条 CqUnit 的字节大小（因实现而异）。 */
    int getUnitSize();

    /** 根据 CommitLog 最小物理偏移校正队列最小逻辑偏移。 */
    void correctMinOffset(long minCommitLogOffset);

    /** 执行 dispatch，将消息位置信息写入消费队列。 */
    void putMessagePositionInfoWrapper(DispatchRequest request);

    /**
     * 为消息分配队列逻辑偏移。
     *
     * @param queueOffsetAssigner 偏移分配器
     * @param msg 待写入消息
     * @throws RocksDBException 仅 RocksDB 模式可能抛出
     */
    void assignQueueOffset(QueueOffsetOperator queueOffsetAssigner, MessageExtBrokerInner msg) throws RocksDBException;

    /**
     * 按消息条数递增队列逻辑偏移。
     *
     * @param queueOffsetAssigner 偏移分配器
     * @param msg 消息体
     * @param messageNum 消息条数
     */
    void increaseQueueOffset(QueueOffsetOperator queueOffsetAssigner, MessageExtBrokerInner msg, short messageNum);

    /**
     * 估算指定区间内满足过滤条件的消息条数。
     *
     * @param from 起始索引（含）
     * @param to 结束索引（含）
     * @param filter 消息过滤器
     * @return 匹配条数
     */
    long estimateMessageCount(long from, long to, MessageFilter filter);

    /**
     * 初始化消费队列，将最大/最小逻辑偏移设为给定值。
     *
     * @param offset 初始逻辑偏移
     * @param minPhyOffset 最小物理偏移，用于校正 min offset
     */
    void initializeWithOffset(long offset, long minPhyOffset);
}
