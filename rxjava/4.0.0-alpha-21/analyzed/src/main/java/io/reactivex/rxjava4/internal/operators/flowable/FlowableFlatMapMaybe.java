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

/* ===== [OCA 中文解析] =====
文件意图总览

对每个上游元素映射 MaybeSource，onSuccess 值可用即向下游发射，支持 maxConcurrency。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/* ===== [OCA 中文解析] =====
class FlowableFlatMapMaybe — 意图说明

FlatMapMaybeSubscriber 合并多路 Maybe 结果。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Maps upstream values into MaybeSources and merges their signals into one sequence.
 * @param <T> the source value type
 * @param <R> the result value type
 */
public final class FlowableFlatMapMaybe<T, R> extends AbstractFlowableWithUpstream<T, R> {

    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

    final boolean delayErrors;

    final int maxConcurrency;

    public FlowableFlatMapMaybe(Flowable<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper,
            boolean delayError, int maxConcurrency) {
        super(source);
        this.mapper = mapper;
        this.delayErrors = delayError;
        this.maxConcurrency = maxConcurrency;
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new FlatMapMaybeSubscriber<>(s, mapper, delayErrors, maxConcurrency));
    }

    /** 内部 FlatMapMaybeSubscriber。 */


    static final class FlatMapMaybeSubscriber<T, R>
    extends AtomicInteger
    implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = 8600231336733376951L;

        final Subscriber<? super R> downstream;

        final boolean delayErrors;

        final int maxConcurrency;

        final AtomicLong requested;

        final CompositeDisposable set;

        final AtomicInteger active;

        final AtomicThrowable errors;

        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

        final AtomicReference<SpscLinkedArrayQueue<R>> queue;

        Subscription upstream;

        volatile boolean cancelled;

        FlatMapMaybeSubscriber(Subscriber<? super R> actual,
                Function<? super T, ? extends MaybeSource<? extends R>> mapper, boolean delayErrors, int maxConcurrency) {
            this.downstream = actual;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
            this.maxConcurrency = maxConcurrency;
            this.requested = new AtomicLong();
            this.set = new CompositeDisposable();
            this.errors = new AtomicThrowable();
            this.active = new AtomicInteger(1);
            this.queue = new AtomicReference<>();
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                int m = maxConcurrency;
                if (m == Integer.MAX_VALUE) {
                    s.request(Long.MAX_VALUE);
                } else {
                    s.request(maxConcurrency);
                }
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            MaybeSource<? extends R> ms;

            try {
                ms = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null MaybeSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.cancel();
                onError(ex);
                return;
            }

            active.getAndIncrement();

            InnerObserver inner = new InnerObserver();

            if (!cancelled && set.add(inner)) {
                ms.subscribe(inner);
            }
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


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

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            active.decrementAndGet();
            drain();
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            cancelled = true;
            upstream.cancel();
            set.dispose();
            errors.tryTerminateAndReport();
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
                drain();
            }
        }

        void innerSuccess(InnerObserver inner, R value) {
            set.delete(inner);
            if (get() == 0 && compareAndSet(0, 1)) {
                boolean d = active.decrementAndGet() == 0;
                if (requested.get() != 0) {
                    downstream.onNext(value);

                    SpscLinkedArrayQueue<R> q = queue.get();

                    if (checkTerminate(d, q)) {
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }
                    BackpressureHelper.produced(requested, 1);
                    if (maxConcurrency != Integer.MAX_VALUE) {
                        upstream.request(1);
                    }
                } else {
                    SpscLinkedArrayQueue<R> q = getOrCreateQueue();
                    synchronized (q) {
                        q.offer(value);
                    }
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
            current = new SpscLinkedArrayQueue<>(Flowable.bufferSize());
            if (queue.compareAndSet(null, current)) {
                return current;
            }
            return queue.get();
        }

        void innerError(InnerObserver inner, Throwable e) {
            set.delete(inner);
            if (errors.tryAddThrowableOrReport(e)) {
                if (!delayErrors) {
                    upstream.cancel();
                    set.dispose();
                } else {
                    if (maxConcurrency != Integer.MAX_VALUE) {
                        upstream.request(1);
                    }
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

                if (checkTerminate(d, q)) {
                    errors.tryTerminateConsumer(downstream);
                    return;
                }

                if (maxConcurrency != Integer.MAX_VALUE) {
                    upstream.request(1);
                }
                if (decrementAndGet() == 0) {
                    return;
                }
                drainLoop();
            } else {
                active.decrementAndGet();
                if (maxConcurrency != Integer.MAX_VALUE) {
                    upstream.request(1);
                }
                drain();
            }
        }

        static boolean checkTerminate(boolean d, SpscLinkedArrayQueue<?> q) {
            return d && (q == null || q.isEmpty());
        }

        /** drain 循环：按 request 从队列取元素发射。 */


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

        void drainLoop() {
            int missed = 1;
            Subscriber<? super R> a = downstream;
            AtomicInteger n = active;
            AtomicReference<SpscLinkedArrayQueue<R>> qr = queue;

            for (;;) {
                long r = requested.get();
                long e = 0L;

                while (e != r) {
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

                    e++;
                }

                if (e == r) {
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
                    boolean empty = q == null || q.isEmpty();

                    if (d && empty) {
                        errors.tryTerminateConsumer(a);
                        return;
                    }
                }

                if (e != 0L) {
                    BackpressureHelper.produced(requested, e);
                    if (maxConcurrency != Integer.MAX_VALUE) {
                        upstream.request(e);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

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

            /** 处理上游/onError 并按策略终止或延迟错误。 */


            @Override


            public void onError(Throwable e) {
                innerError(this, e);
            }

            /** 上游完成：清理资源并向下游发送 onComplete。 */


            @Override


            public void onComplete() {
                innerComplete(this);
            }

            /** 返回是否已 dispose。 */


            @Override


            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }

            /** dispose 连接/inner 并清理状态。 */


            @Override


            public void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
