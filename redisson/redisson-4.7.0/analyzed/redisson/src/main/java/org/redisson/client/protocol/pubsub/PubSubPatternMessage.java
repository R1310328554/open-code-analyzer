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
package org.redisson.client.protocol.pubsub;

import org.redisson.client.ChannelName;

/**
 * 模式订阅（{@code PSUBSCRIBE}）推送的消息载体。
 * <p>
 * 除实际频道与消息体外，还携带用户订阅时使用的模式字符串，
 * 便于在通配符订阅场景下区分匹配来源。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubPatternMessage implements Message {

    /** 用户订阅的模式（如 {@code news.*}）。 */
    private final ChannelName pattern;
    /** 实际触发消息的频道名。 */
    private final ChannelName channel;
    /** 经 Codec 解码后的消息内容。 */
    private final Object value;

    /** @param pattern 订阅模式 @param channel 实际频道 @param value 消息体 */
    public PubSubPatternMessage(ChannelName pattern, ChannelName channel, Object value) {
        super();
        this.pattern = pattern;
        this.channel = channel;
        this.value = value;
    }

    /** 返回订阅时使用的模式。 */
    public ChannelName getPattern() {
        return pattern;
    }

    /** 返回实际发布消息的频道。 */
    @Override
    public ChannelName getChannel() {
        return channel;
    }

    /** 返回已解码的消息值。 */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PubSubPatternMessage [pattern=" + pattern + ", channel=" + channel + ", value=" + value + "]";
    }

}
