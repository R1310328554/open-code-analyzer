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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.BiConsumer;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 上游 Maybe 终止时用 {@link BiConsumer} 回调：
 * onSuccess 传 (value, null)，onError 传 (null, error)，onComplete 传 (null, null)。
 *
 * @param <T> 元素类型
 */
public final class MaybeDoOnEvent<T> extends AbstractMaybeWithUpstream<T, T> {

    final BiConsumer<? super T, ? super Throwable> onEvent;

    /**
     * @param source 上游 MaybeSource
     * @param onEvent 事件回调 BiConsumer
     */
    public MaybeDoOnEvent(MaybeSource<T> source, BiConsumer<? super T, ? super Throwable> onEvent) {
        super(source);
        this.onEvent = onEvent;
    }

    /** 包装为 DoOnEventMaybeObserver。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new DoOnEventMaybeObserver<>(observer, onEvent));
    }

    /** 先调用 onEvent 再转发下游；回调异常会中断正常事件。 */
    static final class DoOnEventMaybeObserver<T> implements MaybeObserver<T>, Disposable {
        final MaybeObserver<? super T> downstream;

        final BiConsumer<? super T, ? super Throwable> onEvent;

        Disposable upstream;

        DoOnEventMaybeObserver(MaybeObserver<? super T> actual, BiConsumer<? super T, ? super Throwable> onEvent) {
            this.downstream = actual;
            this.onEvent = onEvent;
        }

        @Override
        public void dispose() {
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
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
            upstream = DisposableHelper.DISPOSED;

            try {
                onEvent.accept(value, null);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;

            try {
                onEvent.accept(null, e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }

            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            upstream = DisposableHelper.DISPOSED;

            try {
                onEvent.accept(null, null);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            downstream.onComplete();
        }
    }
}
