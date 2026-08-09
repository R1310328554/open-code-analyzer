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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * Stream 条目字段 Map 回放解码器。
 * <p>
 * 继承 {@link ObjectMapReplayDecoder}，针对 Stream 响应中
 * 已解码为 Map 或空列表的特殊形状做分支处理。
 *
 * @author Nikita Koksharov
 *
 */
public class StreamObjectMapReplayDecoder extends ObjectMapReplayDecoder<Object, Object> {

    /** 可选的自定义字段解码器，非空时优先使用。 */
    private Decoder<Object> codec;
    
    /** 使用默认父类解码逻辑。 */
    public StreamObjectMapReplayDecoder() {
    }
    
    /** 指定 Stream 字段值的自定义解码器。 */
    public StreamObjectMapReplayDecoder(Decoder<Object> codec) {
        super();
        this.codec = codec;
    }

    /**
     * 解码 Stream 条目字段映射。
     * <p>
     * 空/null/空列表时返回空 Map；首元素已是 Map 时直接合并。
     */
    @Override
    public Map<Object, Object> decode(List<Object> parts, State state) {
        if (parts.isEmpty()
                || parts.get(0) == null
                    || (parts.get(0) instanceof List && ((List) parts.get(0)).isEmpty())) {
            parts.clear();
            return Collections.emptyMap();
        }

        if (parts.get(0) instanceof Map) {
            Map<Object, Object> result = new LinkedHashMap<Object, Object>(parts.size());
            for (int i = 0; i < parts.size(); i++) {
                result.putAll((Map<? extends Object, ? extends Object>) parts.get(i));
            }
            return result;
        }
        return super.decode(parts, state);
    }

    /** 有注入解码器时返回之，否则委托父类按 Codec 选择。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (this.codec != null) {
            return this.codec;
        }
        return super.getDecoder(codec, paramNum, state, size);
    }

}
