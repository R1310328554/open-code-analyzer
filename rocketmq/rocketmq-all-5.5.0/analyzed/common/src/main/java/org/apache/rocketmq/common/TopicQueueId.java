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
package org.apache.rocketmq.common;

import com.google.common.base.Objects;

/**
 * Topic 与队列 ID 的不可变组合键，用于 HashMap 索引与相等比较。
 * hashCode 在构造时预计算以提升热点路径性能。
 */
public class TopicQueueId {
    /** Topic 名称。 */
    private final String topic;
    /** 队列 ID。 */
    private final int queueId;

    /** 预计算的 hash(topic, queueId)。 */
    private final int hash;

    /** 构造 Topic+queueId 键并缓存 hashCode。 */
    public TopicQueueId(String topic, int queueId) {
        this.topic = topic;
        this.queueId = queueId;

        this.hash = Objects.hashCode(topic, queueId);
    }

    @Override
    /** 按 topic 与 queueId 相等比较。 */
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TopicQueueId broker = (TopicQueueId) o;
        return queueId == broker.queueId && Objects.equal(topic, broker.topic);
    }

    @Override
    /** 返回构造时缓存的 hash 值。 */
    public int hashCode() {
        return hash;
    }

    @Override
    /** 调试字符串（类名沿用 MessageQueueInBroker 历史命名）。 */
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageQueueInBroker{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", queueId=").append(queueId);
        sb.append('}');
        return sb.toString();
    }
}
