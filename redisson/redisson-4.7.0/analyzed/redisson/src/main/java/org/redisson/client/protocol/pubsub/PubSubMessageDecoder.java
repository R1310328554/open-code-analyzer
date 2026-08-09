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

import java.util.List;

import org.redisson.client.ChannelName;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.decoder.MultiDecoder;

/**
 * {@code message} 类型 Pub/Sub 推送的多段 RESP 解码器。
 * <p>
 * 将 {@code [type, channel, payload]} 三段数组解析为 {@link PubSubMessage}；
 * 消息体字段使用构造时注入的 {@link Decoder} 解码。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubMessageDecoder implements MultiDecoder<Object> {

    /** 用于解码消息 payload 的解码器。 */
    private final Decoder<Object> decoder;

    /** @param decoder 消息体解码器 */
    public PubSubMessageDecoder(Decoder<Object> decoder) {
        super();
        this.decoder = decoder;
    }

    /** 始终返回构造时指定的 payload 解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return decoder;
    }
    
    /** 从多段数组提取频道名与消息体，组装 {@link PubSubMessage}。 */
    @Override
    public PubSubMessage decode(List<Object> parts, State state) {
        ChannelName name = new ChannelName((byte[]) parts.get(1));
        return new PubSubMessage(name, parts.get(2));
    }

}
