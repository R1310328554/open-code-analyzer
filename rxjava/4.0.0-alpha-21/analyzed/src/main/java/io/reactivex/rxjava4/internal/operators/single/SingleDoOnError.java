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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Consumer;

/**
 * 上游 onError 时先调用 onError Consumer，再转发错误给 downstream。
 * Consumer 抛异常则合并为 CompositeException。
 * @param <T> 元素类型
 */
public final class SingleDoOnError<T> extends Single<T> {

    final SingleSource<T> source;

    final Consumer<? super Throwable> onError;

    /**
     * @param source 上游 SingleSource
     * @param onError 错误发生时的 Consumer
     */
    public SingleDoOnError(SingleSource<T> source, Consumer<? super Throwable> onError) {
        this.source = source;
        this.onError = onError;
    }

    /** 订阅 DoOnError 拦截 onError 路径。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        source.subscribe(new DoOnError(observer));
    }

    /** onError 时 accept 后 downstream.onError；回调异常合并异常。 */
    final class DoOnError implements SingleObserver<T> {
        private final SingleObserver<? super T> downstream;

        DoOnError(SingleObserver<? super T> observer) {
            this.downstream = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            downstream.onSubscribe(d);
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        /** 调用 onError.accept；异常则 CompositeException 合并转发。 */
        @Override
        public void onError(Throwable e) {
            try {
                onError.accept(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }
            downstream.onError(e);
        }

    }
}
