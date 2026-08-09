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

import org.redisson.client.handler.State;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 集合型 RESP 数组回放解码器。
 * <p>
 * 将已逐段解码的 parts 直接包装为 {@link LinkedHashSet}，
 * 保留 Redis 返回元素的顺序并自动去重。
 *
 * @author Nikita Koksharov
 *
 * @param <T> value type
 */
public class ObjectSetReplayDecoder<T> implements MultiDecoder<Set<T>> {

    /** 用 parts 构造 LinkedHashSet，维持服务端顺序。 */
    @Override
    public Set<T> decode(List<Object> parts, State state) {
        return new LinkedHashSet(parts);
    }

}
