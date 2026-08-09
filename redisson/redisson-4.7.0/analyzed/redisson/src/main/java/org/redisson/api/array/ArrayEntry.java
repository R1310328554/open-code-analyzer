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
package org.redisson.api.array;

import java.io.Serializable;
import java.util.Objects;

/**
 * Redis 数组（{@code RArray}）中的索引-值条目。
 *
 * @param <V> 元素值类型
 *
 * @author lamnt2008
 *
 */
public final class ArrayEntry<V> implements Serializable {

    private static final long serialVersionUID = -3681840466166419368L;

    private final long index;
    private final V value;

    public ArrayEntry(long index, V value) {
        this.index = index;
        this.value = value;
    }

    /**
     * 返回数组下标。
     *
     * @return 数组下标
     */
    public long getIndex() {
        return index;
    }

    /**
     * 返回该下标处存储的元素值。
     *
     * @return 元素值
     */
    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArrayEntry<?> that = (ArrayEntry<?>) o;
        return index == that.index && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, value);
    }

    @Override
    public String toString() {
        return "ArrayEntry{" +
                "index=" + index +
                ", value=" + value +
                '}';
    }

}
