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
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 阻塞弹出有序集合（BZPOPMIN/BZPOPMAX 等）的单条结果解码器。
 * <p>
 * 响应形如 {@code [key, member, score]} 三元组；
 * 本解码器仅返回 member（索引 1），空响应返回 null。
 * paramNum 0 为 key（String）、2 为 score（Double），member 走默认 codec。
 *
 * @author Nikita Koksharov
 *
 */
public class ScoredSortedSetPolledObjectDecoder implements MultiDecoder<Object> {

    /** 非空时取 parts[1] 作为弹出的 member 对象。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (!parts.isEmpty()) {
            return parts.get(1);
        }
        return null;
    }

    /** key 用 StringCodec，score 用 DoubleCodec，member 用默认解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum == 0) {
            return StringCodec.INSTANCE.getValueDecoder();
        }
        if (paramNum == 2) {
            return DoubleCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }

}
