/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import org.redisson.api.annotation.EmptyAsAbsent;
import org.redisson.api.stream.*;
import org.redisson.client.protocol.StreamEntryStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link RStream} Reactor 响应式 API。
 * <p>需要 <b>Redis 5.0.0 及以上</b>；各方法返回 {@link Mono} 或 {@link Flux}。
 *
 * @author Nikita Koksharov
 * @param <K> 流条目字段键类型
 * @param <V> 流条目字段值类型
 */
public interface RStreamReactive<K, V> extends RExpirableReactive {

    /**
     * 创建消费者组。（Redisson API）。
     * <p>
     * Usage examples:
     * <pre>
     * StreamMessageId id = stream.createGroup(StreamCreateGroupArgs.name("test").id(id).makeStream());
     * </pre>
     *
     * @param args 方法参数对象
     */
    Mono<Void> createGroup(StreamCreateGroupArgs args);

    /**
     * 按名称移除消费者组。（Redisson API）。
     * 
     * @param groupName 消费者组名称
     * @return 无返回值
     */
    Mono<Void> removeGroup(String groupName);

    /**
     * 在指定组下创建消费者。（Redisson API）。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param groupName 消费者组名称
     * @param consumerName 消费者名称
     */
    Mono<Void> createConsumer(String groupName, String consumerName);

    /**
     * 移除指定组下的消费者。（Redisson API）。
     * 
     * @param groupName 消费者组名称
     * @param consumerName 消费者名称
     * @return number of pending messages owned by consumer
     */
    Mono<Long> removeConsumer(String groupName, String consumerName);
    
    /**
     * 更新投递给消费者的下一条消息 ID。（Redisson API）。
     * 
     * @param groupName 消费者组名称
     * @param id Stream 消息 ID
     * @return 无返回值
     */
    Mono<Void> updateGroupMessageId(String groupName, StreamMessageId id);
    
    /**
     * Redis Stream 相关操作：Marks pending messages by group name and stream <code>ids</code> as correctly processed.。
     * 
     * @param groupName 消费者组名称
     * @param ids - stream ids
     * @return marked messages amount
     */
    Mono<Long> ack(String groupName, StreamMessageId... ids);

    /**
     * 确认并条件删除一条或多条流消息。（Redisson API）。
     * for a stream consumer group at the specified key.
     *
     * Requires <b>Redis 8.2.0 and higher.</b>
     *
     * @param args - method arguments object
     * @return map with entry statuses mapped by id
     */
    Mono<Map<StreamMessageId, StreamEntryStatus>> ack(StreamAckArgs args);

    /**
     * Releases pending messages back to the group without acknowledging them,
     * making them available for redelivery.
     *
     * Requires <b>Redis 8.8.0 and higher.</b>
     *
     * @param args - method arguments object
     * @return negatively acknowledged messages amount
     */
    Mono<Long> nack(StreamNackArgs args);

    /**
     * 返回common info about pending messages by group name.。
     * 
     * @param groupName 消费者组名称
     * @return result object
     */
    Mono<PendingResult> getPendingInfo(String groupName);

    /**
     * 返回list of pending messages by group name.。
     * Limited by start stream id and end stream id and count.
     * <p>
     * {@link StreamMessageId#MAX} is used as max stream id
     * {@link StreamMessageId#MIN} is used as min stream id
     * 
     * @param groupName 消费者组名称
     * @param startId - start stream id
     * @param endId - end stream id
     * @param count - amount of messages
     * @return list
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<List<PendingEntry>> listPending(String groupName, StreamMessageId startId, StreamMessageId endId, int count);
    
    /**
     * 返回list of pending messages by group name and consumer name.。
     * Limited by start stream id and end stream id and count.
     * <p>
     * {@link StreamMessageId#MAX} is used as max stream id
     * {@link StreamMessageId#MIN} is used as min stream id
     * 
     * @param consumerName 消费者名称
     * @param groupName 消费者组名称
     * @param startId - start stream id
     * @param endId - end stream id
     * @param count - amount of messages
     * @return list
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<List<PendingEntry>> listPending(String groupName, String consumerName, StreamMessageId startId, StreamMessageId endId, int count);

    /**
     * 返回list of common info about pending messages by group name.。
     * Limited by minimum idle time, messages count, start and end Stream Message IDs.
     * <p>
     * {@link StreamMessageId#MAX} is used as max Stream Message ID
     * {@link StreamMessageId#MIN} is used as min Stream Message ID
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @see #pendingRange
     *
     * @param groupName 消费者组名称
     * @param startId - start Stream Message ID
     * @param idleTime - minimum idle time of messages
     * @param idleTimeUnit - idle time unit
     * @param endId - end Stream Message ID
     * @param count - amount of messages
     * @return list
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<List<PendingEntry>> listPending(String groupName, StreamMessageId startId, StreamMessageId endId, long idleTime, TimeUnit idleTimeUnit, int count);

    /**
     * 返回list of common info about pending messages by group and consumer name.。
     * Limited by minimum idle time, messages count, start and end Stream Message IDs.
     * <p>
     * {@link StreamMessageId#MAX} is used as max Stream Message ID
     * {@link StreamMessageId#MIN} is used as min Stream Message ID
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @see #pendingRange
     *
     * @param consumerName 消费者名称
     * @param groupName 消费者组名称
     * @param startId - start Stream Message ID
     * @param endId - end Stream Message ID
     * @param idleTime - minimum idle time of messages
     * @param idleTimeUnit - idle time unit
     * @param count - amount of messages
     * @return list
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<List<PendingEntry>> listPending(String groupName, String consumerName, StreamMessageId startId, StreamMessageId endId, long idleTime, TimeUnit idleTimeUnit, int count);

    /**
     * 返回list of common info about pending messages by group and consumer name.。
     * Limited by start Stream Message ID and end Stream Message ID and count.
     *
     * @param args - method arguments object
     * @return list
     */
    @EmptyAsAbsent
    Mono<List<PendingEntry>> listPending(StreamPendingRangeArgs args);

