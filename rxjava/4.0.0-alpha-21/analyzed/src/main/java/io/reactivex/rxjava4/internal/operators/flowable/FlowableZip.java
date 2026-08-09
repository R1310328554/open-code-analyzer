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

并行订阅多路 Publisher，在各路均有可用元素时调用 zipper 合并发射。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.QueueSubscription;
import io.reactivex.rxjava4.operators.SimpleQueue;
import io.reactivex.rxjava4.operators.SpscArrayQueue;

/* ===== [OCA 中文解析] =====
class FlowableZip — 意图说明

ZipCoordinator 协调多路 request/onNext 同步 zip。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * ZipCoordinator 协调多路 request/onNext 同步 zip。
 */
public final class FlowableZip<T, R> extends Flowable<R> {

    final Publisher<? extends T>[] sources;
    final Iterable<? extends Publisher<? extends T>> sourcesIterable;
    final Function<? super Object[], ? extends R> zipper;
    final int bufferSize;
    final boolean delayError;

    /**

     * 构造 FlowableZip。

     * @param int int 参数

     * @param boolean boolean 参数

     */

    public FlowableZip(Publisher<? extends T>[] sources,
            Iterable<? extends Publisher<? extends T>> sourcesIterable,
                    Function<? super Object[], ? extends R> zipper,
                    int bufferSize,
                    boolean delayError) {
        this.sources = sources;
        this.sourcesIterable = sourcesIterable;
        this.zipper = zipper;
        this.bufferSize = bufferSize;
        this.delayError = delayError;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void subscribeActual(Subscriber<? super R> s) {
        Publisher<? extends T>[] sources = this.sources;
        int count = 0;
        if (sources == null) {
            sources = new Publisher[8];
            for (Publisher<? extends T> p : sourcesIterable) {
                if (count == sources.length) {
                    Publisher<? extends T>[] b = new Publisher[count + (count >> 2)];
                    System.arraycopy(sources, 0, b, 0, count);
                    sources = b;
                }
                sources[count++] = p;
            }
        } else {
            count = sources.length;
        }

        if (count == 0) {
            EmptySubscription.complete(s);
            return;
        }

        ZipCoordinator<T, R> coordinator = new ZipCoordinator<>(s, zipper, count, bufferSize, delayError);

        s.onSubscribe(coordinator);

        coordinator.subscribe(sources, count);
    }

    /** 内部 ZipCoordinator。 */


    static final class ZipCoordinator<T, R>
    extends AtomicInteger
    implements Subscription {

        @Serial
        private static final long serialVersionUID = -2434867452883857743L;

        final Subscriber<? super R> downstream;

        final ZipSubscriber<T, R>[] subscribers;

        final Function<? super Object[], ? extends R> zipper;

        final AtomicLong requested;

        final AtomicThrowable errors;

        final boolean delayErrors;

        volatile boolean cancelled;

        final Object[] current;

        ZipCoordinator(Subscriber<? super R> actual,
                Function<? super Object[], ? extends R> zipper, int n, int prefetch, boolean delayErrors) {
            this.downstream = actual;
            this.zipper = zipper;
            this.delayErrors = delayErrors;
            @SuppressWarnings("unchecked")
            ZipSubscriber<T, R>[] a = new ZipSubscriber[n];
            for (int i = 0; i < n; i++) {
                a[i] = new ZipSubscriber<>(this, prefetch);
            }
            this.current = new Object[n];
            this.subscribers = a;
            this.requested = new AtomicLong();
            this.errors = new AtomicThrowable();
        }

        void subscribe(Publisher<? extends T>[] sources, int n) {
            ZipSubscriber<T, R>[] a = subscribers;
            for (int i = 0; i < n; i++) {
                if (cancelled || (!delayErrors && errors.get() != null)) {
                    return;
                }
                sources[i].subscribe(a[i]);
            }
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
            }
        }

        void error(ZipSubscriber<T, R> inner, Throwable e) {
            if (errors.tryAddThrowableOrReport(e)) {
                inner.done = true;
                drain();
            }
        }

        void cancelAll() {
            for (ZipSubscriber<T, R> s : subscribers) {
                s.cancel();
            }
        }

        /** drain 循环：按 request 从队列取元素发射。 */


        void drain() {

            if (getAndIncrement() != 0) {
                return;
            }

            final Subscriber<? super R> a = downstream;
            final ZipSubscriber<T, R>[] qs = subscribers;
            final int n = qs.length;
            Object[] values = current;

            int missed = 1;

            for (;;) {

                long r = requested.get();
                long e = 0L;

                while (r != e) {

                    if (cancelled) {
                        return;
                    }

                    if (!delayErrors && errors.get() != null) {
                        cancelAll();
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    boolean empty = false;

                    for (int j = 0; j < n; j++) {
                        ZipSubscriber<T, R> inner = qs[j];
                        if (values[j] == null) {
                            boolean d = inner.done;
                            SimpleQueue<T> q = inner.queue;
                            T v = null;
                            try {

                                v = q != null ? q.poll() : null;
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);

                                errors.tryAddThrowableOrReport(ex);
                                if (!delayErrors) {
                                    cancelAll();
                                    errors.tryTerminateConsumer(a);
                                    return;
                                }
                                d = true;
                            }

                            boolean sourceEmpty = v == null;
                            if (d && sourceEmpty) {
                                cancelAll();
                                errors.tryTerminateConsumer(a);
                                return;
                            }
                            if (!sourceEmpty) {
                                values[j] = v;
                            } else {
                                empty = true;
                            }
                        }
                    }

                    if (empty) {
                        break;
                    }

                    R v;

                    try {
                        v = Objects.requireNonNull(zipper.apply(values.clone()), "The zipper returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        cancelAll();
                        errors.tryAddThrowableOrReport(ex);
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    a.onNext(v);

                    e++;

                    Arrays.fill(values, null);
                }

                if (r == e) {
                    if (cancelled) {
                        return;
                    }

                    if (!delayErrors && errors.get() != null) {
                        cancelAll();
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    for (int j = 0; j < n; j++) {
                        ZipSubscriber<T, R> inner = qs[j];
                        if (values[j] == null) {
                            boolean d = inner.done;
                            SimpleQueue<T> q = inner.queue;
                            T v = null;
                            try {
                                v = q != null ? q.poll() : null;
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                errors.tryAddThrowableOrReport(ex);
                                if (!delayErrors) {
                                    cancelAll();
                                    errors.tryTerminateConsumer(a);
                                    return;
                                }
                                d = true;
                            }
                            boolean empty = v == null;
                            if (d && empty) {
                                cancelAll();
                                errors.tryTerminateConsumer(a);
                                return;
                            }
                            if (!empty) {
                                values[j] = v;
                            }
                        }
                    }

                }

                if (e != 0L) {

                    for (ZipSubscriber<T, R> inner : qs) {
                        inner.request(e);
                    }

                    if (r != Long.MAX_VALUE) {
                        requested.addAndGet(-e);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

    /** 内部 ZipSubscriber。 */


    static final class ZipSubscriber<T, R> extends AtomicReference<Subscription> implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -4627193790118206028L;

        final ZipCoordinator<T, R> parent;

        final int prefetch;

        final int limit;

        SimpleQueue<T> queue;

        long produced;

        volatile boolean done;

        int sourceMode;

        ZipSubscriber(ZipCoordinator<T, R> parent, int prefetch) {
            this.parent = parent;
            this.prefetch = prefetch;
            this.limit = prefetch - (prefetch >> 2);
        }

        @SuppressWarnings("unchecked")
        /** 校验 Subscription 并初始化内部状态。 */

        @Override

        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.setOnce(this, s)) {
                if (s instanceof QueueSubscription) {
                    QueueSubscription<T> f = (QueueSubscription<T>) s;

                    int m = f.requestFusion(QueueSubscription.ANY | QueueSubscription.BOUNDARY);

                    if (m == QueueSubscription.SYNC) {
                        sourceMode = m;
                        queue = f;
                        done = true;
                        parent.drain();
                        return;
                    }
                    if (m == QueueSubscription.ASYNC) {
                        sourceMode = m;
                        queue = f;
                        s.request(prefetch);
                        return;
                    }
                }

                queue = new SpscArrayQueue<>(prefetch);

                s.request(prefetch);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            if (sourceMode != QueueSubscription.ASYNC) {
                queue.offer(t);
            }
            parent.drain();
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            parent.error(this, t);
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            done = true;
            parent.drain();
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            SubscriptionHelper.cancel(this);
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (sourceMode != QueueSubscription.SYNC) {
                long p = produced + n;
                if (p >= limit) {
                    produced = 0L;
                    get().request(p);
                } else {
                    produced = p;
                }
            }
        }
    }
}
