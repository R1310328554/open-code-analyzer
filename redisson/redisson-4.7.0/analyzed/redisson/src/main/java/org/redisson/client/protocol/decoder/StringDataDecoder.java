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

import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * 原始字节缓冲 UTF-8 字符串解码器。
 * <p>
 * 直接将 {@link ByteBuf} 内容按 UTF-8 编码转为 {@link String}，
 * 不做 RESP 类型前缀解析，适用于已剥离协议头的裸字符串数据。
 *
 * @author Nikita Koksharov
 *
 */
public class StringDataDecoder implements Decoder<String> {

    /** 以 UTF-8 读取缓冲区全部可读字节并返回字符串。 */
    @Override
    public String decode(ByteBuf buf, State state) {
        return buf.toString(CharsetUtil.UTF_8);
    }

}
