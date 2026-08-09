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
package org.apache.rocketmq.store;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.SystemClock;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageExtBatch;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.remoting.protocol.body.HARuntimeInfo;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.exception.ConsumeQueueException;
import org.apache.rocketmq.store.ha.HAService;
import org.apache.rocketmq.store.hook.PutMessageHook;
import org.apache.rocketmq.store.hook.SendMessageBackHook;
import org.apache.rocketmq.store.logfile.MappedFile;
import org.apache.rocketmq.store.queue.ConsumeQueueInterface;
import org.apache.rocketmq.store.queue.ConsumeQueueStoreInterface;
import org.apache.rocketmq.store.rocksdb.MessageRocksDBStorage;
import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.apache.rocketmq.store.timer.TimerMessageStore;
import org.apache.rocketmq.store.timer.rocksdb.TimerMessageRocksDBStore;
import org.apache.rocketmq.store.transaction.TransMessageRocksDBStore;
import org.apache.rocketmq.store.util.PerfCounter;
import org.apache.rocketmq.store.metrics.StoreMetricsManager;
import org.rocksdb.RocksDBException;

/**
 * 消息存储核心接口：第三方厂商可据此实现自定义存储引擎。
 */
/**
 * 消息存储核心接口：定义 Broker 落盘、读取、索引、HA 同步、位点管理与统计等契约，第三方可基于此实现自定义存储引擎。
 */
public interface MessageStore {

    /**
     * 加载持久化消息与索引数据。
     *
     * @return 成功返回 true，否则 false
     */
    boolean load();

    /**
     * 启动消息存储（Reput、HA、定时线程等）。
     *
     * @throws Exception 启动异常
     */
    void start() throws Exception;

    /**
     * 关闭消息存储并释放资源。
     */
    void shutdown();

    /**
     * 销毁消息存储；通常应删除全部持久化文件。
     */
    void destroy();

    /**
     * 异步落盘单条消息；Processor 无需阻塞等待结果。
     * result when result is completed, notify the client in async manner
     *
     * @param msg 待存储消息
     * @return a CompletableFuture for the result of store operation
     */
    default CompletableFuture<PutMessageResult> asyncPutMessage(final MessageExtBrokerInner msg) {
        return CompletableFuture.completedFuture(putMessage(msg));
    }

    /**
     * 异步落盘批量消息。
     *
     * @param messageExtBatch 批量消息
     * @return a CompletableFuture for the result of store operation
     */
    default CompletableFuture<PutMessageResult> asyncPutMessages(final MessageExtBatch messageExtBatch) {
        return CompletableFuture.completedFuture(putMessages(messageExtBatch));
    }

    /**
     * 同步落盘单条消息。
     *
     * @param msg Message instance to store
     * @return result of store operation.
     */
    PutMessageResult putMessage(final MessageExtBrokerInner msg);

    /**
     * 同步落盘批量消息。
     *
     * @param messageExtBatch Message batch.
     * @return result of storing batch messages.
     */
    PutMessageResult putMessages(final MessageExtBatch messageExtBatch);

    /**
     * 从指定 Topic/队列 offset 起最多拉取 maxMsgNums 条消息。
     * from given <code>offset</code>. Resulting messages will further be screened using provided message filter.
     *
     * @param group         Consumer group that launches this query.
     * @param topic         Topic to query.
     * @param queueId       Queue ID to query.
     * @param offset        Logical offset to start from.
     * @param maxMsgNums    Maximum count of messages to query.
     * @param messageFilter Message filter used to screen desired messages.
     * @return Matched messages.
     */
    GetMessageResult getMessage(final String group, final String topic, final int queueId,
        final long offset, final int maxMsgNums, final MessageFilter messageFilter);

    /**
     * 异步拉取消息。
     * @see #getMessage(String, String, int, long, int, MessageFilter) getMessage
     *
     * @param group         Consumer group that launches this query.
     * @param topic         Topic to query.
     * @param queueId       Queue ID to query.
     * @param offset        Logical offset to start from.
     * @param maxMsgNums    Maximum count of messages to query.
     * @param messageFilter Message filter used to screen desired messages.
     * @return Matched messages.
     */
    CompletableFuture<GetMessageResult> getMessageAsync(final String group, final String topic, final int queueId,
        final long offset, final int maxMsgNums, final MessageFilter messageFilter);

