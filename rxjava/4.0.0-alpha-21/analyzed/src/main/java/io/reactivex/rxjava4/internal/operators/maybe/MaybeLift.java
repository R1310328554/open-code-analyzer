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

package io.reactivex.rxjava4.internal.operators.maybe;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;

import java.util.Objects;

/**
 * 对下游 {@link MaybeObserver} 应用 {@link MaybeOperator} 进行变换，
 * 再用变换后的 Observer 订阅上游。
 *
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 */
public final class MaybeLift<T, R> extends AbstractMaybeWithUpstream<T, R> {

    final MaybeOperator<? extends R, ? super T> operator;

    /**
     * @param source 上游 Maybe
     * @param operator 对 MaybeObserver 进行变换的 MaybeOperator
     */
    public MaybeLift(MaybeSource<T> source, MaybeOperator<? extends R, ? super T> operator) {
        super(source);
        this.operator = operator;
    }

    /** 应用 operator 获取 lifted Observer 后订阅上游。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super R> observer) {
        MaybeObserver<? super T> lifted;

        try {
            lifted = Objects.requireNonNull(operator.apply(observer), "The operator returned a null MaybeObserver");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        source.subscribe(lifted);
    }
}
