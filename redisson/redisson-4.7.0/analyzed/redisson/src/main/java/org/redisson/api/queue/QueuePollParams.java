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
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * {@link QueuePollArgs} 的可变实现，持有拉取操作的各项参数及默认值。
 *
 * @author Nikita Koksharov
 *
 */
public final class QueuePollParams extends BaseSyncParams<QueuePollArgs> implements QueuePollArgs {

    private AcknowledgeMode acknowledgeMode = AcknowledgeMode.MANUAL;
    private Duration timeout;
    private Duration visibility = Duration.ofSeconds(0);
    private int count = 1;

    private Codec headersCodec;

    @Override
    public QueuePollArgs acknowledgeMode(AcknowledgeMode mode) {
        this.acknowledgeMode = mode;
        return this;
    }

    @Override
    public QueuePollArgs headersCodec(Codec codec) {
        this.headersCodec = codec;
        return this;
    }

    @Override
    public QueuePollArgs timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public QueuePollArgs visibility(Duration visibility) {
        this.visibility = visibility;
        return this;
    }

    @Override
    public QueuePollArgs count(int value) {
        this.count = value;
        return this;
    }

    /** 返回阻塞等待超时，未设置时为 {@code null}。 */
    public Duration getTimeout() {
        return timeout;
    }

    /** 返回可见性超时时长。 */
    public Duration getVisibility() {
        return visibility;
    }

    /** 返回单次拉取的最大条数。 */
    public int getCount() {
        return count;
    }

    /** 返回消息头编解码器。 */
    public Codec getHeadersCodec() {
        return headersCodec;
    }

    /** 返回确认模式。 */
    public AcknowledgeMode getAcknowledgeMode() {
        return acknowledgeMode;
    }
}
