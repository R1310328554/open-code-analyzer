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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.redisson.client.handler.State;

/**
 * 批量存在性探测结果解码器（集合形式）。
 * <p>
 * 与 {@link ContainsDecoder} 逻辑相同，但结果放入 {@link LinkedHashSet}
 * 以保持插入顺序并去重。
 *
 * @author Su Ko
 *
 */
public class ContainsSetDecoder<T> implements MultiDecoder<Set<T>> {

    /** 与 Redis 0/1 回复逐位对应的待检测元素。 */
    private final List<T> args;

    /** 保存待检测元素集合。 */
    public ContainsSetDecoder(Collection<T> args) {
        if (args instanceof List) {
            this.args = (List<T>) args;
        } else {
            this.args = new ArrayList<>(args);
        }
    }

    /** 将标记为 1 的参数加入 LinkedHashSet 并返回。 */
    @Override
    public Set<T> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return Collections.emptySet();
        }

        Set<T> result = new LinkedHashSet<>(parts.size());
        for (int index = 0; index < parts.size(); index++) {
            Long value = (Long) parts.get(index);
            if (value == 1) {
                result.add(args.get(index));
            }
        }

        return result;
    }

}
