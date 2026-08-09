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
package org.redisson.api.fanout;

import org.redisson.api.MessageArgs;
import org.redisson.api.BaseSyncParams;
import org.redisson.client.codec.Codec;

/**
 * {@link FanoutPublishArgs} 的默认实现，持有待发布消息及可选的消息头编解码器。
 *
 * @param <V> 消息体类型
 * @author Nikita Koksharov
 */
public final class FanoutPublishParams<V> extends BaseSyncParams<FanoutPublishArgs<V>> implements FanoutPublishArgs<V> {

    /** 待发布的消息参数数组。 */
    private final MessageArgs<V>[] msgs;
    /** 消息头编解码器，可为 null 表示使用默认编解码。 */
    private Codec headersCodec;

    /** 以给定消息列表创建发布参数。 */
    public FanoutPublishParams(MessageArgs<V>[] msgs) {
        this.msgs = msgs;
    }

    @Override
    public FanoutPublishArgs<V> headersCodec(Codec codec) {
        this.headersCodec = codec;
        return this;
    }

    /** 返回待发布的消息参数数组。 */
    public MessageArgs<V>[] getMsgs() {
        return msgs;
    }

    /** 返回消息头编解码器。 */
    public Codec getHeadersCodec() {
        return headersCodec;
    }

}

