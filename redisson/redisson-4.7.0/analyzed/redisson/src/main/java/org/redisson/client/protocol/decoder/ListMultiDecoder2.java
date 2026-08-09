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
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 按嵌套层级分派的复合列表解码器。
 * <p>
 * 根据 {@link State#getLevel()} 从解码器数组中选取当前层对应的
 * {@link MultiDecoder}，用于多层嵌套数组的逐层解析。
 *
 * @author Nikita Koksharov
 *
 * @param <T> type
 */
public class ListMultiDecoder2<T> implements MultiDecoder<Object> {

    /** 各嵌套层级对应的解码器，索引与 state.level 一致。 */
    private final MultiDecoder<?>[] decoders;
    
    /** 按声明顺序注册各层解码器。 */
    public ListMultiDecoder2(MultiDecoder<?>... decoders) {
        this.decoders = decoders;
    }

    /** 按当前层级委托子解码器选择字段解码器（含 parts 上下文）。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size, List<Object> parts) {
        int index = state.getLevel();
        return decoders[index].getDecoder(codec, paramNum, state, size, parts);
    }

    /** 按当前层级委托子解码器选择字段解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        int index = state.getLevel();
        return decoders[index].getDecoder(codec, paramNum, state, size);
    }
    
    /** 按当前层级调用对应解码器的 decode。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        int index = state.getLevel();
        return decoders[index].decode(parts, state);
    }
    
}
