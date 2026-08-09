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

import java.util.Objects;

/**
 * {@link StreamRangeArgs} 的默认实现，封装流范围查询参数。
 *
 * @author seakider
 *
 */
public final class StreamRangeParams implements StreamRangeArgs, StreamEndIdArgs {
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

    StreamRangeParams(StreamMessageId startId, boolean startIdExclusive) {
        this.startId = startId;
        this.startIdExclusive = startIdExclusive;
    }

    @Override
    public StreamRangeArgs endId(StreamMessageId endId) {
        this.endId = endId;
        return this;
    }

    @Override
    public StreamRangeArgs endIdExclusive(StreamMessageId endId) {
        this.endId = endId;
        endIdExclusive = true;
        return this;
    }

    @Override
    public StreamRangeArgs count(int count) {
        this.count = count;
        return this;
    }

    /** 起始 ID 是否为开区间（不包含边界）。 */
    public boolean isStartIdExclusive() {
        return startIdExclusive;
    }

    /** 结束 ID 是否为开区间（不包含边界）。 */
    public boolean isEndIdExclusive() {
        return endIdExclusive;
    }

    /** 返回范围起始消息 ID。 */
    public StreamMessageId getStartId() {
        return startId;
    }

    /** 返回范围结束消息 ID。 */
    public StreamMessageId getEndId() {
        return endId;
    }

    /** 返回条目数量上限。 */
    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamRangeParams that = (StreamRangeParams) o;
        return startIdExclusive == that.startIdExclusive
                && endIdExclusive == that.endIdExclusive
                && count == that.count
                && Objects.equals(startId, that.startId)
                && Objects.equals(endId, that.endId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startId, endId, startIdExclusive, endIdExclusive, count);
    }
}
