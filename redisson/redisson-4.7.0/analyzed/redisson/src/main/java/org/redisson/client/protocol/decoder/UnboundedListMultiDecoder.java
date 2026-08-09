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
 * A {@link MultiDecoder} that dispatches the top-level array to one decoder
 * and treats every nested array uniformly with a second decoder. Unlike
 * {@link ListMultiDecoder2}, the nested decoder is reused for every level
 * deeper than the top level, so this decoder handles responses of arbitrary
 * nesting depth without the caller needing to know the depth in advance.
 * <p>
 * If no nested decoder is supplied, an {@link ObjectListReplayDecoder} is
 * used, which produces a {@link List} for each nested array.
 *
 * @param <T> top level decoded type parameter
 *
 * @author Nikita Koksharov
 *
 */
public class UnboundedListMultiDecoder<T> implements MultiDecoder<Object> {

    private final MultiDecoder<?> topLevel;
    private final MultiDecoder<?> nested;

    public UnboundedListMultiDecoder(MultiDecoder<?> topLevel) {
        this(topLevel, new ObjectListReplayDecoder<>());
    }

    public UnboundedListMultiDecoder(MultiDecoder<?> topLevel, MultiDecoder<?> nested) {
        this.topLevel = topLevel;
        this.nested = nested;
    }

    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size, List<Object> parts) {
        return active(state).getDecoder(codec, paramNum, state, size, parts);
    }

    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return active(state).getDecoder(codec, paramNum, state, size);
    }

    @Override
    public Object decode(List<Object> parts, State state) {
        return active(state).decode(parts, state);
    }

    private MultiDecoder<?> active(State state) {
        if (state.getLevel() == 0) {
            return topLevel;
        }
        return nested;
    }

}
