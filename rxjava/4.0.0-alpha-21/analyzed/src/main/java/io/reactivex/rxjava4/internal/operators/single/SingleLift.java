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

package io.reactivex.rxjava4.internal.operators.single;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;

import java.util.Objects;

/**
 * 通过 {@link SingleOperator} 变换下游 SingleObserver 后订阅上游。
 * onLift 返回 null 或抛异常时以 EmptyDisposable.error 终止。
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 */
public final class SingleLift<T, R> extends Single<R> {

    final SingleSource<T> source;

    final SingleOperator<? extends R, ? super T> onLift;

    /**
     * @param source 上游 SingleSource
     * @param onLift 将 downstream Observer 变换为 upstream Observer 的算子
     */
    public SingleLift(SingleSource<T> source, SingleOperator<? extends R, ? super T> onLift) {
        this.source = source;
        this.onLift = onLift;
    }

    /** 应用 onLift 得到 sr，成功则 source.subscribe(sr)。 */
    @Override
    protected void subscribeActual(SingleObserver<? super R> observer) {
        SingleObserver<? super T> sr;

        try {
            sr = Objects.requireNonNull(onLift.apply(observer), "The onLift returned a null SingleObserver");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        source.subscribe(sr);
    }

}