    /**
     * 从指定 Topic/队列 offset 起最多拉取 maxMsgNums 条消息。
     * from given <code>offset</code>. Resulting messages will further be screened using provided message filter.
     *
     * @param group           Consumer group that launches this query.
     * @param topic           Topic to query.
     * @param queueId         Queue ID to query.
     * @param offset          Logical offset to start from.
     * @param maxMsgNums      Maximum count of messages to query.
     * @param maxTotalMsgSize Maximum total msg size of the messages
     * @param messageFilter   Message filter used to screen desired messages.
     * @return Matched messages.
     */
    GetMessageResult getMessage(final String group, final String topic, final int queueId,
        final long offset, final int maxMsgNums, final int maxTotalMsgSize, final MessageFilter messageFilter);

    /**
     * 异步拉取消息。
     * @see #getMessage(String, String, int, long, int, int, MessageFilter) getMessage
     *
     * @param group           Consumer group that launches this query.
     * @param topic           Topic to query.
     * @param queueId         Queue ID to query.
     * @param offset          Logical offset to start from.
     * @param maxMsgNums      Maximum count of messages to query.
     * @param maxTotalMsgSize Maximum total msg size of the messages
     * @param messageFilter   Message filter used to screen desired messages.
     * @return Matched messages.
     */
    CompletableFuture<GetMessageResult> getMessageAsync(final String group, final String topic, final int queueId,
        final long offset, final int maxMsgNums, final int maxTotalMsgSize, final MessageFilter messageFilter);

    /**
     * 返回 Topic 队列最大消费 offset。
     *
     * @param topic   Topic name.
     * @param queueId Queue ID.
     * @return Maximum offset at present.
     */
    long getMaxOffsetInQueue(final String topic, final int queueId) throws ConsumeQueueException;

    /**
     * 返回 Topic 队列最大消费 offset。
     *
     * @param topic     Topic name.
     * @param queueId   Queue ID.
     * @param committed return the max offset in ConsumeQueue if true, or the max offset in CommitLog if false
     * @return Maximum offset at present.
     */
    long getMaxOffsetInQueue(final String topic, final int queueId, final boolean committed) throws ConsumeQueueException;

    /**
     * 返回 Topic 队列最小消费 offset。
     *
     * @param topic   Topic name.
     * @param queueId Queue ID.
     * @return Minimum offset at present.
     */
    long getMinOffsetInQueue(final String topic, final int queueId);

    TimerMessageStore getTimerMessageStore();

    TimerMessageRocksDBStore getTimerMessageRocksDBStore();

    TransMessageRocksDBStore getTransMessageRocksDBStore();

    void setTimerMessageStore(TimerMessageStore timerMessageStore);

    void setTimerMessageRocksDBStore(TimerMessageRocksDBStore timerMessageRocksDBStore);

    void setTransMessageRocksDBStore(TransMessageRocksDBStore transMessageRocksDBStore);

    /**
     * 返回消息在 CommitLog 中的物理 offset。
     *
     * @param topic              Topic of the message to lookup.
     * @param queueId            Queue ID.
     * @param consumeQueueOffset offset of consume queue.
     * @return physical offset.
     */
    long getCommitLogOffsetInQueue(final String topic, final int queueId, final long consumeQueueOffset);

    /**
     * 按存储时间戳查找对应消息的 CommitLog 物理 offset。
     *
     * @param topic     Topic of the message.
     * @param queueId   Queue ID.
     * @param timestamp Timestamp to look up.
     * @return physical offset which matches.
     */
    long getOffsetInQueueByTime(final String topic, final int queueId, final long timestamp);

