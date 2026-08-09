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

package io.reactivex.rxjava4.internal.operators.observable;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.operators.SimplePlainQueue;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/**
 * 合并 {@link Observable} 与 {@link Maybe}：交错发射 Observable 元素与 Maybe 的 onSuccess 值，
 * 两者均正常终止后才 onComplete。
 * <p>History: 2.1.10 - experimental
 * @param <T> Observable 元素类型
 * @since 2.2
 */
public final class ObservableMergeWithMaybe<T> extends AbstractObservableWithUpstream<T, T> {

    final MaybeSource<? extends T> other;

    public ObservableMergeWithMaybe(Observable<T> source, MaybeSource<? extends T> other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        MergeWithObserver<T> parent = new MergeWithObserver<>(observer);
        observer.onSubscribe(parent);
        source.subscribe(parent);
        other.subscribe(parent.otherObserver);
    }

    static final class MergeWithObserver<T> extends AtomicInteger
    implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -4592979584110982903L;

        final Observer<? super T> downstream;

        final AtomicReference<Disposable> mainDisposable;

        final OtherObserver<T> otherObserver;

        final AtomicThrowable errors;

        volatile SimplePlainQueue<T> queue;

        T singleItem;

        volatile boolean disposed;

        volatile boolean mainDone;

        volatile int otherState;

        /** Maybe 已成功且值尚未消费。 */
        static final int OTHER_STATE_HAS_VALUE = 1;

        /** Maybe 已消费或为空（onComplete）。 */
        static final int OTHER_STATE_CONSUMED_OR_EMPTY = 2;

        MergeWithObserver(Observer<? super T> downstream) {
            this.downstream = downstream;
            this.mainDisposable = new AtomicReference<>();
            this.otherObserver = new OtherObserver<>(this);
            this.errors = new AtomicThrowable();
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(mainDisposable, d);
        }

        @Override
        public void onNext(T t) {
            if (compareAndSet(0, 1)) {
                downstream.onNext(t);
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimplePlainQueue<T> q = getOrCreateQueue();
                q.offer(t);
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override
        public void onError(Throwable ex) {
            if (errors.tryAddThrowableOrReport(ex)) {
                DisposableHelper.dispose(otherObserver);
                drain();
            }
        }

        @Override
        public void onComplete() {
            mainDone = true;
            drain();
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(mainDisposable.get());
        }

        @Override
        public void dispose() {
            disposed = true;
            DisposableHelper.dispose(mainDisposable);
            DisposableHelper.dispose(otherObserver);
            errors.tryTerminateAndReport();
            if (getAndIncrement() == 0) {
                queue = null;
                singleItem = null;
            }
        }

        /** Maybe onSuccess：立即或入队后 drain 发射。 */
        void otherSuccess(T value) {
            if (compareAndSet(0, 1)) {
                downstream.onNext(value);
                otherState = OTHER_STATE_CONSUMED_OR_EMPTY;
            } else {
                singleItem = value;
                otherState = OTHER_STATE_HAS_VALUE;
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        void otherError(Throwable ex) {
            if (errors.tryAddThrowableOrReport(ex)) {
                DisposableHelper.dispose(mainDisposable);
                drain();
            }
        }

        void otherComplete() {
            otherState = OTHER_STATE_CONSUMED_OR_EMPTY;
            drain();
        }

        SimplePlainQueue<T> getOrCreateQueue() {
            SimplePlainQueue<T> q = queue;
            if (q == null) {
                q = new SpscLinkedArrayQueue<>(bufferSize());
                queue = q;
            }
            return q;
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        /** 串行 drain：先消费 Maybe 缓存值，再 poll 主队列，双端完成时 onComplete。 */
        void drainLoop() {
            Observer<? super T> actual = this.downstream;
            int missed = 1;
            for (;;) {

                for (;;) {
                    if (disposed) {
                        singleItem = null;
                        queue = null;
                        return;
                    }

                    if (errors.get() != null) {
                        singleItem = null;
                        queue = null;
                        errors.tryTerminateConsumer(actual);
                        return;
                    }

                    int os = otherState;
                    if (os == OTHER_STATE_HAS_VALUE) {
                        T v = singleItem;
                        singleItem = null;
                        otherState = OTHER_STATE_CONSUMED_OR_EMPTY;
                        os = OTHER_STATE_CONSUMED_OR_EMPTY;
                        actual.onNext(v);
                    }

                    boolean d = mainDone;
                    SimplePlainQueue<T> q = queue;
                    T v = q != null ? q.poll() : null;
                    boolean empty = v == null;

                    if (d && empty && os == OTHER_STATE_CONSUMED_OR_EMPTY) {
                        queue = null;
                        actual.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    actual.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /** Maybe 侧 Observer，成功/完成/错误回调至 parent。 */
        static final class OtherObserver<T> extends AtomicReference<Disposable>
        implements MaybeObserver<T> {

            @Serial
            private static final long serialVersionUID = -2935427570954647017L;

            final MergeWithObserver<T> parent;

            OtherObserver(MergeWithObserver<T> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onSuccess(T t) {
                parent.otherSuccess(t);
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
