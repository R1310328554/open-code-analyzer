/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.internal.functions;

import io.reactivex.rxjava4.functions.BiPredicate;
import java.util.Objects;

/**
 * 包含 Java 7 {@code Objects} 工具类 backport 的工具方法。
 * <p>命名如此以避免与 {@code java.util.Objects} 冲突。
 */
public final class ObjectHelper {

    /** 工具类。 */
    private ObjectHelper() {
        throw new IllegalStateException("No instances!");
    }

    static final BiPredicate<Object, Object> EQUALS = new BiObjectPredicate();

    /**
     * 返回通过 {@code Objects.equals()} 比较参数的 {@link BiPredicate}。
     * @param <T> 值类型
     * @return bi-predicate 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> BiPredicate<T, T> equalsPredicate() {
        return (BiPredicate<T, T>)EQUALS;
    }

    /**
     * 验证给定值为正数，否则抛出带参数名的 {@link IllegalArgumentException}。
     * @param value 待验证的值
     * @param paramName 值的参数名
     * @return value
     * @throws IllegalArgumentException 若 bufferSize &lt;= 0
     */
    public static int verifyPositive(int value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " > 0 required but it was " + value);
        }
        return value;
    }

    /**
     * 验证给定值为正数，否则抛出带参数名的 {@link IllegalArgumentException}。
     * @param value 待验证的值
     * @param paramName 值的参数名
     * @return value
     * @throws IllegalArgumentException 若 bufferSize &lt;= 0
     */
    public static long verifyPositive(long value, String paramName) {
        if (value <= 0L) {
            throw new IllegalArgumentException(paramName + " > 0 required but it was " + value);
        }
        return value;
    }

    static final class BiObjectPredicate implements BiPredicate<Object, Object> {
        @Override
        public boolean test(Object o1, Object o2) {
            return Objects.equals(o1, o2);
        }
    }
}
