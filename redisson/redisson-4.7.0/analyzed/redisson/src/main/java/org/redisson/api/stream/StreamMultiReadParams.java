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
import java.util.Map;

/**
 * {@link StreamMultiReadArgs} 的默认实现，封装多流读取参数。
 *
 * @author Nikita Koksharov
 *
 */
public final class StreamMultiReadParams implements StreamMultiReadArgs {

    /** 单流读取参数。 */
    private final StreamReadParams params;

    /** 各附加流的上次读取消息 ID 映射。 */
    private final Map<String, StreamMessageId> offsets;

    StreamMultiReadParams(StreamMessageId id1, Map<String, StreamMessageId> offsets) {
        this.params = new StreamReadParams(id1);
        this.offsets = offsets;
    }

    @Override
    public StreamMultiReadArgs count(int count) {
        params.count(count);
        return this;
    }

    @Override
    public StreamMultiReadArgs maxCount(int maxCount) {
        params.maxCount(maxCount);
        return this;
    }

    @Override
    public StreamMultiReadArgs maxSize(long maxSize) {
        params.maxSize(maxSize);
        return this;
    }

    @Override
    public StreamMultiReadArgs timeout(Duration timeout) {
        params.timeout(timeout);
        return this;
    }

    /** 返回当前流的上次读取消息 ID。 */
    public StreamMessageId getId1() {
        return params.getId1();
    }

    /** 返回单流数据条数上限。 */
    public int getCount() {
        return params.getCount();
    }

    /** 返回条目总数上限。 */
    public int getMaxCount() {
        return params.getMaxCount();
    }

    /** 返回条目总字节数上限。 */
    public long getMaxSize() {
        return params.getMaxSize();
    }

    /** 返回等待超时时间。 */
    public Duration getTimeout() {
        return params.getTimeout();
    }

    /** 返回各附加流的上次读取消息 ID 映射。 */
    public Map<String, StreamMessageId> getOffsets() {
        return offsets;
    }
}
