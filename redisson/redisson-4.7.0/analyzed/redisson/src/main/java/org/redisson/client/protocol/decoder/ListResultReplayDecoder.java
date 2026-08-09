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
import java.util.Map;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 列表形式的 Map 回放解码器。
 * <p>
 * 将 Redis 返回的 Map 对象数组转为不可变语义的 {@link List}，
 * 各元素均为 {@code Map<Object, Object>}。
 *
 * @author Nikita Koksharov
 *
 */
public class ListResultReplayDecoder implements MultiDecoder<List<Map<Object, Object>>> {

    /** 所有字段统一用字符串值解码器。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return StringCodec.INSTANCE.getValueDecoder();
    }
    
    /** 将 parts 中每个 Map 元素收集为 List。 */
    @Override
    @SuppressWarnings("unchecked")
    public List<Map<Object, Object>> decode(List<Object> parts, State state) {
        Map<Object, Object>[] res = parts.toArray(new Map[parts.size()]);
        return Arrays.asList(res);
    }

}
