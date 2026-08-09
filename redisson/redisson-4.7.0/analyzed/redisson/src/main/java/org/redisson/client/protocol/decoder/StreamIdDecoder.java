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
package org.redisson.client.protocol.decoder;

import java.io.IOException;

import org.redisson.api.stream.StreamMessageId;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import io.netty.buffer.ByteBuf;

/**
 * Stream 消息 ID 字节级解码器。
 * <p>
 * 从 RESP 缓冲区读取 {@code "毫秒-序号"} 格式字符串，
 * 解析为 {@link StreamMessageId}；无序号部分时返回 {@code null}。
 *
 * @author Nikita Koksharov
 *
 */
public class StreamIdDecoder implements Decoder<Object> {

    /** 读取字符串并按 {@code -} 分割，构造 {@link StreamMessageId}。 */
    @Override
    public Object decode(ByteBuf buf, State state) throws IOException {
        String id = (String) StringCodec.INSTANCE.getValueDecoder().decode(buf, state);
        String[] parts = id.toString().split("-");
        if (parts.length == 1) {
            return null;
        }
        return new StreamMessageId(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }

}
