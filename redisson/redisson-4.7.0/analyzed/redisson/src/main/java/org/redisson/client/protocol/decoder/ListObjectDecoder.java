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
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 按固定索引从列表回复中提取单个元素的解码器。
 * <p>
 * 第一个参数（{@code paramNum == 0}）强制用 {@link StringCodec} 解码，
 * 其余参数沿用默认编解码策略。
 *
 * @author Nikita Koksharov
 *
 */
public class ListObjectDecoder<T> implements MultiDecoder<T> {

    /** 要提取的元素在 parts 列表中的下标。 */
    private final int index;
    
    /** 指定目标元素索引。 */
    public ListObjectDecoder(int index) {
        super();
        this.index = index;
    }

    /** 首字段使用字符串解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum == 0) {
            return StringCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
    
    /** 空列表返回 null，否则返回 index 处元素。 */
    @Override
    public T decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return null;
        }
        return (T) parts.get(index);
    }

}
