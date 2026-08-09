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

import io.reactivex.rxjava4.functions.Function;

/**
 * 对 List 原地排序的 {@link Function}，供 toSortedList 等算子使用。
 *
 * @param <T> 列表元素类型
 */
public final class SorterFunction<T> implements Function<List<T>, List<T>> {

    /** 排序比较器。 */
    final Comparator<? super T> comparator;

    /** @param comparator 用于 List.sort 的比较器 */
    public SorterFunction(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    /** 原地 sort 后返回同一 List 引用。 */
    @Override
    public List<T> apply(List<T> t) {
        t.sort(comparator);
        return t;
    }
}
