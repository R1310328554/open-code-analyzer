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

import org.redisson.api.GcraResult;
import org.redisson.client.handler.State;

import java.util.List;

/**
 * GCRA 通用限流算法（{@code CL.THROTTLE}）回复解码器。
 * <p>
 * 期望固定 5 个数值字段：是否允许、剩余配额、重试间隔、
 * 总限制与已用计数，组装为 {@link GcraResult}。
 *
 * @author Su Ko
 *
 */
public class GcraResultDecoder implements MultiDecoder<GcraResult> {

    /** 校验长度为 5，否则抛出 {@link IllegalStateException}。 */
    @Override
    public GcraResult decode(List<Object> parts, State state) {
        if (parts == null || parts.size() != 5) {
            throw new IllegalStateException("Unexpected GCRA response: " + parts);
        }

        return new GcraResult(toLong(parts.get(0)) == 1,
                toLong(parts.get(1)),
                toLong(parts.get(2)),
                toLong(parts.get(3)),
                toLong(parts.get(4)));
    }

    /** 将 Number 或字符串安全转为 long。 */
    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

}
