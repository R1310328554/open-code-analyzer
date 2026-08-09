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
package org.apache.rocketmq.tieredstore.metadata.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 队列元数据：记录 MessageQueue 在分层存储中的最小/最大偏移及更新时间。
 */
public class QueueMetadata {

    /** 关联的 MessageQueue（Topic、Broker、QueueId）。 */
    @JSONField(ordinal = 1)
    private MessageQueue queue;

    /** 分层存储中该队列的最小逻辑偏移。 */
    @JSONField(ordinal = 2)
    private long minOffset;

    /** 分层存储中该队列的最大逻辑偏移。 */
    @JSONField(ordinal = 3)
    private long maxOffset;

    /** 元数据最后更新时间戳。 */
    @JSONField(ordinal = 4)
    private long updateTimestamp;

    // fastjson 反序列化使用的默认构造器
    @SuppressWarnings("unused")
    public QueueMetadata() {
    }

    /** 构造队列元数据并设置当前更新时间。 */
    public QueueMetadata(MessageQueue queue, long minOffset, long maxOffset) {
        this.queue = queue;
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.updateTimestamp = System.currentTimeMillis();
    }

    public MessageQueue getQueue() {
        return queue;
    }

    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }

    public long getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
}
