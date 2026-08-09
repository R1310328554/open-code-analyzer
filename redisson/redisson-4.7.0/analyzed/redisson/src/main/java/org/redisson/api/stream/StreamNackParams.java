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

/**
 * {@link StreamNackArgs} 的默认实现，封装否定确认参数。
 *
 * @author lamnt2008
 *
 */
public class StreamNackParams implements StreamNackArgs, StreamMessageIdArgs<StreamNackArgs> {

    /** 消费者组名称。 */
    private final String groupName;
    /** 否定确认模式。 */
    private final StreamNackMode mode;
    /** 待否定确认的消息 ID 数组。 */
    private StreamMessageId[] ids;
    /** 重试次数，未设置时为 null。 */
    private Long retryCount;
    /** 是否强制创建待处理条目。 */
    private boolean force;

    public StreamNackParams(String groupName, StreamNackMode mode) {
        this.groupName = groupName;
        this.mode = mode;
    }

    @Override
    public StreamNackArgs ids(StreamMessageId... ids) {
        this.ids = ids;
        return this;
    }

    @Override
    public StreamNackArgs retryCount(long count) {
        this.retryCount = count;
        return this;
    }

    @Override
    public StreamNackArgs force() {
        this.force = true;
        return this;
    }

    /** 返回消费者组名称。 */
    public String getGroupName() {
        return groupName;
    }

    /** 返回否定确认模式。 */
    public StreamNackMode getMode() {
        return mode;
    }

    /** 返回待否定确认的消息 ID 数组。 */
    public StreamMessageId[] getIds() {
        return ids;
    }

    /** 返回设置的重试次数，未设置时返回 null。 */
    public Long getRetryCount() {
        return retryCount;
    }

    /** 是否启用了强制创建待处理条目。 */
    public boolean isForce() {
        return force;
    }
}
