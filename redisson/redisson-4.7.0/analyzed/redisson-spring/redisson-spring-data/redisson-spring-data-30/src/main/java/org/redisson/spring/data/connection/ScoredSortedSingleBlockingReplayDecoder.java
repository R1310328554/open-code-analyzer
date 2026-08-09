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
import org.springframework.data.redis.connection.RedisZSetCommands;

import java.util.List;

/**
 * 单条阻塞有序集合弹出解码器：将 BZPOPMIN/BZPOPMAX 等单 key 响应
 * （key、member、score 三元素）解析为一条 {@link Tuple}。
 * <p>{@code paramNum == 2} 时以 {@link DoubleCodec} 解析 score。
 *
 * @author Nikita Koksharov
 *
 */
public class ScoredSortedSingleBlockingReplayDecoder implements MultiDecoder<Tuple> {

    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        // 响应第三项为 score，使用 DoubleCodec。
        if (paramNum == 2) {
            return DoubleCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
    
    /** 从 parts[1] member 与 parts[2] score 构造 {@link DefaultTuple}（parts[0] 为 key）。 */
    @Override
    public Tuple decode(List<Object> parts, State state) {
        return new DefaultTuple((byte[])parts.get(1), ((Number)parts.get(2)).doubleValue());
    }

}
