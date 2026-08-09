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
package org.redisson.iterator;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 基于 Redis HSCAN 的 Map 迭代器抽象基类。
 * <p>
 * 将 SCAN 得到的 {@link java.util.Map.Entry} 包装为可写 {@link java.util.Map.Entry}，
 * 调用 {@link Entry#setValue(Object)} 时委托子类 {@link #put} 写回 Redis。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public abstract class RedissonBaseMapIterator<V> extends BaseIterator<V, Entry<Object, Object>> {

    /** 构造可写 Entry 代理，setValue 时调用 {@link #put} 更新 Redis。 */
    @SuppressWarnings("unchecked")
    protected V getValue(Map.Entry<Object, Object> entry) {
        return (V) new AbstractMap.SimpleEntry(entry.getKey(), entry.getValue()) {

            @Override
            public Object setValue(Object value) {
                return put(entry, value);
            }

        };
    }

    /** 子类实现：将 entry 的 value 写回底层 Map 结构。 */
    protected abstract Object put(Entry<Object, Object> entry, Object value);

}