    /**
     * 按存储时间戳与边界类型查找 CommitLog 物理 offset。
     *
     * @param topic        Topic of the message.
     * @param queueId      Queue ID.
     * @param timestamp    Timestamp to look up.
     * @param boundaryType Lower or Upper
     * @return physical offset which matches.
     */
    long getOffsetInQueueByTime(final String topic, final int queueId, final long timestamp, final BoundaryType boundaryType);

    /**
     * 按 CommitLog 物理 offset 查找消息。
     *
     * @param commitLogOffset physical offset.
     * @return Message whose physical offset is as specified.
     */
    MessageExt lookMessageByOffset(final long commitLogOffset);

    /**
     * 按 CommitLog offset 与 size 查找消息。
     *
     * @param commitLogOffset physical offset.
     * @param size            message size
     * @return Message whose physical offset is as specified.
     */
    MessageExt lookMessageByOffset(long commitLogOffset, int size);

    /**
     * 从指定 CommitLog offset 读取单条消息。
     *
     * @param commitLogOffset commit log offset.
     * @return wrapped result of the message.
     */
    SelectMappedBufferResult selectOneMessageByOffset(final long commitLogOffset);

    /**
     * 从指定 CommitLog offset 读取单条消息。
     *
     * @param commitLogOffset commit log offset.
     * @param msgSize         message size.
     * @return wrapped result of the message.
     */
    SelectMappedBufferResult selectOneMessageByOffset(final long commitLogOffset, final int msgSize);

    /**
     * 返回存储运行时信息。
     *
     * @return message store running info.
     */
    String getRunningDataInfo();

    long getTimingMessageCount(String topic);

    /**
     * 存储运行时信息，通常包含各类统计数据。
     *
     * @return runtime information of the message store in format of key-value pairs.
     */
    HashMap<String, String> getRuntimeInfo();

    /**
     * HA 运行时信息。
     * @return runtime information of ha
     */
    HARuntimeInfo getHARuntimeInfo();

    /**
     * 返回 CommitLog 最大物理 offset。
     *
     * @return maximum commit log offset.
     */
    long getMaxPhyOffset();

    /**
     * 返回 CommitLog 最小物理 offset。
     *
     * @return minimum commit log offset.
     */
    long getMinPhyOffset();

    /**
     * 返回指定队列最早消息的存储时间。
     *
     * @param topic   Topic of the messages to query.
     * @param queueId Queue ID to find.
     * @return store time of the earliest message.
     */
    long getEarliestMessageTime(final String topic, final int queueId);

    /**
     * 返回 Store 中最早消息的存储时间。
     *
     * @return timestamp of the earliest message in this store.
     */
    long getEarliestMessageTime();

    /**
     * 异步返回 Store 中最早消息的存储时间。
     * @see #getEarliestMessageTime() getEarliestMessageTime
     *
     * @return timestamp of the earliest message in this store.
     */
    CompletableFuture<Long> getEarliestMessageTimeAsync(final String topic, final int queueId);

    /**
     * 返回指定消息的存储时间。
     *
     * @param topic              message topic.
     * @param queueId            queue ID.
     * @param consumeQueueOffset consume queue offset.
     * @return store timestamp of the message.
     */
    long getMessageStoreTimeStamp(final String topic, final int queueId, final long consumeQueueOffset);

    /**
     * 异步返回指定消息的存储时间。
     * @see #getMessageStoreTimeStamp(String, int, long) getMessageStoreTimeStamp
     *
     * @param topic              message topic.
     * @param queueId            queue ID.
     * @param consumeQueueOffset consume queue offset.
     * @return store timestamp of the message.
     */
    CompletableFuture<Long> getMessageStoreTimeStampAsync(final String topic, final int queueId,
        final long consumeQueueOffset);

    /**
     * 返回指定队列消息总数。
     *
     * @param topic   Topic
     * @param queueId Queue ID.
     * @return total number.
     */
    long getMessageTotalInQueue(final String topic, final int queueId);

    /**
     * 从指定 offset 读取原始 CommitLog 数据（用于复制）。
     *
     * @param offset starting offset.
     * @return commit log data.
     */
    SelectMappedBufferResult getCommitLogData(final long offset);

