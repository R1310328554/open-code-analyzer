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

/**
 * {@link StreamReadArgs} 的默认实现，封装单流读取参数。
 *
 * @author Nikita Koksharov
 *
 */
public final class StreamReadParams implements StreamReadArgs {

    /** 上次读取的消息 ID。 */
    private final StreamMessageId id1;
    /** 返回数据条数上限。 */
    private int count;
    /** 条目总数上限。 */
    private int maxCount;
    /** 条目总字节数上限。 */
    private long maxSize;
    /** 等待超时时间。 */
    private Duration timeout;

    StreamReadParams(StreamMessageId id1) {
        this.id1 = id1;
    }

    @Override
    public StreamReadArgs count(int count) {
        this.count = count;
        return this;
    }

    @Override
    public StreamReadArgs maxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    @Override
    public StreamReadArgs maxSize(long maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    @Override
    public StreamReadArgs timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /** 返回上次读取的消息 ID。 */
    public StreamMessageId getId1() {
        return id1;
    }

    /** 返回数据条数上限。 */
    public int getCount() {
        return count;
    }

    /** 返回条目总数上限。 */
    public int getMaxCount() {
        return maxCount;
    }

    /** 返回条目总字节数上限。 */
    public long getMaxSize() {
        return maxSize;
    }

    /** 返回等待超时时间。 */
    public Duration getTimeout() {
        return timeout;
    }
}
