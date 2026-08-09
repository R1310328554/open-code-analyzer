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
package org.apache.rocketmq.tieredstore.file;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.store.DispatchRequest;
import org.apache.rocketmq.store.SelectMappedBufferResult;
import org.apache.rocketmq.tieredstore.common.AppendResult;

/**
 * 分层消息文件统一接口：CommitLog/CQ 追加、commit、按偏移/时间查询与生命周期管理。
 */
public interface FlatFileInterface {

    /** 返回 Topic 内部 ID。 */
    long getTopicId();

    /** 返回文件级互斥锁。 */
    Lock getFileLock();

    /** 返回所属 MessageQueue。 */
    MessageQueue getMessageQueue();

    /** 扁平文件是否已完成初始化。 */
    boolean isFlatFileInit();

    /** 以给定 baseOffset 初始化文件。 */
    void initOffset(long offset);

    /** 按 interval 尝试滚动 CommitLog 文件。 */
    boolean rollingFile(long interval);

    /**
     * 向 CommitLog 追加消息。
     *
     * @param message 消息字节缓冲
     * @return 追加结果
     */
    /** {@inheritDoc} */
    AppendResult appendCommitLog(ByteBuffer message);

    /** 从映射缓冲区追加 CommitLog 消息。 */
    AppendResult appendCommitLog(SelectMappedBufferResult message);

    /**
     * 向 ConsumeQueue 追加索引单元（不立即 commit）。
     *
     * @param request 分发请求
     * @return 追加结果
     */
    /** {@inheritDoc} */
    AppendResult appendConsumeQueue(DispatchRequest request);

    /** 释放临时资源。 */
    void release();

    /** 最早存储时间戳。 */
    long getMinStoreTimestamp();

    /** 最晚存储时间戳。 */
    long getMaxStoreTimestamp();

    /** 首条消息 consume queue offset。 */
    long getFirstMessageOffset();

    /** CommitLog 最小偏移。 */
    long getCommitLogMinOffset();

    /** CommitLog 最大 append 偏移。 */
    long getCommitLogMaxOffset();

    /** CommitLog 已 commit 偏移。 */
    long getCommitLogCommitOffset();

    /** ConsumeQueue 最小偏移。 */
    long getConsumeQueueMinOffset();

    /** ConsumeQueue 最大 append 偏移。 */
    long getConsumeQueueMaxOffset();

    /** ConsumeQueue 已 commit 偏移。 */
    long getConsumeQueueCommitOffset();

    /** 持久化 CommitLog 与 ConsumeQueue。 */
    /** {@inheritDoc} */
    CompletableFuture<Boolean> commitAsync();

    /**
     * 按 consume queue offset 异步读取完整消息体。
     *
     * @param consumeQueueOffset 消费队列偏移
     * @return 消息序列化内容
     */
    /** {@inheritDoc} */
    CompletableFuture<ByteBuffer> getMessageAsync(long consumeQueueOffset);

    /**
     * 从 CommitLog 指定偏移与长度读取消息。
     *
     * @param offset 物理偏移
     * @param length 读取长度
     * @return 消息序列化内容
     */
    /** {@inheritDoc} */
    CompletableFuture<ByteBuffer> getCommitLogAsync(long offset, int length);

    /**
     * 异步读取单条 ConsumeQueue 单元。
     *
     * @param consumeQueueOffset 消费队列偏移
     * @return CQ 单元序列化内容
     */
    /** {@inheritDoc} */
    CompletableFuture<ByteBuffer> getConsumeQueueAsync(long consumeQueueOffset);

    /**
     * 从指定 CQ offset 起连续读取 count 条 CQ 单元。
     *
     * @param consumeQueueOffset 起始偏移
     * @param count 条数
     * @return 合并后的 CQ 内容
     */
    /** {@inheritDoc} */
    CompletableFuture<ByteBuffer> getConsumeQueueAsync(long consumeQueueOffset, int count);

    /**
     * 按时间戳与边界类型在有序 CQ 上二分查找起始 offset。
     * 存储时间非递减；命中时按 lower/upper 取首/末条，未命中返回下一条 offset。
     * 示例：store time 40,50,50,50,60,60,70 对应 offset 10–16；
     * 查 35→10，45→11，50+lower→11，50+upper→13，60→14，75→17。
     *
     * @param timestamp 查询时间戳
     * @param boundaryType lower 或 upper 边界
     * @return 消费队列 offset
     */
    /** {@inheritDoc} */
    CompletableFuture<Long> getQueueOffsetByTimeAsync(long timestamp, BoundaryType boundaryType);

    /** 文件是否已关闭。 */
    boolean isClosed();

    /** 关闭文件与后台资源。 */
    /** {@inheritDoc} */
    void shutdown();

    /** 删除早于 timestamp 的过期文件段。 */
    /** {@inheritDoc} */
    void destroyExpiredFile(long timestamp);

    /** 删除全部文件数据。 */
    /** {@inheritDoc} */
    void destroy();
}
