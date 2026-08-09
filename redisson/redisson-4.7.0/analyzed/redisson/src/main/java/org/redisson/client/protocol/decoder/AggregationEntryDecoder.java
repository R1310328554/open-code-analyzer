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
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 聚合查询单行字段解码器，继承 {@link ObjectMapReplayDecoder}。
 * <p>
 * 根据 {@code reducers} 数量，对尾部归约器字段强制使用字符串解码；
 * 其余键值对按用户 {@link Codec} 解码值、字符串解码键。
 *
 * @author Nikita Koksharov
 *
 */
public class AggregationEntryDecoder extends ObjectMapReplayDecoder {

    private final Codec codec;
    /** 尾部归约器字段占用的参数个数（构造时 {@code reducers * 2}）。 */
    private final int reducers;

    /**
     * @param codec 聚合行属性值的编解码器
     * @param reducers 归约器数量
     */
    public AggregationEntryDecoder(Codec codec, int reducers) {
        this.codec = codec;
        this.reducers = reducers*2;
    }

    /** 按参数序号选择键/值/归约器字段对应的解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (reducers > 0
                && paramNum >= size - reducers) {
            return StringCodec.INSTANCE.getValueDecoder();
        }

        if (paramNum % 2 != 0) {
            return this.codec.getMapValueDecoder();
        }
        return StringCodec.INSTANCE.getValueDecoder();
    }

}
