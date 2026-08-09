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
package org.apache.rocketmq.broker.transaction.queue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事务 Op 消息批量上下文：维护待发送 Op 队列、累计条数与最近写入时间。
 * 供 {@link TransactionalOpBatchService} 批量刷写 Op 消息。
 */
public class MessageQueueOpContext {
    private AtomicInteger totalSize = new AtomicInteger(0);
    private volatile long lastWriteTimestamp;
    private LinkedBlockingQueue<String> contextQueue;

    /** @param timestamp 初始最后写入时间戳
     *  @param queueLength Op 上下文队列容量 */
    public MessageQueueOpContext(long timestamp, int queueLength) {
        this.lastWriteTimestamp = timestamp;
        contextQueue = new LinkedBlockingQueue<String>(queueLength);
    }

    /** 返回待批量发送的 Op 上下文队列。 */
    public LinkedBlockingQueue<String> getContextQueue() {
        return contextQueue;
    }


    /** 返回累计 Op 条数计数器。 */
    public AtomicInteger getTotalSize() {
        return totalSize;
    }


    /** 返回最近一次写入 Op 的时间戳。 */
    public long getLastWriteTimestamp() {
        return lastWriteTimestamp;
    }


    /** 更新最近一次写入 Op 的时间戳。 */
    public void setLastWriteTimestamp(long lastWriteTimestamp) {
        this.lastWriteTimestamp = lastWriteTimestamp;
    }
}
