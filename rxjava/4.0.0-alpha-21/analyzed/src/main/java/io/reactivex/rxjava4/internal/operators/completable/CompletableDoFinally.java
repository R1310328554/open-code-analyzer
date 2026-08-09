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

package io.reactivex.rxjava4.internal.operators.completable;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Action;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 在 onError、onComplete 或 dispose 事件之后执行一个 action。
 * <p>History: 2.0.1 - experimental
 * @since 2.1
 */
public final class CompletableDoFinally extends Completable {

    final CompletableSource source;

    final Action onFinally;

    /**
     * @param source 上游 CompletableSource
     * @param onFinally 终止或 dispose 时执行的 action
     */
    public CompletableDoFinally(CompletableSource source, Action onFinally) {
        this.source = source;
        this.onFinally = onFinally;
    }

    /** 订阅上游并在 DoFinallyObserver 中包装下游 observer。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new DoFinallyObserver(observer, onFinally));
    }

    static final class DoFinallyObserver extends AtomicInteger implements CompletableObserver, Disposable {

        @Serial
        private static final long serialVersionUID = 4109457741734051389L;

        final CompletableObserver downstream;

        final Action onFinally;

        Disposable upstream;

        DoFinallyObserver(CompletableObserver actual, Action onFinally) {
            this.downstream = actual;
            this.onFinally = onFinally;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
            runFinally();
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
            runFinally();
        }

        @Override
        public void dispose() {
            upstream.dispose();
            runFinally();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** 保证 onFinally 至多执行一次。 */
        void runFinally() {
            if (compareAndSet(0, 1)) {
                try {
                    onFinally.run();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                }
            }
        }
    }
}
