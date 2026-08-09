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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.decoder.MultiDecoder;

/**
 * Redis 集合型响应回放解码器：将元素列表转为 {@link Set}。
 * <p>使用 {@link LinkedHashSet} 保持插入顺序并去重；元素解码由注入的 {@link Decoder} 完成。
 *
 * @author Nikita Koksharov
 *
 */
public class SetReplayDecoder<T> implements MultiDecoder<Set<T>> {

    /** 集合元素解码器。 */
    private final Decoder<Object> decoder;
    
    /** 指定集合元素的 {@link Decoder}。 */
    public SetReplayDecoder(Decoder<Object> decoder) {
        super();
        this.decoder = decoder;
    }

    /** 返回构造时注入的元素解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return decoder;
    }
    
    /** 将已解码元素列表包装为 {@link LinkedHashSet}。 */
    @Override
    public Set<T> decode(List<Object> parts, State state) {
        return new LinkedHashSet(parts);
    }

}
