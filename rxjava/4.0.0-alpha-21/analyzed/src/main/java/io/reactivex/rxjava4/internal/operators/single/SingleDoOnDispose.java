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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Action;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 下游 dispose 时先执行 onDispose Action，再 dispose 上游。
 * AtomicReference 存 Action，getAndSet(null) 保证仅调用一次。
 * @param <T> 元素类型
 */
public final class SingleDoOnDispose<T> extends Single<T> {
    final SingleSource<T> source;

    final Action onDispose;

    /**
     * @param source 上游 SingleSource
     * @param onDispose dispose 时执行的 Action
     */
    public SingleDoOnDispose(SingleSource<T> source, Action onDispose) {
        this.source = source;
        this.onDispose = onDispose;
    }

    /** 订阅 DoOnDisposeObserver 拦截 dispose 路径。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        source.subscribe(new DoOnDisposeObserver<>(observer, onDispose));
    }

    /** dispose 时 getAndSet Action 并 run，再 upstream.dispose。 */
    static final class DoOnDisposeObserver<T>
    extends AtomicReference<Action>
    implements SingleObserver<T>, Disposable {
        @Serial
        private static final long serialVersionUID = -8583764624474935784L;

        final SingleObserver<? super T> downstream;

        Disposable upstream;

        DoOnDisposeObserver(SingleObserver<? super T> actual, Action onDispose) {
            this.downstream = actual;
            this.lazySet(onDispose);
        }

        /** 取并清空 Action 后 run；再 dispose 上游。 */
        @Override
        public void dispose() {
            Action a = getAndSet(null);
            if (a != null) {
                try {
                    a.run();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                }
                upstream.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }
    }

}
