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

/**
 * Redisson 同步迭代器基类：元素即值本身，无需从 Map Entry 解包。
 * <p>
 * 继承 {@link BaseIterator}，{@link #getValue(Object)} 直接强转 SCAN 结果。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public abstract class RedissonBaseIterator<V> extends BaseIterator<V, Object> {

    /** 将 SCAN 批次中的原始对象直接作为迭代元素返回。 */
    @Override
    protected V getValue(Object entry) {
        return (V) entry;
    }
    
}