    /**
     * 返回stream data of pending messages by group name.。
     * Limited by minimum idle time, messages count, start and end Stream Message IDs.
     * <p>
     * {@link StreamMessageId#MAX} is used as max Stream Message ID
     * {@link StreamMessageId#MIN} is used as min Stream Message ID
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @see #listPending
     *
     * @param groupName 消费者组名称
     * @param startId - start Stream Message ID
     * @param endId - end Stream Message ID
     * @param idleTime - minimum idle time of messages
     * @param idleTimeUnit - idle time unit
     * @param count - amount of messages
     * @return map
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> pendingRange(String groupName, StreamMessageId startId, StreamMessageId endId, long idleTime, TimeUnit idleTimeUnit, int count);

    /**
     * 返回stream data of pending messages by group and customer name.。
     * Limited by minimum idle time, messages count, start and end Stream Message IDs.
     * <p>
     * {@link StreamMessageId#MAX} is used as max Stream Message ID
     * {@link StreamMessageId#MIN} is used as min Stream Message ID
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @see #listPending
     *
     * @param consumerName 消费者名称
     * @param groupName 消费者组名称
     * @param startId - start Stream Message ID
     * @param endId - end Stream Message ID
     * @param idleTime - minimum idle time of messages
     * @param idleTimeUnit - idle time unit
     * @param count - amount of messages
     * @return map
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> pendingRange(String groupName, String consumerName, StreamMessageId startId, StreamMessageId endId, long idleTime, TimeUnit idleTimeUnit, int count);

    /**
     * Transfers ownership of pending messages by id to a new consumer 
     * by name if idle time of messages is greater than defined value. 
     * 
     * @param groupName - name of group
     * @param consumerName - name of consumer
     * @param idleTime - minimum idle time of messages
     * @param idleTimeUnit - idle time unit
     * @param ids - stream ids
     * @return stream data mapped by Stream ID
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> claim(String groupName, String consumerName, long idleTime, TimeUnit idleTimeUnit, StreamMessageId... ids);

    /**
     * Transfers ownership of pending messages by id to a new consumer
     * by name if idle time of messages and startId are greater than defined value.
     *
     * @param groupName - name of group
     * @param consumerName - name of consumer
     * @param idleTime - minimum idle time of messages
     * @param idleTimeUnit - idle time unit
     * @param startId - start Stream Message ID
     * @return stream data mapped by Stream ID
     */
    Mono<AutoClaimResult<K, V>> autoClaim(String groupName, String consumerName, long idleTime, TimeUnit idleTimeUnit, StreamMessageId startId, int count);

    /**
     * Transfers ownership of pending messages by id to a new consumer
     * by name if idle time of messages and startId are greater than defined value.
     *
     * @param groupName - name of group
     * @param consumerName - name of consumer
     * @param idleTime - minimum idle time of messages
     * @param idleTimeUnit - idle time unit
     * @param startId - start Stream Message ID
     * @return list of Stream Message IDs
     */
    Mono<FastAutoClaimResult> fastAutoClaim(String groupName, String consumerName, long idleTime, TimeUnit idleTimeUnit, StreamMessageId startId, int count);

