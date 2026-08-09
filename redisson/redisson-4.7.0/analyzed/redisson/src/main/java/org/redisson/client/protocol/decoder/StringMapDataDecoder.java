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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import io.netty.util.CharsetUtil;

/**
 * 纯文本键值对 Map 解码器（非 RESP Map 结构）。
 * <p>
 * 将整个 {@link ByteBuf} 按 UTF-8 读成多行文本，每行 {@code key:value}
 * 解析为 {@link Map}{@code <String, String>}。适用于 INFO 类
 * 行式键值响应，而非标准 RESP 嵌套数组。
 *
 * @author Nikita Koksharov
 *
 */
public class StringMapDataDecoder implements MultiDecoder<Map<String, String>> {

    /** 按行分割并解析 {@code key:value} 对，写入 {@link HashMap}。 */
    private final Decoder decoder = (buf, state) -> {
        String value = buf.toString(CharsetUtil.UTF_8);
        Map<String, String> result = new HashMap<String, String>();
        for (String entry : value.split("\r\n|\n")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    };

    /** 返回上述行式文本解码器；实际 Map 在元素级 {@link Decoder} 中完成。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return decoder;
    }

    /** 本解码器不在批量阶段组装 Map，故返回 {@code null}。 */
    @Override
    public Map<String, String> decode(List<Object> parts, State state) {
        return null;
    }
}
