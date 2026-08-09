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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * other {@link MaybeSource} 先 onSuccess 或 onComplete 时切换到 fallback Maybe；
 * fallback 为 null 则向下游 onError({@link TimeoutException})。
 *
 * @param <T> 主源元素类型
 * @param <U> other 元素类型
 */
public final class MaybeTimeoutMaybe<T, U> extends AbstractMaybeWithUpstream<T, T> {

    final MaybeSource<U> other;

    final MaybeSource<? extends T> fallback;

    /**
     * @param source 主 MaybeSource
     * @param other 超时触发源
     * @param fallback 超时后的备用 MaybeSource（可为 null）
     */
    public MaybeTimeoutMaybe(MaybeSource<T> source, MaybeSource<U> other, MaybeSource<? extends T> fallback) {
        super(source);
        this.other = other;
        this.fallback = fallback;
    }

    /** 同时订阅 main 与 other；other 先终止则 timeout 逻辑。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        TimeoutMainMaybeObserver<T, U> parent = new TimeoutMainMaybeObserver<>(observer, fallback);
        observer.onSubscribe(parent);

        other.subscribe(parent.other);

        source.subscribe(parent);
    }

    /** 主源正常终止时 dispose other；other 先终止则 otherComplete。 */
    static final class TimeoutMainMaybeObserver<T, U>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -5955289211445418871L;

        final MaybeObserver<? super T> downstream;

        final TimeoutOtherMaybeObserver<T, U> other;

        final MaybeSource<? extends T> fallback;

        final TimeoutFallbackMaybeObserver<T> otherObserver;

        TimeoutMainMaybeObserver(MaybeObserver<? super T> actual, MaybeSource<? extends T> fallback) {
            this.downstream = actual;
            this.other = new TimeoutOtherMaybeObserver<>(this);
            this.fallback = fallback;
            this.otherObserver = fallback != null ? new TimeoutFallbackMaybeObserver<>(actual) : null;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
            DisposableHelper.dispose(other);
            TimeoutFallbackMaybeObserver<T> oo = otherObserver;
            if (oo != null) {
                DisposableHelper.dispose(oo);
            }
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

        public void otherError(Throwable e) {
            if (DisposableHelper.dispose(this)) {
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        /** fallback 非 null 则 subscribe otherObserver；否则 TimeoutException。 */
        public void otherComplete() {
            if (DisposableHelper.dispose(this)) {
                if (fallback == null) {
                    downstream.onError(new TimeoutException());
                } else {
                    fallback.subscribe(otherObserver);
                }
            }
        }
    }

    /** other 任意终止事件通知 parent.otherComplete/otherError。 */
    static final class TimeoutOtherMaybeObserver<T, U>
    extends AtomicReference<Disposable>
    implements MaybeObserver<Object> {

        @Serial
        private static final long serialVersionUID = 8663801314800248617L;

        final TimeoutMainMaybeObserver<T, U> parent;

        TimeoutOtherMaybeObserver(TimeoutMainMaybeObserver<T, U> parent) {
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
    /** fallback Maybe 信号 relay 到 downstream。 */
    static final class TimeoutFallbackMaybeObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T> {

        @Serial
        private static final long serialVersionUID = 8663801314800248617L;

        final MaybeObserver<? super T> downstream;

        TimeoutFallbackMaybeObserver(MaybeObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
