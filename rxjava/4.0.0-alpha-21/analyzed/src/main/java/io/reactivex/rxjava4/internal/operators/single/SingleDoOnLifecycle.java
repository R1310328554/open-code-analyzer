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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 在上游 onSubscribe 与下游 dispose 时分别调用 onSubscribe Consumer 与 onDispose Action。
 * onSubscribe 回调异常则 dispose 上游并以 EmptyDisposable.error 终止。
 * @param <T> 元素类型
 * @since 3.0.0
 */
public final class SingleDoOnLifecycle<T> extends Single<T> {

    final Single<T> source;

    final Consumer<? super Disposable> onSubscribe;

    final Action onDispose;

    /**
     * @param upstream 上游 Single
     * @param onSubscribe 上游 onSubscribe 时接收 Disposable 的 Consumer
     * @param onDispose 下游 dispose 时执行的 Action
     */
    public SingleDoOnLifecycle(Single<T> upstream, Consumer<? super Disposable> onSubscribe,
            Action onDispose) {
        this.source = upstream;
        this.onSubscribe = onSubscribe;
        this.onDispose = onDispose;
    }

    /** 订阅 SingleLifecycleObserver 桥接生命周期回调。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new SingleLifecycleObserver<>(observer, onSubscribe, onDispose));
    }

    /** onSubscribe 先 accept 再 validate；dispose 先 onDispose 再 upstream.dispose。 */
    static final class SingleLifecycleObserver<T> implements SingleObserver<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final Consumer<? super Disposable> onSubscribe;

        final Action onDispose;

        Disposable upstream;

        SingleLifecycleObserver(SingleObserver<? super T> downstream, Consumer<? super Disposable> onSubscribe, Action onDispose) {
            this.downstream = downstream;
            this.onSubscribe = onSubscribe;
            this.onDispose = onDispose;
        }

        /** 先 onSubscribe.accept；异常则 dispose 并以 error 终止订阅。 */
        @Override
        public void onSubscribe(@NonNull Disposable d) {
            // this way, multiple calls to onSubscribe can show up in tests that use doOnSubscribe to validate behavior
            try {
                onSubscribe.accept(d);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                d.dispose();
                this.upstream = DisposableHelper.DISPOSED;
                EmptyDisposable.error(e, downstream);
                return;
            }
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(@NonNull T t) {
            if (upstream != DisposableHelper.DISPOSED) {
                upstream = DisposableHelper.DISPOSED;
                downstream.onSuccess(t);
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            if (upstream != DisposableHelper.DISPOSED) {
                upstream = DisposableHelper.DISPOSED;
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        /** 先 onDispose.run 再 upstream.dispose 并置 DISPOSED。 */
        @Override
        public void dispose() {
            try {
                onDispose.run();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                RxJavaPlugins.onError(e);
            }
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }
}
