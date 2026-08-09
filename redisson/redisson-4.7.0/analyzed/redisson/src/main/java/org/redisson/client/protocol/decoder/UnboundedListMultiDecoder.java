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
 * 无深度限制的嵌套数组 {@link MultiDecoder} 分发器。
 * <p>
 * 顶层数组委托 {@link #topLevel} 解码；所有更深层嵌套数组
 * 统一由 {@link #nested} 处理。与 {@link ListMultiDecoder2} 不同，
 * 嵌套解码器在每一层复用，调用方无需预先知道响应嵌套深度。
 * 若未指定嵌套解码器，默认使用 {@link ObjectListReplayDecoder}，
 * 将每个内层数组解码为 {@link List}。
 *
 * @param <T> top level decoded type parameter
 *
 * @author Nikita Koksharov
 *
 */
public class UnboundedListMultiDecoder<T> implements MultiDecoder<Object> {

    /** 响应最外层（{@link State#getLevel()}==0）使用的解码器。 */
    private final MultiDecoder<?> topLevel;
    /** 所有内层嵌套数组共用的解码器。 */
    private final MultiDecoder<?> nested;

    /** 嵌套层默认使用 {@link ObjectListReplayDecoder}。 */
    public UnboundedListMultiDecoder(MultiDecoder<?> topLevel) {
        this(topLevel, new ObjectListReplayDecoder<>());
    }

    /** @param topLevel 顶层数组解码器；@param nested 内层数组解码器 */
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

    /** 根据当前解析层级选择顶层或嵌套解码器。 */
    private MultiDecoder<?> active(State state) {
        if (state.getLevel() == 0) {
            return topLevel;
        }
        return nested;
    }

}
