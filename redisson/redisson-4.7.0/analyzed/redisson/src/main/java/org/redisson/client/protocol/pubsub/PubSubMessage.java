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
 * Redis 频道订阅（{@code SUBSCRIBE}）推送的消息载体。
 * <p>
 * 封装频道名与已解码的消息体，实现 {@link Message} 供上层监听器消费。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubMessage implements Message {

    /** 消息来源频道。 */
    private final ChannelName channel;
    /** 经 Codec 解码后的消息内容。 */
    private final Object value;

    /** @param channel 频道名 @param value 消息体 */
    public PubSubMessage(ChannelName channel, Object value) {
        super();
        this.channel = channel;
        this.value = value;
    }

    /** 返回消息所属频道。 */
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
        return "Message [channel=" + channel + ", value=" + value + "]";
    }

}
