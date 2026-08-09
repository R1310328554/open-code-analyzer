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

package io.reactivex.rxjava4.operators;

import io.reactivex.rxjava4.annotations.*;

/**
 * 精简队列接口：仅 offer、poll、isEmpty、clear。
 * 不含 Collection/Queue 的迭代与内省方法，供算子内部缓冲使用。
 *
 * @param <T> 元素类型，非 null
 * @since 3.1.1
 */
public interface SimpleQueue<@NonNull T> {

    /**
     * 原子入队单个元素。
     * @param value 非 null 元素
     * @return 成功 true；容量满等则 false
     */
    boolean offer(@NonNull T value);

    /**
     * 原子入队两个元素（成对 poll 时第二项保证非 null）。
     * @return 成功 true；满则 false
     */
    boolean offer(@NonNull T v1, @NonNull T v2);

    /**
     * 出队非 null 元素，空队列返回 null。
     * 成对 offer 时，若第一次 poll 非 null，第二次 poll 也保证非 null。
     * @throws Throwable 融合函数预处理可能抛出
     */
    @Nullable
    T poll() throws Throwable;

    /**
     * 队列是否为空。
     * 融合 poll 可能使 isEmpty 为 false 而 poll 仍返回 null。
     */
    boolean isEmpty();

    /** 清空队列中所有已入队元素。 */
    void clear();
}