    /**
     * 跨多个 MappedFile 读取原始 CommitLog 数据。
     *
     * @param offset starting offset.
     * @param size   size of data to get
     * @return commit log data.
     */
    List<SelectMappedBufferResult> getBulkCommitLogData(final long offset, final int size);

    /**
     * 向 CommitLog 追加数据。
     *
     * @param startOffset starting offset.
     * @param data        data to append.
     * @param dataStart   the start index of data array
     * @param dataLength  the length of data array
     * @return 成功返回 true，否则 false
     */
    boolean appendToCommitLog(final long startOffset, final byte[] data, int dataStart, int dataLength);

    /**
     * 手动触发过期文件删除。
     */
    void executeDeleteFilesManually();

    /**
     * 按消息 key 查询索引。
     *
     * @param topic  topic of the message.
     * @param key    message key.
     * @param maxNum maximum number of the messages possible.
     * @param begin  begin timestamp.
     * @param end    end timestamp.
     */
    QueryMessageResult queryMessage(final String topic, final String key, final int maxNum, final long begin,
        final long end);

    QueryMessageResult queryMessage(final String topic, final String key, final int maxNum, final long begin, final long end, final String indexType, final String lastKey);

    /**
     * 异步按 key 查询消息。
     * @see #queryMessage(String, String, int, long, long) queryMessage
     *
     * @param topic  topic of the message.
     * @param key    message key.
     * @param maxNum maximum number of the messages possible.
     * @param begin  begin timestamp.
     * @param end    end timestamp.
     */
    CompletableFuture<QueryMessageResult> queryMessageAsync(final String topic, final String key, final int maxNum,
        final long begin, final long end);

    CompletableFuture<QueryMessageResult> queryMessageAsync(final String topic, final String key, final int maxNum, final long begin, final long end, final String indexType, final String lastKey);

    /**
     * 更新 HA Master 地址。
     *
     * @param newAddr new address.
     */
    void updateHaMasterAddress(final String newAddr);

    /**
     * 更新 Master 地址。
     *
     * @param newAddr new address.
     */
    void updateMasterAddress(final String newAddr);

    /**
     * 返回从节点落后 Master 的字节数。
     *
     * @return number of bytes that slave falls behind.
     */
    long slaveFallBehindMuch();

    /**
     * 返回 Store 当前时间戳。
     *
     * @return current time in milliseconds since 1970-01-01.
     */
    long now();

    /**
     * 删除 Topic 消费队列文件及无用统计。
     * This interface allows user delete system topic.
     *
     * @param deleteTopics unused topic name set
     * @return the number of the topics which has been deleted.
     */
    int deleteTopics(final Set<String> deleteTopics);

    /**
     * 清理不在保留集合中的 Topic。
     *
     * @param retainTopics all valid topics.
     * @return number of the topics deleted.
     */
    int cleanUnusedTopic(final Set<String> retainTopics);

    /**
     * 清理过期消费队列。
     */
    void cleanExpiredConsumerQueue();

    /**
     * 检查消息是否已被换出内存。
     *
     * @param topic         topic.
     * @param queueId       queue ID.
     * @param consumeOffset consume queue offset.
     * @return true if the message is no longer in memory; false otherwise.
     * @deprecated As of RIP-57, replaced by {@link #checkInMemByConsumeOffset(String, int, long, int)}, see <a href="https://github.com/apache/rocketmq/issues/5837">this issue</a> for more details
     */
    @Deprecated
    boolean checkInDiskByConsumeOffset(final String topic, final int queueId, long consumeOffset);

    /**
     * 检查消息是否在 PageCache 中。
     *
     * @param topic         topic.
     * @param queueId       queue ID.
     * @param consumeOffset consume queue offset.
     * @return true if the message is in page cache; false otherwise.
     */
    boolean checkInMemByConsumeOffset(final String topic, final int queueId, long consumeOffset, int batchSize);

    /**
     * 检查消息是否仍在 Store 中。
     *
     * @param topic         topic.
     * @param queueId       queue ID.
     * @param consumeOffset consume queue offset.
     * @return true if the message is in store; false otherwise.
     */
    boolean checkInStoreByConsumeOffset(final String topic, final int queueId, long consumeOffset);

