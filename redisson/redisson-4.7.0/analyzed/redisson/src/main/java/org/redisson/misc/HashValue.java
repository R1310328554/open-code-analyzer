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
package org.redisson.misc;

import java.util.Arrays;
import java.util.Objects;

/**
 * 不可变的哈希值包装，内部持有 {@code long[]} 摘要。
 * <p>
 * 使用 {@link Arrays#deepEquals} 比较内容，
 * 适合作为 Map 键或缓存指纹标识。
 *
 * @author Nikita Koksharov
 *
 */
public final class HashValue {

    /** 空哈希常量，表示零长度摘要。 */
    public static final HashValue EMPTY = new HashValue(new long[0]);

    /** 哈希摘要数组（调用方不应修改）。 */
    private final long[] value;

    /** 用 long 数组构造哈希值对象。 */
    public HashValue(long[] hash) {
        this.value = hash;
    }
    
    /** 返回内部哈希数组副本的引用。 */
    public long[] getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashValue hashValue = (HashValue) o;
        return Objects.deepEquals(value, hashValue.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
