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
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Action;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 在 onSuccess、onError 或 dispose 后执行 onFinally，且仅执行一次。
 * AtomicInteger CAS 门控保证 runFinally 不重复调用。
 * <p>History: 2.0.1 - experimental
 * @param <T> 元素类型
 * @since 2.1
 */
public final class SingleDoFinally<T> extends Single<T> {

    final SingleSource<T> source;

    final Action onFinally;

    /**
     * @param source 上游 SingleSource
     * @param onFinally 终止或 dispose 后执行的 Action
     */
    public SingleDoFinally(SingleSource<T> source, Action onFinally) {
        this.source = source;
        this.onFinally = onFinally;
    }

    /** 订阅 DoFinallyObserver 在三种路径触发 runFinally。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new DoFinallyObserver<>(observer, onFinally));
    }

    /** compareAndSet(0,1) 保证 onFinally 仅执行一次。 */
    static final class DoFinallyObserver<T> extends AtomicInteger implements SingleObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 4109457741734051389L;

        final SingleObserver<? super T> downstream;

        final Action onFinally;

        Disposable upstream;

        DoFinallyObserver(SingleObserver<? super T> actual, Action onFinally) {
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
        public void onSuccess(T t) {
            downstream.onSuccess(t);
            runFinally();
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
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

        /** CAS 成功后 run onFinally；异常走 RxJavaPlugins。 */
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