    /**
     * 返回已写入 CommitLog 但尚未 dispatch 到 CQ 的字节数。
     *
     * @return number of the bytes to dispatch.
     */
    long dispatchBehindBytes();

    /**
     * 返回已写入 CommitLog 但尚未刷盘的字节数。
     *
     * @return number of the bytes to flush.
     */
    long flushBehindBytes();

    /**
     * 返回 CommitLog 中尚未 dispatch 的数据对应的时间跨度（毫秒）。
     *
     * @return number of the milliseconds to dispatch.
     */
    long dispatchBehindMilliseconds();

    /**
     * 刷盘持久化全部数据。
     *
     * @return maximum offset flushed to persistent storage device.
     */
    long flush();

    /**
     * 返回当前已刷盘 offset。
     *
     * @return flushed offset
     */
    long getFlushedWhere();

    /**
     * 返回 confirm offset。
     *
     * @return confirm offset.
     */
    long getConfirmOffset();

    /**
     * 设置 confirm offset。
     *
     * @param phyOffset confirm offset to set.
     */
    void setConfirmOffset(long phyOffset);

    /**
     * 检查 OS PageCache 是否繁忙。
     *
     * @return true if the OS page cache is busy; false otherwise.
     */
    boolean isOSPageCacheBusy();

    /**
     * 返回 Store 迄今持锁时间（毫秒）。
     *
     * @return lock time in milliseconds.
     */
    long lockTimeMills();

    /**
     * 检查 TransientStorePool 是否不足。
     *
     * @return true if the transient store pool is running out; false otherwise.
     */
    boolean isTransientStorePoolDeficient();

    /**
     * 返回 CommitLog dispatch 处理器列表。
     *
     * @return list of the dispatcher.
     */
    LinkedList<CommitLogDispatcher> getDispatcherList();

    /**
     * 添加 dispatch 处理器。
     *
     * @param dispatcher commit log dispatcher to add
     */
    void addDispatcher(CommitLogDispatcher dispatcher);

    /**
     * 获取 Topic/队列消费队列；不存在时返回 null。
     *
     * @param topic   Topic.
     * @param queueId Queue ID.
     * @return Consume queue.
     */
    ConsumeQueueInterface getConsumeQueue(String topic, int queueId);

    /**
     * 获取 Topic/队列消费队列；不存在时创建并返回。
     * @param topic   Topic.
     * @param queueId Queue ID.
     * @return Consume queue.
     */
    ConsumeQueueInterface findConsumeQueue(String topic, int queueId);

    /**
     * 返回 Broker 统计管理器。
     *
     * @return BrokerStatsManager.
     */
    BrokerStatsManager getBrokerStatsManager();

    /**
     * CommitLog 追加新消息后触发。
     *
     * @param msg           the msg that is appended to commit log
     * @param result        append message result
     * @param commitLogFile commit log file
     */
    void onCommitLogAppend(MessageExtBrokerInner msg, AppendMessageResult result, MappedFile commitLogFile);

    /**
     * 向 Store 发送 dispatch 请求时触发。
     *
     * @param dispatchRequest dispatch request
     * @param doDispatch      do dispatch if true
     * @param commitLogFile   commit log file
     * @param isRecover       is from recover process
     * @param isFileEnd       if the dispatch request represents 'file end'
     * @throws RocksDBException      only in rocksdb mode
     */
    void onCommitLogDispatch(DispatchRequest dispatchRequest, boolean doDispatch, MappedFile commitLogFile,
        boolean isRecover, boolean isFileEnd) throws RocksDBException;

    /**
     * 返回 MessageStore 配置。
     *
     * @return the message store config
     */
    MessageStoreConfig getMessageStoreConfig();

    /**
     * 返回存储统计服务。
     *
     * @return the statistics service
     */
    StoreStatsService getStoreStatsService();

    /**
     * 返回 Store 检查点组件。
     *
     * @return the checkpoint component
     */
    StoreCheckpoint getStoreCheckpoint();

