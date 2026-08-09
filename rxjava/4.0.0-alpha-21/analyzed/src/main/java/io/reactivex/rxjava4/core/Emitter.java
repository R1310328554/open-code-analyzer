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

package io.reactivex.rxjava4.core;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 各类生成器式源算子（create、generate）中以 push 方式发射信号的基础接口。
 * <p>
 * 注意：通过 {@link Emitter} 实例提供给函数的 {@link Emitter#onNext}、{@link Emitter#onError}
 * 与 {@link Emitter#onComplete} 方法应同步调用，不可并发调用。
 * 从多线程调用不受支持，会导致未定义行为。
 *
 * @param <T> 发射的值类型
 */
public interface Emitter<@NonNull T> {

    /**
     * 发射正常值。
     * @param value 要发射的值，不可为 {@code null}
     */
    void onNext(@NonNull T value);

    /**
     * 发射 {@link Throwable} 异常。
     * @param error 要发射的 {@code Throwable}，不可为 {@code null}
     */
    void onError(@NonNull Throwable error);

    /**
     * 发射完成信号。
     */
    void onComplete();
}
