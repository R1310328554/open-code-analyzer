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

将 ParallelFlowable 各 rail 无序合并为单路 Flowable，协调多路 request/onNext 与 delayErrors。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.parallel;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SimplePlainQueue;
import io.reactivex.rxjava4.operators.SimpleQueue;
import io.reactivex.rxjava4.operators.SpscArrayQueue;
import io.reactivex.rxjava4.parallel.ParallelFlowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/* ===== [OCA 中文解析] =====
class ParallelJoin — 意图说明

JoinSubscription 合并多路 JoinInnerSubscriber 队列。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 将 ParallelFlowable 各 rail 无序合并为单路 Publisher（Flowable）序列。
 * into a single regular Publisher sequence (exposed as Flowable).
 *
 * @param <T> the value type
 */
public final class ParallelJoin<T> extends Flowable<T> {

    final ParallelFlowable<? extends T> source;

    final int prefetch;

    final boolean delayErrors;

    public ParallelJoin(ParallelFlowable<? extends T> source, int prefetch, boolean delayErrors) {
        this.source = source;
        this.prefetch = prefetch;
        this.delayErrors = delayErrors;
    }

    /** 组装内部 Subscriber 并订阅上游 ParallelFlowable。 */


    @Override


    protected void subscribeActual(Subscriber<? super T> s) {
        JoinSubscriptionBase<T> parent;
        if (delayErrors) {
            parent = new JoinSubscriptionDelayError<>(s, source.parallelism(), prefetch);
        } else {
            parent = new JoinSubscription<>(s, source.parallelism(), prefetch);
        }
        s.onSubscribe(parent);
        source.subscribe(parent.subscribers);
    }

    /** 内部抽象类 JoinSubscriptionBase。 */


    abstract static class JoinSubscriptionBase<T> extends AtomicInteger
    implements Subscription {

        @Serial
        private static final long serialVersionUID = 3100232009247827843L;

        final Subscriber<? super T> downstream;

        final JoinInnerSubscriber<T>[] subscribers;

        final AtomicThrowable errors = new AtomicThrowable();

        final AtomicLong requested = new AtomicLong();

        volatile boolean cancelled;

        final AtomicInteger done = new AtomicInteger();

        JoinSubscriptionBase(Subscriber<? super T> actual, int n, int prefetch) {
            this.downstream = actual;
            @SuppressWarnings("unchecked")
            JoinInnerSubscriber<T>[] a = new JoinInnerSubscriber[n];

            for (int i = 0; i < n; i++) {
                a[i] = new JoinInnerSubscriber<>(this, prefetch);
            }

            this.subscribers = a;
            done.lazySet(n);
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
                drain();
            }
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            if (!cancelled) {
                cancelled = true;

                cancelAll();

                if (getAndIncrement() == 0) {
                    cleanup();
                }
            }
        }

        void cancelAll() {
            for (JoinInnerSubscriber<T> s : subscribers) {
                s.cancel();
            }
        }

        void cleanup() {
            for (JoinInnerSubscriber<T> s : subscribers) {
                s.queue = null;
            }
        }

        abstract void onNext(JoinInnerSubscriber<T> inner, T value);

        abstract void onError(Throwable e);

        abstract void onComplete();

