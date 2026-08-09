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
package org.redisson.spring.data.connection;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.decoder.MultiDecoder;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;

import java.util.List;

/**
 * 单条 member/score 对解码为 {@link Tuple}；空响应返回 {@code null}。
 * <p>奇数下标参数经 {@link DoubleCodec} 解析 score，适用于仅含一对元素的 ZSET 命令响应。
 *
 * @author Nikita Koksharov
 *
 */
public class ScoredSortedSingleReplayDecoder implements MultiDecoder<Tuple> {

    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        // 奇数下标为 score，使用 DoubleCodec。
        if (paramNum % 2 != 0) {
            return DoubleCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
    
    /** 空列表返回 null，否则从 member/score 构造 {@link DefaultTuple}。 */
    @Override
    public Tuple decode(List<Object> parts, State state) {
        // 无元素时返回 null（如 ZPOPMIN 空集合）。
        if (parts.isEmpty()) {
            return null;
        }
        return new DefaultTuple((byte[])parts.get(0), ((Number)parts.get(1)).doubleValue());
    }

}