    /**
     * 读取stream data from consumer group and multiple streams including current.。
     * <p>
     * Usage examples:
     * <pre>
     * Map result = stream.read("group1", "consumer1",  StreamMultiReadGroupArgs.greaterThan(id, "stream2", id2));
     * </pre>
     * <pre>
     * Map result = stream.read("group1", "consumer1", StreamMultiReadGroupArgs.greaterThan(id, "stream2", id2)
     *                                                                          .count(100)
     *                                                                          .timeout(Duration.ofSeconds(5))));
     * </pre>
     *
     * @param args - method arguments object
     * @return stream data mapped by stream name and Stream Message ID
     */
    @EmptyAsAbsent
    Mono<Map<String, Map<StreamMessageId, Map<K, V>>>> readGroup(String groupName, String consumerName, StreamMultiReadGroupArgs args);

    /**
     * 读取stream data from consumer group and current stream only.。
     * <p>
     * Usage examples:
     * <pre>
     * Map result = stream.read("group1", "consumer1",  StreamReadGroupArgs.greaterThan(id));
     * </pre>
     * <pre>
     * Map result = stream.read("group1", "consumer1", StreamReadGroupArgs.greaterThan(id)
     *                                                                          .count(100)
     *                                                                          .timeout(Duration.ofSeconds(5))));
     * </pre>
     *
     * @param args - method arguments object
     * @return stream data mapped by Stream Message ID
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> readGroup(String groupName, String consumerName, StreamReadGroupArgs args);

    /**
     * 返回number of entries in stream。
     * 
     * @return size of stream
     */
    Mono<Long> size();

    /**
     * Redis Stream 相关操作：追加流条目并返回生成的 Stream 消息 ID。。
     * <p>
     * Usage examples:
     * <pre>
     * StreamMessageId id = stream.add(StreamAddArgs.entry(15, 37));
     * </pre>
     * <pre>
     * StreamMessageId id = stream.add(StreamAddArgs.entries(15, 37, 23, 43)
     *                                 .trim(TrimStrategy.MAXLEN, 100)));
     * </pre>
     *
     * @param args - method arguments object
     * @return Stream Message ID
     */
    Mono<StreamMessageId> add(StreamAddArgs<K, V> args);

    /**
     * Redis Stream 相关操作：按指定 Stream 消息 ID 追加流条目。。
     * <p>
     * Usage examples:
     * <pre>
     * stream.add(id, StreamAddArgs.entry(15, 37));
     * </pre>
     * <pre>
     * stream.add(id, StreamAddArgs.entries(15, 37, 23, 43)
     *                                 .trim(TrimStrategy.MAXLEN, 100)));
     * </pre>
     *
     * @param id Stream 消息 ID
     * @param args - method arguments object
     */
    Mono<Void> add(StreamMessageId id, StreamAddArgs<K, V> args);

    /**
     * 读取stream data from multiple streams including current.。
     * <p>
     * Usage examples:
     * <pre>
     * Map result = stream.read(StreamMultiReadArgs.greaterThan(id, "stream2", id2));
     * </pre>
     * <pre>
     * Map result = stream.read(StreamMultiReadArgs.greaterThan(id, "stream2", id2)
     *                                 .count(100)
     *                                 .timeout(Duration.ofSeconds(5))));
     * </pre>
     *
     * @param args - method arguments object
     * @return stream data mapped by stream name and Stream Message ID
     */
    @EmptyAsAbsent
    Mono<Map<String, Map<StreamMessageId, Map<K, V>>>> read(StreamMultiReadArgs args);

    /**
     * 读取stream data from current stream only.。
     * <p>
     * Usage examples:
     * <pre>
     * Map result = stream.read(StreamReadArgs.greaterThan(id));
     * </pre>
     * <pre>
     * Map result = stream.read(StreamReadArgs.greaterThan(id)
     *                                 .count(100)
     *                                 .timeout(Duration.ofSeconds(5))));
     * </pre>
     *
     * @param args - method arguments object
     * @return stream data mapped by Stream Message ID
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> read(StreamReadArgs args);

    /**
     * 返回stream data in range by specified start Stream ID (included) and end Stream ID (included).。
     * 
     * @param startId - start Stream ID
     * @param endId - end Stream ID
     * @return stream data mapped by Stream ID
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> range(StreamMessageId startId, StreamMessageId endId);

    /**
     * 返回stream data in range by specified start Stream ID (included) and end Stream ID (included).。
     * 
     * @param count - stream data size limit
     * @param startId - start Stream ID
     * @param endId - end Stream ID
     * @return stream data mapped by Stream ID
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> range(int count, StreamMessageId startId, StreamMessageId endId);
    
    /**
     * 返回stream data in reverse order in range by specified start Stream ID (included) and end Stream ID (included).。
     * 
     * @param startId - start Stream ID
     * @param endId - end Stream ID
     * @return stream data mapped by Stream ID
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> rangeReversed(StreamMessageId startId, StreamMessageId endId);
    
    /**
     * 返回stream data in reverse order in range by specified start Stream ID (included) and end Stream ID (included).。
     * 
     * @param count - stream data size limit
     * @param startId - start Stream ID
     * @param endId - end Stream ID
     * @return stream data mapped by Stream ID
     */
    @Deprecated
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> rangeReversed(int count, StreamMessageId startId, StreamMessageId endId);

