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
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 有序集合回复中首个分值的回放解码器。
 * <p>
 * 适用于 {@code ZRANGEBYSCORE WITHSCORES} 等返回 [member, score, ...] 交替数组的场景，
 * 聚合阶段仅提取最后一个元素（即首个 member 对应的 score）。
 *
 * @author Nikita Koksharov
 *
 * 
 */
public class ObjectFirstScoreReplayDecoder implements MultiDecoder<Double> {

    /** 奇数索引为 score 字段，使用 {@link DoubleCodec} 解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum % 2 != 0) {
            return DoubleCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
    
    /** 空回复返回 null；否则取 parts 末尾元素作为首个 score。 */
    @Override
    public Double decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return null;
        }
        return (Double) parts.get(parts.size()-1);
    }

}
