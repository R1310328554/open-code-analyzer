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

package io.reactivex.rxjava4.internal.util;

import java.util.*;

import io.reactivex.rxjava4.functions.*;

/**
 * 提供空 {@link ArrayList} 的 {@link Supplier} 与 {@link Function} 单例。
 */
public enum ArrayListSupplier implements Supplier<List<Object>>, Function<Object, List<Object>> {
    INSTANCE;

    /** 返回创建空 {@link ArrayList} 的 {@link Supplier} 单例。 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Supplier<List<T>> asSupplier() {
        return (Supplier)INSTANCE;
    }

    /** 返回忽略输入并创建空 {@link ArrayList} 的 {@link Function} 单例。 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, O> Function<O, List<T>> asFunction() {
        return (Function)INSTANCE;
    }

    /** @return 新的空 {@link ArrayList} */
    @Override
    public List<Object> get() {
        return new ArrayList<>();
    }

    /** @return 新的空 {@link ArrayList}（忽略参数） */
    @Override public List<Object> apply(Object o) {
        return new ArrayList<>();
    }
}
