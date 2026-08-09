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

import java.util.Objects;

/**
 * 不可变二元组，用于在 API 中成对传递两个值（如信号量锁与锁集合）。
 * 实现 {@link #equals} 与 {@link #hashCode} 便于作为 Map 键。
 *
 * @author Nikita Koksharov
 *
 * @param <T1> 第一个元素类型
 * @param <T2> 第二个元素类型
 */
public final class Tuple<T1, T2> {

    /** 第一个分量。 */
    private final T1 t1;
    /** 第二个分量。 */
    private final T2 t2;

    /** 构造二元组。 */
    public Tuple(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    /** 按两个分量值相等判定。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(t1, tuple.t1) && Objects.equals(t2, tuple.t2);
    }

    /** 基于 t1、t2 的复合哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }

    /** @return 第一个元素 */
    public T1 getT1() {
        return t1;
    }

    /** @return 第二个元素 */
    public T2 getT2() {
        return t2;
    }
}
