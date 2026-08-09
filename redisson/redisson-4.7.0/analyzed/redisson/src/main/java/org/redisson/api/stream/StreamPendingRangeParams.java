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
package org.redisson.api.stream;

import java.time.Duration;
import java.util.Objects;

/**
 * {@link StreamPendingRangeArgs} 的默认实现，封装待处理消息范围查询参数。
 *
 * @author seakider
 *
 */
public class StreamPendingRangeParams implements StreamPendingRangeArgs,
        StreamStartIdArgs<StreamCountArgs>,
        StreamEndIdArgs<StreamCountArgs>, StreamCountArgs {
    /** 消费者组名称。 */
    private String groupName;
    /** 消费者名称，可选。 */
    private String consumerName;
    /** 范围起始消息 ID。 */
    private StreamMessageId startId;
    /** 范围结束消息 ID。 */
    private StreamMessageId endId;
    /** 起始 ID 是否排除边界。 */
    private boolean startIdExclusive;
    /** 结束 ID 是否排除边界。 */
    private boolean endIdExclusive;
    /** 返回条目数量上限。 */
    private int count;
    /** 最小空闲时长过滤条件。 */
    private Duration idleTime;

    StreamPendingRangeParams(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public StreamPendingRangeArgs consumerName(String consumerName) {
        this.consumerName = consumerName;
        return this;
    }

    @Override
    public StreamPendingRangeArgs idleTime(Duration idleTime) {
        this.idleTime = idleTime;
        return this;
    }

    @Override
    public StreamPendingRangeArgs count(int count) {
        this.count = count;
        return this;
    }

    @Override
    public StreamCountArgs endId(StreamMessageId endId) {
        this.endId = endId;
        return this;
    }

    @Override
    public StreamCountArgs endIdExclusive(StreamMessageId endId) {
        this.endId = endId;
        this.endIdExclusive = true;
        return this;
    }

    @Override
    public StreamEndIdArgs<StreamCountArgs> startId(StreamMessageId startId) {
        this.startId = startId;
        return this;
    }

    @Override
    public StreamEndIdArgs<StreamCountArgs> startIdExclusive(StreamMessageId startId) {
        this.startId = startId;
        this.startIdExclusive = true;
        return this;
    }

    /** 返回消费者组名称。 */
    public String getGroupName() {
        return groupName;
    }

    /** 返回范围起始消息 ID。 */
    public StreamMessageId getStartId() {
        return startId;
    }

    /** 返回范围结束消息 ID。 */
    public StreamMessageId getEndId() {
        return endId;
    }

    /** 起始 ID 是否为开区间（不包含边界）。 */
    public boolean isStartIdExclusive() {
        return startIdExclusive;
    }

    /** 结束 ID 是否为开区间（不包含边界）。 */
    public boolean isEndIdExclusive() {
        return endIdExclusive;
    }

    /** 返回条目数量上限。 */
    public int getCount() {
        return count;
    }

    /** 返回消费者名称，未设置时可能为 null。 */
    public String getConsumerName() {
        return consumerName;
    }

    /** 返回最小空闲时长过滤条件。 */
    public Duration getIdleTime() {
        return idleTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamPendingRangeParams that = (StreamPendingRangeParams) o;
        return startIdExclusive == that.startIdExclusive
                && endIdExclusive == that.endIdExclusive
                && count == that.count
                && Objects.equals(groupName, that.groupName)
                && Objects.equals(consumerName, that.consumerName)
                && Objects.equals(startId, that.startId)
                && Objects.equals(endId, that.endId)
                && Objects.equals(idleTime, that.idleTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, consumerName, startId, endId, startIdExclusive, endIdExclusive, count, idleTime);
    }
}
