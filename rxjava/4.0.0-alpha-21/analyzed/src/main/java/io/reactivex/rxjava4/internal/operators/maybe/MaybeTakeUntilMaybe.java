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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 转发主源事件，除非 other {@link MaybeSource} 先 onSuccess 或 onComplete，
 * 此时取消主源并向下游 onComplete。
 *
 * @param <T> 主源元素类型
 * @param <U> other 元素类型
 */
public final class MaybeTakeUntilMaybe<T, U> extends AbstractMaybeWithUpstream<T, T> {

    final MaybeSource<U> other;

    /**
     * @param source 主 MaybeSource
     * @param other 触发截断的 MaybeSource
     */
    public MaybeTakeUntilMaybe(MaybeSource<T> source, MaybeSource<U> other) {
        super(source);
        this.other = other;
    }

    /** 同时订阅 main 与 other；other 先终止则截断 main。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        TakeUntilMainMaybeObserver<T, U> parent = new TakeUntilMainMaybeObserver<>(observer);
        observer.onSubscribe(parent);

        other.subscribe(parent.other);

        source.subscribe(parent);
    }

    /** 主源事件前 dispose other；other 先完成则 otherComplete。 */
    static final class TakeUntilMainMaybeObserver<T, U>
    extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -2187421758664251153L;

        final MaybeObserver<? super T> downstream;

        final TakeUntilOtherMaybeObserver<U> other;

        TakeUntilMainMaybeObserver(MaybeObserver<? super T> downstream) {
            this.downstream = downstream;
            this.other = new TakeUntilOtherMaybeObserver<>(this);
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
            DisposableHelper.dispose(other);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onSuccess(T value) {
            DisposableHelper.dispose(other);
            if (getAndSet(DisposableHelper.DISPOSED) != DisposableHelper.DISPOSED) {
                downstream.onSuccess(value);
            }
        }

        @Override
        public void onError(Throwable e) {
            DisposableHelper.dispose(other);
            if (getAndSet(DisposableHelper.DISPOSED) != DisposableHelper.DISPOSED) {
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        @Override
        public void onComplete() {
            DisposableHelper.dispose(other);
            if (getAndSet(DisposableHelper.DISPOSED) != DisposableHelper.DISPOSED) {
                downstream.onComplete();
            }
        }

        void otherError(Throwable e) {
            if (DisposableHelper.dispose(this)) {
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        /** other 成功或完成时 dispose 主源并 onComplete。 */
        void otherComplete() {
            if (DisposableHelper.dispose(this)) {
                downstream.onComplete();
            }
        }

        /** other 任意终止事件通知 parent.otherComplete/otherError。 */
        static final class TakeUntilOtherMaybeObserver<U>
        extends AtomicReference<Disposable> implements MaybeObserver<U> {

            @Serial
            private static final long serialVersionUID = -1266041316834525931L;

            final TakeUntilMainMaybeObserver<?, U> parent;

            TakeUntilOtherMaybeObserver(TakeUntilMainMaybeObserver<?, U> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onSuccess(Object value) {
                parent.otherComplete();
            }

            @Override
            public void onError(Throwable e) {
                parent.otherError(e);
            }

            @Override
            public void onComplete() {
                parent.otherComplete();
            }
        }
    }

}
