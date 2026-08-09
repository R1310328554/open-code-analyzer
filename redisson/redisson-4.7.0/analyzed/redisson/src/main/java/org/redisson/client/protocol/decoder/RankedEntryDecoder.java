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

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.RankedEntry;

import java.util.List;

/**
 * 带排名的有序集合条目解码器。
 * <p>
 * 解析 {@code [rank, score]} 二元组为 {@link RankedEntry}：
 * 偶数索引用 {@link LongCodec} 解码排名，奇数索引用 {@link DoubleCodec} 解码分值。
 *
 * @author Nikita Koksharov
 *
 */
public class RankedEntryDecoder implements MultiDecoder<RankedEntry<?>> {

    /** 奇数索引为 score（Double），偶数索引为 rank（Long）。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum % 2 != 0) {
            return DoubleCodec.INSTANCE.getValueDecoder();
        }
        return LongCodec.INSTANCE.getValueDecoder();
    }

    /** 将 [rank, score] 组装为 RankedEntry，空列表返回 null。 */
    @Override
    public RankedEntry<?> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return null;
        }
        return new RankedEntry<>(((Long) parts.get(0)).intValue(), (Double) parts.get(1));
    }

}