    /**
     * 返回系统时钟。
     *
     * @return the system clock
     */
    SystemClock getSystemClock();

    /**
     * 返回 CommitLog 实例。
     *
     * @return the commit log
     */
    CommitLog getCommitLog();

    /**
     * 返回运行标志。
     *
     * @return running flags
     */
    RunningFlags getRunningFlags();

    /**
     * 返回 TransientStorePool。
     *
     * @return the transient store pool
     */
    TransientStorePool getTransientStorePool();

    /**
     * 返回 HA 服务。
     *
     * @return the HA service
     */
    HAService getHaService();

    /**
     * 返回 MappedFile 分配服务。
     *
     * @return the allocate-mappedFile service
     */
    AllocateMappedFileService getAllocateMappedFileService();

    /**
     * 截断脏逻辑文件。
     *
     * @param phyOffset physical offset
     * @throws RocksDBException only in rocksdb mode
     */
    void truncateDirtyLogicFiles(long phyOffset) throws RocksDBException;

    /**
     * 解锁 MappedFile。
     *
     * @param unlockMappedFile the file that needs to be unlocked
     */
    void unlockMappedFile(MappedFile unlockMappedFile);

    /**
     * 返回性能计数器。
     *
     * @return the perf counter component
     */
    PerfCounter.Ticks getPerfCounter();

    /**
     * 返回消费队列存储。
     *
     * @return the queue store
     */
    @Nonnull
    ConsumeQueueStoreInterface getQueueStore();

    /**
     * 是否配置为同步刷盘。
     *
     * @return yes if true, no if false
     */
    boolean isSyncDiskFlush();

    /**
     * 是否为 SyncMaster 角色。
     *
     * @return yes if true, no if false
     */
    boolean isSyncMaster();

    /**
     * 为消息分配队列 offset；存在竞态时需外部加锁。
     * yourself.
     *
     * @param msg        message
     * @throws RocksDBException
     */
    void assignOffset(MessageExtBrokerInner msg) throws RocksDBException;

    /**
     * 递增内存位点表中的队列 offset；存在竞态时需外部加锁。
     *
     * @param msg        message
     * @param messageNum message num
     */
    void increaseOffset(MessageExtBrokerInner msg, short messageNum);

    /**
     * BrokerContainer 中获取同进程 Master Store。
     *
     * @return
     */
    MessageStore getMasterStoreInProcess();

    /**
     * 设置同进程 Master Store 引用。
     *
     * @param masterStoreInProcess
     */
    void setMasterStoreInProcess(MessageStore masterStoreInProcess);

    /**
     * 通过 FileChannel 读取数据。
     *
     * @param offset
     * @param size
     * @param byteBuffer
     * @return
     */
    boolean getData(long offset, int size, ByteBuffer byteBuffer);

    /**
     * 设置副本组存活副本数。
     *
     * @param aliveReplicaNums number of alive replicas
     */
    void setAliveReplicaNumInGroup(int aliveReplicaNums);

    /**
     * 返回副本组存活副本数。
     *
     * @return number of alive replicas
     */
    int getAliveReplicaNumInGroup();

    /**
     * 唤醒 AutoRecoverHAClient 建立 HA 连接。
     */
    void wakeupHAClient();

    /**
     * 返回 Master 已刷盘 offset。
     *
     * @return master flushed offset
     */
    long getMasterFlushedOffset();

    /**
     * 返回 Broker 初始化最大 offset。
     *
     * @return broker max offset in startup
     */
    long getBrokerInitMaxOffset();

    /**
     * 设置 Master 已刷盘 offset。
     *
     * @param masterFlushedOffset master flushed offset
     */
    void setMasterFlushedOffset(long masterFlushedOffset);

    /**
     * 设置 Broker 初始化最大 offset。
     *
     * @param brokerInitMaxOffset broker init max offset
     */
    void setBrokerInitMaxOffset(long brokerInitMaxOffset);

    /**
     * 计算指定数据范围的校验和。
     *
     * @param from begin offset
     * @param to   end offset
     * @return checksum
     */
    byte[] calcDeltaChecksum(long from, long to);

