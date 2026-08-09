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

package io.reactivex.rxjava4.core.config;

import java.util.Objects;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Observable;
import io.reactivex.rxjava4.functions.BiPredicate;
import io.reactivex.rxjava4.internal.functions.ObjectHelper;

/**
 * sequenceEqual() 算子的配置 record。
 * @param <T> 被比较序列的元素类型
 * @param bufferSize 预期从内部 {@code ObservableSource} 缓存的元素数量
 * @param isEqual 比较两个元素的自定义 lambda
 * @since 4.0.0
 */
public record SequenceEqualConfig<T>(int bufferSize, @NonNull BiPredicate<? super T, ? super T> isEqual) {

    /**
     * 默认配置：bufferSize 为 Observable.bufferSize()，比较谓词为默认 Objects.equals。
     */
    public static final SequenceEqualConfig<Object> DEFAULT =
            new SequenceEqualConfig<>(Observable.bufferSize(), ObjectHelper.equalsPredicate());

    /**
     * 构造配置 record。
     * @param bufferSize 预期在内部缓冲的行组合项数量
     */
    public SequenceEqualConfig(int bufferSize) {
        this(bufferSize, ObjectHelper.equalsPredicate());
    }

    /**
     * 构造配置 record。
 * @param isEqual 比较两个元素的自定义 lambda
     */
    public SequenceEqualConfig(@NonNull BiPredicate<? super T, ? super T> isEqual) {
        this(Observable.bufferSize(), isEqual);
    }

    /**
     * 构造配置 record。
     * @param bufferSize 预期从内部 {@code ObservableSource} 缓存的元素数量
     * @param isEqual 比较两个元素的自定义 lambda
     */
    public SequenceEqualConfig {
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
        Objects.requireNonNull(isEqual, "isEqual is null");
    }
}