        abstract /** drain 循环：从队列取元素向下游发射。 */
 void drain();
    }

    /** 内部 JoinSubscription。 */


    static final class JoinSubscription<T> extends JoinSubscriptionBase<T> {

        @Serial
        private static final long serialVersionUID = 6312374661811000451L;

        JoinSubscription(Subscriber<? super T> actual, int n, int prefetch) {
            super(actual, n, prefetch);
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(JoinInnerSubscriber<T> inner, T value) {
            if (get() == 0 && compareAndSet(0, 1)) {
                if (requested.get() != 0) {
                    downstream.onNext(value);
                    if (requested.get() != Long.MAX_VALUE) {
                        requested.decrementAndGet();
                    }
                    inner.request(1);
                } else {
                    SimplePlainQueue<T> q = inner.getQueue();

                    if (!q.offer(value)) {
                        cancelAll();
                        Throwable mbe = new QueueOverflowException();
                        if (errors.compareAndSet(null, mbe)) {
                            downstream.onError(mbe);
                        } else {
                            RxJavaPlugins.onError(mbe);
                        }
                        return;
                    }
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimplePlainQueue<T> q = inner.getQueue();

                if (!q.offer(value)) {
                    cancelAll();
                    onError(new QueueOverflowException());
                    return;
                }

                if (getAndIncrement() != 0) {
                    return;
                }
            }

            drainLoop();
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable e) {
            if (errors.compareAndSet(null, e)) {
                cancelAll();
                drain();
            } else {
                if (e != errors.get()) {
                    RxJavaPlugins.onError(e);
                }
            }
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            done.decrementAndGet();
            drain();
        }

        @Override
        /** drain 循环：从队列取元素向下游发射。 */

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            drainLoop();
        }

        void drainLoop() {
            int missed = 1;

            JoinInnerSubscriber<T>[] s = this.subscribers;
            Subscriber<? super T> a = this.downstream;

            for (;;) {

                long r = requested.get();
                long e = 0;

                middle:
                while (e != r) {
                    if (cancelled) {
                        cleanup();
                        return;
                    }

                    Throwable ex = errors.get();
                    if (ex != null) {
                        cleanup();
                        a.onError(ex);
                        return;
                    }

                    boolean d = done.get() == 0;

                    boolean empty = true;

                    for (JoinInnerSubscriber<T> inner : s) {
                        SimplePlainQueue<T> q = inner.queue;
                        if (q != null) {
                            T v = q.poll();

                            if (v != null) {
                                empty = false;
                                a.onNext(v);
                                inner.requestOne();
                                if (++e == r) {
                                    break middle;
                                }
                            }
                        }
                    }

                    if (d && empty) {
                        a.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }
                }

                if (e == r) {
                    if (cancelled) {
                        cleanup();
                        return;
                    }

                    Throwable ex = errors.get();
                    if (ex != null) {
                        cleanup();
                        a.onError(ex);
                        return;
                    }

                    boolean d = done.get() == 0;

                    boolean empty = true;

                    for (JoinInnerSubscriber<T> inner : s) {
                        SimpleQueue<T> q = inner.queue;
                        if (q != null && !q.isEmpty()) {
                            empty = false;
                            break;
                        }
                    }

                    if (d && empty) {
                        a.onComplete();
                        return;
                    }
                }

                if (e != 0) {
                    BackpressureHelper.produced(requested, e);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

    /** 内部 JoinSubscriptionDelayError。 */


    static final class JoinSubscriptionDelayError<T> extends JoinSubscriptionBase<T> {

        @Serial
        private static final long serialVersionUID = -5737965195918321883L;

        JoinSubscriptionDelayError(Subscriber<? super T> actual, int n, int prefetch) {
            super(actual, n, prefetch);
        }

        @Override
        void onNext(JoinInnerSubscriber<T> inner, T value) {
            if (get() == 0 && compareAndSet(0, 1)) {
                if (requested.get() != 0) {
                    downstream.onNext(value);
                    if (requested.get() != Long.MAX_VALUE) {
                        requested.decrementAndGet();
                    }
                    inner.request(1);
                } else {
                    SimplePlainQueue<T> q = inner.getQueue();

                    if (!q.offer(value)) {
                        inner.cancel();
                        errors.tryAddThrowableOrReport(new QueueOverflowException());
                        done.decrementAndGet();
                        drainLoop();
                        return;
                    }
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimplePlainQueue<T> q = inner.getQueue();

                if (!q.offer(value)) {
                    inner.cancel();
                    errors.tryAddThrowableOrReport(new QueueOverflowException());
                    done.decrementAndGet();
                }

                if (getAndIncrement() != 0) {
                    return;
                }
            }

            drainLoop();
        }

        @Override
        void onError(Throwable e) {
            if (errors.tryAddThrowableOrReport(e)) {
                done.decrementAndGet();
                drain();
            }
        }

        @Override
        void onComplete() {
            done.decrementAndGet();
            drain();
        }

        @Override
        /** drain 循环：从队列取元素向下游发射。 */

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            drainLoop();
        }

        void drainLoop() {
            int missed = 1;

            JoinInnerSubscriber<T>[] s = this.subscribers;
            Subscriber<? super T> a = this.downstream;

            for (;;) {

                long r = requested.get();
                long e = 0;

                middle:
                while (e != r) {
                    if (cancelled) {
                        cleanup();
                        return;
                    }

                    boolean d = done.get() == 0;

                    boolean empty = true;

                    for (JoinInnerSubscriber<T> inner : s) {
                        SimplePlainQueue<T> q = inner.queue;
                        if (q != null) {
                            T v = q.poll();

                            if (v != null) {
                                empty = false;
                                a.onNext(v);
                                inner.requestOne();
                                if (++e == r) {
                                    break middle;
                                }
                            }
                        }
                    }

                    if (d && empty) {
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    if (empty) {
                        break;
                    }
                }

                if (e == r) {
                    if (cancelled) {
                        cleanup();
                        return;
                    }

                    boolean d = done.get() == 0;

                    boolean empty = true;

                    for (JoinInnerSubscriber<T> inner : s) {
                        SimpleQueue<T> q = inner.queue;
                        if (q != null && !q.isEmpty()) {
                            empty = false;
                            break;
                        }
                    }

                    if (d && empty) {
                        errors.tryTerminateConsumer(a);
                        return;
                    }
                }

                if (e != 0) {
                    BackpressureHelper.produced(requested, e);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

    /** 内部 JoinInnerSubscriber。 */


    static final class JoinInnerSubscriber<T>
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<T> {

        @Serial
        private static final long serialVersionUID = 8410034718427740355L;

        final JoinSubscriptionBase<T> parent;

        final int prefetch;

        final int limit;

        long produced;

        volatile SimplePlainQueue<T> queue;

        JoinInnerSubscriber(JoinSubscriptionBase<T> parent, int prefetch) {
            this.parent = parent;
            this.prefetch = prefetch ;
            this.limit = prefetch - (prefetch >> 2);
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, prefetch);
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            parent.onNext(this, t);
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            parent.onError(t);
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            parent.onComplete();
        }

        public void requestOne() {
            long p = produced + 1;
            if (p == limit) {
                produced = 0;
                get().request(p);
            } else {
                produced = p;
            }
        }

        public void request(long n) {
            long p = produced + n;
            if (p >= limit) {
                produced = 0;
                get().request(p);
            } else {
                produced = p;
            }
        }

        public boolean cancel() {
            return SubscriptionHelper.cancel(this);
        }

        SimplePlainQueue<T> getQueue() {
            SimplePlainQueue<T> q = queue;
            if (q == null) {
                q = new SpscArrayQueue<>(prefetch);
                this.queue = q;
            }
            return q;
        }
    }
}