    /**
     * 返回stream data in range.。
     *
     * @param args - method arguments object
     * @return stream data mapped by Stream ID
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> range(StreamRangeArgs args);

    /**
     * 返回stream data in reverse order in range.。
     *
     * @param args - method arguments object
     * @return stream data mapped by Stream ID
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> rangeReversed(StreamRangeArgs args);

    /**
     * 移除messages by id.。
     * 
     * @param ids - id of messages to remove
     * @return deleted messages amount
     */
    Mono<Long> remove(StreamMessageId... ids);

    /**
     * 移除messages.。
     * Requires <b>Redis 8.2.0 and higher.</b>
     *
     * @param args - method arguments object
     * @return map with entry statuses mapped by id
     */
    Mono<Map<StreamMessageId, StreamEntryStatus>> remove(StreamRemoveArgs args);
    /**
     * Redis Stream 相关操作：Trims stream using strict trimming.。
     *
     * @param args - method arguments object
     * @return number of deleted messages
     */
    Mono<Long> trim(StreamTrimArgs args);

    /**
     * Redis Stream 相关操作：Trims stream using non-strict trimming.。
     *
     * @param args - method arguments object
     * @return number of deleted messages
     */
    Mono<Long> trimNonStrict(StreamTrimArgs args);

    /**
     * 返回information about this stream.。
     * 
     * @return info object
     */
    Mono<StreamInfo<K, V>> getInfo();
    
    /**
     * 返回list of objects with information about groups belonging to this stream.。
     * 
     * @return list of info objects 
     */
    @EmptyAsAbsent
    Mono<List<StreamGroup>> listGroups();

    /**
     * 返回list of objects with information about group customers for specified <code>groupName</code>.。
     * 
     * @param groupName 消费者组名称
     * @return list of info objects
     */
    @EmptyAsAbsent
    Mono<List<StreamConsumer>> listConsumers(String groupName);

    /**
     * 返回stream data of pending messages by group name.。
     * Limited by start Stream Message ID and end Stream Message ID and count.
     * <p>
     * {@link StreamMessageId#MAX} is used as max Stream Message ID
     * {@link StreamMessageId#MIN} is used as min Stream Message ID
     * 
     * @see #listPending
     * 
     * @param groupName 消费者组名称
     * @param startId - start Stream Message ID
     * @param endId - end Stream Message ID
     * @param count - amount of messages
     * @return map
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> pendingRange(String groupName, StreamMessageId startId, StreamMessageId endId, int count);
    
    /**
     * 返回stream data of pending messages by group and customer name.。
     * Limited by start Stream Message ID and end Stream Message ID and count.
     * <p>
     * {@link StreamMessageId#MAX} is used as max Stream Message ID
     * {@link StreamMessageId#MIN} is used as min Stream Message ID
     * 
     * @see #listPending
     * 
     * @param consumerName 消费者名称
     * @param groupName 消费者组名称
     * @param startId - start Stream Message ID
     * @param endId - end Stream Message ID
     * @param count - amount of messages
     * @return map
     */
    @EmptyAsAbsent
    Mono<Map<StreamMessageId, Map<K, V>>> pendingRange(String groupName, String consumerName, StreamMessageId startId, StreamMessageId endId, int count);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.StreamAddListener
     * @see org.redisson.api.listener.StreamRemoveListener
     * @see org.redisson.api.listener.StreamCreateGroupListener
     * @see org.redisson.api.listener.StreamRemoveGroupListener
     * @see org.redisson.api.listener.StreamCreateConsumerListener
     * @see org.redisson.api.listener.StreamRemoveConsumerListener
     * @see org.redisson.api.listener.StreamTrimListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    Mono<Integer> addListener(ObjectListener listener);

}