    /**
     * 将 CommitLog 与 CQ 截断到指定 offset。
     *
     * @param offsetToTruncate offset to truncate
     * @return true if truncate succeed, false otherwise
     * @throws RocksDBException only in rocksdb mode
     */
    boolean truncateFiles(long offsetToTruncate) throws RocksDBException;

    /**
     * 检查 offset 是否与单条消息对齐。
     *
     * @param offset offset to check
     * @return true if aligned, false otherwise
     */
    boolean isOffsetAligned(long offset);

    /**
     * 返回落盘 Hook 列表。
     *
     * @return List of PutMessageHook
     */
    List<PutMessageHook> getPutMessageHookList();

    /**
     * 设置消息退回 Hook。
     *
     * @param sendMessageBackHook
     */
    void setSendMessageBackHook(SendMessageBackHook sendMessageBackHook);

    /**
     * 返回消息退回 Hook。
     *
     * @return SendMessageBackHook
     */
    SendMessageBackHook getSendMessageBackHook();

    /** 以下接口用于副本/复制模式 */
    /**
     * 返回最后一个 MappedFile 及其首条 offset。
     *
     * @return lastMappedFile first Offset
     */
    long getLastFileFromOffset();

    /**
     * 返回最后一个 MappedFile。
     *
     * @param startOffset
     * @return true when get the last mapped file, false when get null
     */
    boolean getLastMappedFile(long startOffset);

    /**
     * 设置物理 offset。
     *
     * @param phyOffset
     */
    void setPhysicalOffset(long phyOffset);

    /**
     * 返回 MappedFile 是否为空。
     *
     * @return whether mapped file is empty
     */
    boolean isMappedFilesEmpty();

    /**
     * 返回状态机版本。
     *
     * @return state machine version
     */
    long getStateMachineVersion();

    /**
     * 返回 Store 指标管理器。
     *
     * @return store metrics manager
     */
    StoreMetricsManager getStoreMetricsManager();

    /**
     * 校验消息并返回大小。
     *
     * @param byteBuffer
     * @param checkCRC
     * @param checkDupInfo
     * @param readBody
     * @return DispatchRequest
     */
    DispatchRequest checkMessageAndReturnSize(final ByteBuffer byteBuffer, final boolean checkCRC,
        final boolean checkDupInfo, final boolean readBody);

    /**
     * 返回 TransientStore 缓冲剩余数量。
     *
     * @return remain transientStoreBuffer numbers
     */
    int remainTransientStoreBufferNumbs();

    /**
     * 返回待 commit 数据量。
     *
     * @return remain how many data to commit
     */
    long remainHowManyDataToCommit();

    /**
     * 返回待 flush 数据量。
     *
     * @return remain how many data to flush
     */
    long remainHowManyDataToFlush();

    /**
     * 返回 Store 是否已 shutdown。
     *
     * @return whether shutdown
     */
    boolean isShutdown();

    /**
     * 估算 [from, to] 范围内匹配过滤器的消息数量。
     *
     * @param topic   Topic name
     * @param queueId Queue ID
     * @param from    Lower boundary of the range, inclusive.
     * @param to      Upper boundary of the range, inclusive.
     * @param filter  The message filter.
     * @return Estimate number of messages matching given filter.
     */
    long estimateMessageCount(String topic, int queueId, long from, long to, MessageFilter filter);

    /**
     * 返回 Store 指标视图。
     *
     * @return List of metrics selector and view pair
     */
    List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView();

    /**
     * 初始化 Store 指标。
     *
     * @param meter                     opentelemetry meter
     * @param attributesBuilderSupplier metrics attributes builder
     */
    void initMetrics(Meter meter, Supplier<AttributesBuilder> attributesBuilderSupplier);

    /**
     * 恢复 Topic 队列表。
     */
    void recoverTopicQueueTable();

    /**
     * 必要时通知消息到达。
     */
    void notifyMessageArriveIfNecessary(DispatchRequest dispatchRequest);

    MessageStoreStateMachine getStateMachine();

    MessageRocksDBStorage getMessageRocksDBStorage();
}
