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
import io.reactivex.rxjava4.functions.Action;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 在 downstream 收到 onSuccess 或 onError 之后调用 onAfterTerminate。
 * 回调异常走 RxJavaPlugins.onError，不影响已转发的终端事件。
 * <p>History: 2.0.6 - experimental
 * @param <T> 元素类型
 * @since 2.1
 */
public final class SingleDoAfterTerminate<T> extends Single<T> {

    final SingleSource<T> source;

    final Action onAfterTerminate;

    /**
     * @param source 上游 SingleSource
     * @param onAfterTerminate 终端事件转发后执行的 Action
     */
    public SingleDoAfterTerminate(SingleSource<T> source, Action onAfterTerminate) {
        this.source = source;
        this.onAfterTerminate = onAfterTerminate;
    }

    /** 订阅 DoAfterTerminateObserver 在成功或错误后触发 Action。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new DoAfterTerminateObserver<>(observer, onAfterTerminate));
    }

    /** onSuccess/onError 均先转发再 onAfterTerminate()。 */
    static final class DoAfterTerminateObserver<T> implements SingleObserver<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final Action onAfterTerminate;

        Disposable upstream;

        DoAfterTerminateObserver(SingleObserver<? super T> actual, Action onAfterTerminate) {
            this.downstream = actual;
            this.onAfterTerminate = onAfterTerminate;
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

            onAfterTerminate();
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);

            onAfterTerminate();
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** 执行 Action；异常经 RxJavaPlugins 上报。 */
        private void onAfterTerminate() {
            try {
                onAfterTerminate.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
            }
        }
    }
}
