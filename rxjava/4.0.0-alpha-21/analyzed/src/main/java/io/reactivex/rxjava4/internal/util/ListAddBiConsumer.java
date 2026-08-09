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

import java.util.List;

import io.reactivex.rxjava4.functions.BiFunction;

/**
 * 将元素追加到 {@link List} 并返回同一列表的 {@link BiFunction} 单例。
 */
@SuppressWarnings("rawtypes")
public enum ListAddBiConsumer implements BiFunction<List, Object, List> {
    INSTANCE;

    /** @return ListAddBiConsumer 单例 */
    @SuppressWarnings("unchecked")
    public static <T> BiFunction<List<T>, T, List<T>> instance() {
        return (BiFunction)INSTANCE;
    }

    /** 将 t2 追加到 t1 并返回 t1。 */
    @SuppressWarnings("unchecked")
    @Override
    public List apply(List t1, Object t2) {
        t1.add(t2);
        return t1;
    }
}
