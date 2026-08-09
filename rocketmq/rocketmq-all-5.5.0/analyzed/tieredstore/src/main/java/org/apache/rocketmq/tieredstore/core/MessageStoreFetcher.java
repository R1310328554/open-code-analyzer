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

package org.apache.rocketmq.tieredstore.core;

import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.store.GetMessageResult;
import org.apache.rocketmq.store.MessageFilter;
import org.apache.rocketmq.store.QueryMessageResult;

/**
 * 分层存储异步消息拉取接口：支持按时间/偏移查询与 key 检索。
 */
public interface MessageStoreFetcher {

    /**

     * 异步获取该队列最早消息的存储时间戳。
     *
     * @return 该 Store 中最早消息存储时间戳
     
     */
        /** 异步获取最早消息存储时间。 */
    CompletableFuture<Long> getEarliestMessageTimeAsync(String topic, int queueId);

    /**

     * 异步获取指定消费队列偏移对应消息的存储时间戳。
     *
     * @param topic 消息 Topic。
     * @param queueId 队列 ID。
     * @param consumeQueueOffset 消费队列逻辑偏移。
     * @return 消息存储时间戳。
     
     */
    CompletableFuture<Long> getMessageStoreTimeStampAsync(String topic, int queueId, long consumeQueueOffset);

    /**

     * 按存储时间戳查找匹配的消费队列偏移。
     *
     * @param topic 消息 Topic。
     * @param queueId 队列 ID。
     * @param timestamp 待查找的时间戳。
     * @return 匹配的消费队列偏移。
     
     */
    long getOffsetInQueueByTime(String topic, int queueId, long timestamp, BoundaryType type);

    /**

     * 异步拉取消息。
     *
     * @param group 发起查询的消费者组。
     * @param topic 待查询 Topic。
     * @param queueId 待查询队列 ID。
     * @param offset 起始逻辑偏移。
     * @param maxCount 最多拉取消息条数。
     * @param messageFilter 消息过滤器。
     * @return 匹配的消息结果。
     
     */
        /** 异步拉取消息。 */
    CompletableFuture<GetMessageResult> getMessageAsync(
        String group, String topic, int queueId, long offset, int maxCount, MessageFilter messageFilter);

    /**

     * 按 key 异步查询消息。
     *
     * @param topic 消息 Topic。
     * @param key 消息 Key。
     * @param maxCount 最多返回消息条数。
     * @param begin 起始时间戳。
     * @param end 结束时间戳。
     
     */
        /** 按 key 异步查询消息。 */
    CompletableFuture<QueryMessageResult> queryMessageAsync(
        String topic, String key, int maxCount, long begin, long end);
}
