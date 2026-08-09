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
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 竞速上游 Single 与 other {@link Publisher}：other 先发出或完成则 CancellationException。
 * source 先成功则 dispose other 并 onSuccess；任一侧错误按规则转发。
 * @param <T> 主 Single 元素类型
 * @param <U> other Publisher 元素类型
 */
public final class SingleTakeUntil<T, U> extends Single<T> {

    final SingleSource<T> source;

    final Publisher<U> other;

    /**
     * @param source 主 SingleSource
     * @param other 用于竞速取消的 Publisher
     */
    public SingleTakeUntil(SingleSource<T> source, Publisher<U> other) {
        this.source = source;
        this.other = other;
    }

    /** 同时订阅 TakeUntilMainObserver 与 other 的 TakeUntilOtherSubscriber。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        TakeUntilMainObserver<T> parent = new TakeUntilMainObserver<>(observer);
        observer.onSubscribe(parent);

        other.subscribe(parent.other);

        source.subscribe(parent);
    }

    /** 主 Single 观察者：成功时 dispose other；错误/other 先动则取消或转发。 */
    static final class TakeUntilMainObserver<T>
    extends AtomicReference<Disposable>
    implements SingleObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -622603812305745221L;

        final SingleObserver<? super T> downstream;

        final TakeUntilOtherSubscriber other;

        TakeUntilMainObserver(SingleObserver<? super T> downstream) {
            this.downstream = downstream;
            this.other = new TakeUntilOtherSubscriber(this);
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
            other.dispose();
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        /** dispose other 后若未 DISPOSED 则 downstream.onSuccess。 */
        @Override
        public void onSuccess(T value) {
            other.dispose();

            Disposable a = getAndSet(DisposableHelper.DISPOSED);
            if (a != DisposableHelper.DISPOSED) {
                downstream.onSuccess(value);
            }
        }

        @Override
        public void onError(Throwable e) {
            other.dispose();

            Disposable a = get();
            if (a != DisposableHelper.DISPOSED) {
                a = getAndSet(DisposableHelper.DISPOSED);
                if (a != DisposableHelper.DISPOSED) {
                    downstream.onError(e);
                    return;
                }
            }
            RxJavaPlugins.onError(e);
        }

        /** other 触发错误/取消：dispose 主源并以 e 终止 downstream。 */
        void otherError(Throwable e) {
            Disposable a = get();
            if (a != DisposableHelper.DISPOSED) {
                a = getAndSet(DisposableHelper.DISPOSED);
                if (a != DisposableHelper.DISPOSED) {
                    if (a != null) {
                        a.dispose();
                    }
                    downstream.onError(e);
                    return;
                }
            }
            RxJavaPlugins.onError(e);
        }
    }

    /** other 侧订阅者：onNext/onComplete 取消并 parent.otherError(CancellationException)。 */
    static final class TakeUntilOtherSubscriber
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<Object> {

        @Serial
        private static final long serialVersionUID = 5170026210238877381L;

        final TakeUntilMainObserver<?> parent;

        TakeUntilOtherSubscriber(TakeUntilMainObserver<?> parent) {
            this.parent = parent;
        }

        @Override
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        /** cancel 成功后 parent.otherError(CancellationException)。 */
        @Override
        public void onNext(Object t) {
            if (SubscriptionHelper.cancel(this)) {
                parent.otherError(new CancellationException());
            }
        }

        @Override
        public void onError(Throwable t) {
            parent.otherError(t);
        }

        @Override
        public void onComplete() {
            if (get() != SubscriptionHelper.CANCELLED) {
                lazySet(SubscriptionHelper.CANCELLED);
                parent.otherError(new CancellationException());
            }
        }

        public void dispose() {
            SubscriptionHelper.cancel(this);
        }
    }
}
