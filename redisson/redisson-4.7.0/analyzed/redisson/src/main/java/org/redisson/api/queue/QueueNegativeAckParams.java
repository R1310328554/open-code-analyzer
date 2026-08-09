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
package org.redisson.api.queue;

import org.redisson.api.BaseSyncParams;

import java.time.Duration;
import java.util.Objects;

/**
 * {@link QueueNegativeAckArgs} 的可变实现，保存消息 ID、失败标志与重投延迟。
 *
 * @author Nikita Koksharov
 *
 */
public class QueueNegativeAckParams extends BaseSyncParams<QueueNegativeAckArgs> implements QueueNegativeAckArgs, FailedAckArgs {

    private final String[] ids;
    private Duration delay = Duration.ZERO;
    private boolean failed;

    /** 构造负确认参数。{@code failed} 为 {@code true} 表示处理失败需重投。 */
    public QueueNegativeAckParams(String[] ids, boolean failed) {
        this.ids = ids;
        this.failed = failed;
    }

    @Override
    public QueueNegativeAckArgs delay(Duration value) {
        Objects.requireNonNull(value);
        this.delay = value;
        return this;
    }

    /** 返回重新投递前的延迟时长。 */
    public Duration getDelay() {
        return delay;
    }

    /** 返回待负确认的消息 ID 列表。 */
    public String[] getIds() {
        return ids;
    }

    /** 返回是否为处理失败（需重投）状态。 */
    public boolean isFailed() {
        return failed;
    }
}
