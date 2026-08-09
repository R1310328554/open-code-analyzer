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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;

/**
 * 在上游 Single 成功时执行 onSuccess 副作用回调，
 * 再向下游转发同一成功值（回调异常转 onError）。
 *
 * @param <T> 元素类型
 */
public final class SingleDoOnSuccess<T> extends Single<T> {

    final SingleSource<T> source;

    final Consumer<? super T> onSuccess;

    /**
     * @param source 上游 SingleSource
     * @param onSuccess 成功值到达时执行的 Consumer
     */
    public SingleDoOnSuccess(SingleSource<T> source, Consumer<? super T> onSuccess) {
        this.source = source;
        this.onSuccess = onSuccess;
    }

    /** 订阅 DoOnSuccess 包装 Observer 执行副作用后转发。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        source.subscribe(new DoOnSuccess(observer));
    }

    /** onSuccess 时先 onSuccess.accept 再 downstream.onSuccess。 */
    final class DoOnSuccess implements SingleObserver<T> {

        final SingleObserver<? super T> downstream;

        DoOnSuccess(SingleObserver<? super T> observer) {
            this.downstream = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            downstream.onSubscribe(d);
        }

        /** 执行副作用后转发成功值；回调异常转 downstream.onError。 */
        @Override
        public void onSuccess(T value) {
            try {
                onSuccess.accept(value);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

    }
}
