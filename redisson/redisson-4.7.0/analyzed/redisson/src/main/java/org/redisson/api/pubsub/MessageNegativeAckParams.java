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
package org.redisson.api.pubsub;

import org.redisson.api.BaseSyncParams;

import java.time.Duration;
import java.util.Objects;

/**
 * {@link MessageNegativeAckArgs} 与 {@link FailedAckArgs} 的可变参数实现。
 *
 * @author Nikita Koksharov
 *
 */
public class MessageNegativeAckParams extends BaseSyncParams<MessageNegativeAckArgs> implements MessageNegativeAckArgs, FailedAckArgs {

    private final String[] ids;
    private Duration delay = Duration.ZERO;
    private boolean failed;

    /**
     * 构造负向确认参数。
     *
     * @param ids 待 nack 的消息 ID 数组
     * @param failed 为 {@code true} 表示处理失败（将重新投递），为 {@code false} 表示业务拒绝
     */
    public MessageNegativeAckParams(String[] ids, boolean failed) {
        this.ids = ids;
        this.failed = failed;
    }

    /**
     * 设置处理失败消息的重新投递延迟。
     *
     * @param value 延迟时长，不可为 {@code null}
     * @return 参数对象
     */
    @Override
    public MessageNegativeAckArgs delay(Duration value) {
        Objects.requireNonNull(value);
        this.delay = value;
        return this;
    }

    public Duration getDelay() {
        return delay;
    }

    public String[] getIds() {
        return ids;
    }

    public boolean isFailed() {
        return failed;
    }
}
