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

import java.util.List;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 长整型占位解码器（第二段 Long 回复）。
 * <p>
 * 空列表时返回 {@code 0L}，非空时返回 {@code null}（由外层组合解码器处理实际值）。
 * 所有字段均通过 {@link LongCodec} 解码。
 *
 * @author Nikita Koksharov
 *
 */
public class Long2MultiDecoder implements MultiDecoder<Object> {

    /** 统一使用 Long 编解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return LongCodec.INSTANCE.getValueDecoder();
    }

    /** 空回复视为 0，否则交由上层处理。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return 0L;
        }
        return null;
    }
    
}
