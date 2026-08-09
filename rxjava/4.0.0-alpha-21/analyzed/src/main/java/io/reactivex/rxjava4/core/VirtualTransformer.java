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

import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 由 {@link Flowable#virtualTransform(VirtualTransformer, java.util.concurrent.ExecutorService)} 算子调用，
 * 基于上游当前输入生成任意数量的输出值。
 *
 * @param <T> 源值类型
 * @param <R> 结果值类型
 * @since 4.0.0
 */
@FunctionalInterface
public interface VirtualTransformer<T, R> {

    /**
     * 实现本方法，通过
     * {@link VirtualEmitter#emit(Object)}.
     * 
     * @param value 上游值
     * @param emitter 用于生成结果值的 emitter
     * @param stopper 调用以停止上游
     * @throws Throwable 作为下游的 {@code onError} 信号
     */
    void transform(T value, VirtualEmitter<R> emitter, Disposable stopper) throws Throwable;
}