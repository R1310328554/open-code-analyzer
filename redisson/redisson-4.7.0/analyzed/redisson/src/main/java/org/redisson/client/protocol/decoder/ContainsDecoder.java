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

import java.util.*;

/**
 * 批量存在性探测结果解码器（列表形式）。
 * <p>
 * 将 Redis 返回的 0/1 序列与构造时传入的参数列表按位对应，
 * 收集值为 {@code 1} 的元素组成 {@link List}。
 *
 * @author Nikita Koksharov
 *
 */
public class ContainsDecoder<T> implements MultiDecoder<List<T>> {

    /** 回复索引与参数索引的偏移（当前恒为 0）。 */
    private final int shiftIndex = 0;
    /** 与 Redis 回复逐位对应的待检测元素列表。 */
    private final List<T> args;

    /** 保存待检测元素；{@link List} 直接引用，否则复制为 ArrayList。 */
    public ContainsDecoder(Collection<T> args) {
        if (args instanceof List) {
            this.args = (List<T>) args;
        } else {
            this.args = new ArrayList<>(args);
        }
    }

    /** 空回复返回空列表；否则按 1 标记筛选对应参数。 */
    @Override
    public List<T> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(parts.size());
        for (int index = 0; index < parts.size()-shiftIndex; index++) {
            Long value = (Long) parts.get(index);
            // Redis EXISTS/SISMEMBER 等批量回复：1 表示存在
            if (value == 1) {
                result.add(args.get(index + shiftIndex));
            }
        }
        return result;
    }

}
