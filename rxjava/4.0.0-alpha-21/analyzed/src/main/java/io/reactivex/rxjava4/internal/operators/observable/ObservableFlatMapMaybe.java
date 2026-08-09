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
import java.util.Objects;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/**
 * 将上游每个元素映射为 {@link MaybeSource} 并合并其 onSuccess 值为单一序列；
 * inner onComplete 被忽略，错误可延迟聚合。
 * @param <T> 上游元素类型
 * @param <R> 输出元素类型
 */
public final class ObservableFlatMapMaybe<T, R> extends AbstractObservableWithUpstream<T, R> {

    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

    final boolean delayErrors;

    final int bufferSize;

    /**
     * @param source 上游 ObservableSource
     * @param mapper 将元素映射为 MaybeSource 的函数
     * @param delayError 为 true 时延迟报告错误
     * @param bufferSize inner 成功值缓冲队列容量
     */
    public ObservableFlatMapMaybe(ObservableSource<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper,
            boolean delayError,
            int bufferSize) {
        super(source);
        this.mapper = mapper;
        this.delayErrors = delayError;
        this.bufferSize = bufferSize;
    }

    /** 订阅 FlatMapMaybeObserver 并 drain 合并 inner 成功值。 */
    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        source.subscribe(new FlatMapMaybeObserver<>(observer, mapper, delayErrors, bufferSize));
    }

    /** 维护 active 计数、错误集合与 Spsc 队列，drain 合并 inner onSuccess。 */
    static final class FlatMapMaybeObserver<T, R>
    extends AtomicInteger
    implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 8600231336733376951L;

        final Observer<? super R> downstream;

        final boolean delayErrors;

        final int bufferSize;

        final CompositeDisposable set;

        final AtomicInteger active;

        final AtomicThrowable errors;

        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

        final AtomicReference<SpscLinkedArrayQueue<R>> queue;

        Disposable upstream;

        volatile boolean cancelled;

        FlatMapMaybeObserver(Observer<? super R> actual,
                Function<? super T, ? extends MaybeSource<? extends R>> mapper, boolean delayErrors, int bufferSize) {
            this.downstream = actual;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
            this.bufferSize = bufferSize;
            this.set = new CompositeDisposable();
            this.errors = new AtomicThrowable();
            this.active = new AtomicInteger(1);
            this.queue = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            MaybeSource<? extends R> ms;

            try {
                ms = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null MaybeSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.dispose();
                onError(ex);
                return;
            }

            active.getAndIncrement();

            InnerObserver inner = new InnerObserver();

            if (!cancelled && set.add(inner)) {
                ms.subscribe(inner);
            }
        }

        @Override
        public void onError(Throwable t) {
            active.decrementAndGet();
            if (errors.tryAddThrowableOrReport(t)) {
                if (!delayErrors) {
                    set.dispose();
                }
                drain();
            }
        }

        @Override
        public void onComplete() {
            active.decrementAndGet();
            drain();
        }

        @Override
        public void dispose() {
            cancelled = true;
            upstream.dispose();
            set.dispose();
            errors.tryTerminateAndReport();
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        /** inner onSuccess 时直接 emit 或入队后 drainLoop。 */
        void innerSuccess(InnerObserver inner, R value) {
            set.delete(inner);
            if (get() == 0 && compareAndSet(0, 1)) {
                downstream.onNext(value);

                boolean d = active.decrementAndGet() == 0;
                SpscLinkedArrayQueue<R> q = queue.get();

                if (d && (q == null || q.isEmpty())) {
                    errors.tryTerminateConsumer(downstream);
                    return;
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SpscLinkedArrayQueue<R> q = getOrCreateQueue();
                synchronized (q) {
                    q.offer(value);
                }
                active.decrementAndGet();
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        SpscLinkedArrayQueue<R> getOrCreateQueue() {
            SpscLinkedArrayQueue<R> current = queue.get();
            if (current != null) {
                return current;
            }
            current = new SpscLinkedArrayQueue<>(bufferSize);
            if (queue.compareAndSet(null, current)) {
                return current;
            }
            return queue.get();
        }

        void innerError(InnerObserver inner, Throwable e) {
            set.delete(inner);
            if (errors.tryAddThrowableOrReport(e)) {
                if (!delayErrors) {
                    upstream.dispose();
                    set.dispose();
                }
                active.decrementAndGet();
                drain();
            }
        }

        void innerComplete(InnerObserver inner) {
            set.delete(inner);

            if (get() == 0 && compareAndSet(0, 1)) {
                boolean d = active.decrementAndGet() == 0;
                SpscLinkedArrayQueue<R> q = queue.get();

                if (d && (q == null || q.isEmpty())) {
                    errors.tryTerminateConsumer(downstream);
                    return;
                }
                if (decrementAndGet() == 0) {
                    return;
                }
                drainLoop();
            } else {
                active.decrementAndGet();
                drain();
            }
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        void clear() {
            SpscLinkedArrayQueue<R> q = queue.get();
            if (q != null) {
                q.clear();
            }
        }

        /** 从队列 poll 并 onNext，active==0 且队列空时终止下游。 */
        void drainLoop() {
            int missed = 1;
            Observer<? super R> a = downstream;
            AtomicInteger n = active;
            AtomicReference<SpscLinkedArrayQueue<R>> qr = queue;

            for (;;) {
                for (;;) {
                    if (cancelled) {
                        clear();
                        return;
                    }

                    if (!delayErrors) {
                        Throwable ex = errors.get();
                        if (ex != null) {
                            clear();
                            errors.tryTerminateConsumer(a);
                            return;
                        }
                    }

                    boolean d = n.get() == 0;
                    SpscLinkedArrayQueue<R> q = qr.get();
                    R v = q != null ? q.poll() : null;
                    boolean empty = v == null;

                    if (d && empty) {
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /** 订阅单个 inner Maybe 并将 onSuccess/onError/onComplete 回传 parent。 */
        final class InnerObserver extends AtomicReference<Disposable>
        implements MaybeObserver<R>, Disposable {
            @Serial
            private static final long serialVersionUID = -502562646270949838L;

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onSuccess(R value) {
                innerSuccess(this, value);
            }

            @Override
            public void onError(Throwable e) {
                innerError(this, e);
            }

            @Override
            public void onComplete() {
                innerComplete(this);
            }

            @Override
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }

            @Override
            public void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
