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

/**
 * 包装任意对象，{@link #equals} 与 {@link #hashCode} 均基于引用同一性（{@code ==}）。
 * <p>
 * 用于需要在集合中以对象身份而非 {@link Object#equals} 语义比较的场景。
 *
 * @author Nikita Koksharov
 *
 */
public final class IdentityValue<T> {

    /** 被包装的对象引用。 */
    private final T value;

    /** 包装给定对象。 */
    public IdentityValue(T value) {
        this.value = value;
    }

    /** 仅当同为 IdentityValue 且内部引用 {@code ==} 时相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityValue<?> that = (IdentityValue<?>) o;
        return value == that.value;
    }

    /** 使用 {@link System#identityHashCode} 计算哈希。 */
    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }
}
