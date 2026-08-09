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
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 先向 downstream 转发 onSuccess，再调用 onAfterSuccess 消费成功值。
 * onAfterSuccess 异常无法转 onError（已终止），走 RxJavaPlugins.onError。
 * <p>History: 2.0.1 - experimental
 * @param <T> 元素类型
 * @since 2.1
 */
public final class SingleDoAfterSuccess<T> extends Single<T> {

    final SingleSource<T> source;

    final Consumer<? super T> onAfterSuccess;

    /**
     * @param source 上游 SingleSource
     * @param onAfterSuccess 成功值下发后执行的 Consumer
     */
    public SingleDoAfterSuccess(SingleSource<T> source, Consumer<? super T> onAfterSuccess) {
        this.source = source;
        this.onAfterSuccess = onAfterSuccess;
    }

    /** 订阅 DoAfterObserver 在 onSuccess 后触发回调。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new DoAfterObserver<>(observer, onAfterSuccess));
    }

    /** onSuccess 顺序：downstream.onSuccess 再 onAfterSuccess.accept。 */
    static final class DoAfterObserver<T> implements SingleObserver<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final Consumer<? super T> onAfterSuccess;

        Disposable upstream;

        DoAfterObserver(SingleObserver<? super T> actual, Consumer<? super T> onAfterSuccess) {
            this.downstream = actual;
            this.onAfterSuccess = onAfterSuccess;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 先转发成功值；回调异常走 RxJavaPlugins（不可 onError）。 */
        @Override
        public void onSuccess(T t) {
            downstream.onSuccess(t);

            try {
                onAfterSuccess.accept(t);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                // remember, onSuccess is a terminal event and we can't call onError
                RxJavaPlugins.onError(ex);
            }
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }
}
