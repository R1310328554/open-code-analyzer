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

import java.util.Arrays;
import java.util.List;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 字符串列表批量解码器。
 * <p>
 * 外层 {@link MultiDecoder} 已将 RESP 数组元素逐条解码；
 * 本类在 {@link #decode} 阶段将 {@link List}{@code <Object>} 统一转为
 * {@link List}{@code <String>}，元素解码委托 {@link StringCodec}。
 *
 * @author Nikita Koksharov
 *
 */
public class StringListReplayDecoder implements MultiDecoder<List<String>> {

    /** 每个数组元素均使用 {@link StringCodec} 的值解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return StringCodec.INSTANCE.getValueDecoder();
    }
    
    /** 将已解码对象列表拷贝为不可变语义的 {@link String} 数组再包装为 List。 */
    @Override
    public List<String> decode(List<Object> parts, State state) {
        return Arrays.asList(Arrays.copyOf(parts.toArray(), parts.size(), String[].class));
    }

}
