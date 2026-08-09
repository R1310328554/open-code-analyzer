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
 * 简单字符串（Simple String）RESP 回放解码器。
 * <p>
 * 从 {@link ByteBuf} 读取以 {@code \r\n} 结尾的单行文本，
 * 常用于解析 {@code OK}、{@code PONG} 等状态回复。
 *
 * @author Nikita Koksharov
 *
 */
public class StringReplayDecoder implements Decoder<String> {

    /** 读取至 {@code \r} 前的字节并按 UTF-8 转为 {@link String}，随后跳过 CRLF。 */
    @Override
    public String decode(ByteBuf buf, State state) {
        String status = buf.readBytes(buf.bytesBefore((byte) '\r')).toString(CharsetUtil.UTF_8);
        buf.skipBytes(2);
        return status;
    }

}
