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

按固定计数 size 或 skip 将上游元素收集到 Collection 后批量发射。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/* ===== [OCA 中文解析] =====
class FlowableBuffer — 意图说明

BufferExact/Skip/Overlap 等缓冲订阅者实现。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * BufferExact/Skip/Overlap 等缓冲订阅者实现。
 */
public final class FlowableBuffer<T, C extends Collection<? super T>> extends AbstractFlowableWithUpstream<T, C> {
    final int size;

    final int skip;

    final Supplier<C> bufferSupplier;

    public FlowableBuffer(Flowable<T> source, int size, int skip, Supplier<C> bufferSupplier) {
        super(source);
        this.size = size;
        this.skip = skip;
        this.bufferSupplier = bufferSupplier;
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    public void subscribeActual(Subscriber<? super C> s) {
        if (size == skip) {
            source.subscribe(new PublisherBufferExactSubscriber<>(s, size, bufferSupplier));
        } else if (skip > size) {
            source.subscribe(new PublisherBufferSkipSubscriber<>(s, size, skip, bufferSupplier));
        } else {
            source.subscribe(new PublisherBufferOverlappingSubscriber<>(s, size, skip, bufferSupplier));
        }
    }

    /** 内部 PublisherBufferExactSubscriber。 */


    static final class PublisherBufferExactSubscriber<T, C extends Collection<? super T>>
    implements FlowableSubscriber<T>, Subscription {

        final Subscriber<? super C> downstream;

        final Supplier<C> bufferSupplier;

        final int size;

        C buffer;

        Subscription upstream;

        boolean done;

        int index;

        PublisherBufferExactSubscriber(Subscriber<? super C> actual, int size, Supplier<C> bufferSupplier) {
            this.downstream = actual;
            this.size = size;
            this.bufferSupplier = bufferSupplier;
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                upstream.request(BackpressureHelper.multiplyCap(n, size));
            }
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            upstream.cancel();
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            if (done) {
                return;
            }

            C b = buffer;
            if (b == null) {

                try {
                    b = Objects.requireNonNull(bufferSupplier.get(), "The bufferSupplier returned a null buffer");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    onError(e);
                    return;
                }

                buffer = b;
            }

            b.add(t);

            int i = index + 1;
            if (i == size) {
                index = 0;
                buffer = null;
                downstream.onNext(b);
            } else {
                index = i;
            }
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            buffer = null;
            done = true;
            downstream.onError(t);
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            if (done) {
                return;
            }
            done = true;

            C b = buffer;
            buffer = null;

            if (b != null) {
                downstream.onNext(b);
            }
            downstream.onComplete();
        }
    }

    /** 内部 PublisherBufferSkipSubscriber。 */


    static final class PublisherBufferSkipSubscriber<T, C extends Collection<? super T>>
    extends AtomicInteger
    implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -5616169793639412593L;

        final Subscriber<? super C> downstream;

        final Supplier<C> bufferSupplier;

        final int size;

        final int skip;

        C buffer;

        Subscription upstream;

        boolean done;

        int index;

        PublisherBufferSkipSubscriber(Subscriber<? super C> actual, int size, int skip,
                Supplier<C> bufferSupplier) {
            this.downstream = actual;
            this.size = size;
            this.skip = skip;
            this.bufferSupplier = bufferSupplier;
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                if (get() == 0 && compareAndSet(0, 1)) {
                    // n full buffers
                    long u = BackpressureHelper.multiplyCap(n, size);
                    // + (n - 1) gaps
                    long v = BackpressureHelper.multiplyCap(skip - size, n - 1);

                    upstream.request(BackpressureHelper.addCap(u, v));
                } else {
                    // n full buffer + gap
                    upstream.request(BackpressureHelper.multiplyCap(skip, n));
                }
            }
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            upstream.cancel();
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            if (done) {
                return;
            }

            C b = buffer;

            int i = index;

            if (i++ == 0) {
                try {
                    b = Objects.requireNonNull(bufferSupplier.get(), "The bufferSupplier returned a null buffer");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();

                    onError(e);
                    return;
                }

                buffer = b;
            }

            if (b != null) {
                b.add(t);
                if (b.size() == size) {
                    buffer = null;
                    downstream.onNext(b);
                }
            }

            if (i == skip) {
                i = 0;
            }
            index = i;
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }

            done = true;
            buffer = null;

            downstream.onError(t);
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            if (done) {
                return;
            }

            done = true;
            C b = buffer;
            buffer = null;

            if (b != null) {
                downstream.onNext(b);
            }

            downstream.onComplete();
        }
    }

    /** 内部 PublisherBufferOverlappingSubscriber。 */


    static final class PublisherBufferOverlappingSubscriber<T, C extends Collection<? super T>>
    extends AtomicLong
    implements FlowableSubscriber<T>, Subscription, BooleanSupplier {

        @Serial
        private static final long serialVersionUID = -7370244972039324525L;

        final Subscriber<? super C> downstream;

        final Supplier<C> bufferSupplier;

        final int size;

        final int skip;

        final ArrayDeque<C> buffers;

        final AtomicBoolean once;

        Subscription upstream;

        boolean done;

        int index;

        volatile boolean cancelled;

        long produced;

        PublisherBufferOverlappingSubscriber(Subscriber<? super C> actual, int size, int skip,
                Supplier<C> bufferSupplier) {
            this.downstream = actual;
            this.size = size;
            this.skip = skip;
            this.bufferSupplier = bufferSupplier;
            this.once = new AtomicBoolean();
            this.buffers = new ArrayDeque<>();
        }

        @Override
        public boolean getAsBoolean() {
            return cancelled;
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                if (QueueDrainHelper.postCompleteRequest(n, downstream, buffers, this, this)) {
                    return;
                }

                if (!once.get() && once.compareAndSet(false, true)) {
                    // (n - 1) skips
                    long u = BackpressureHelper.multiplyCap(skip, n - 1);

                    // + 1 full buffer
                    long r = BackpressureHelper.addCap(size, u);
                    upstream.request(r);
                } else {
                    // n skips
                    long r = BackpressureHelper.multiplyCap(skip, n);
                    upstream.request(r);
                }
            }
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            cancelled = true;
            upstream.cancel();
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            if (done) {
                return;
            }

            ArrayDeque<C> bs = buffers;

            int i = index;

            if (i++ == 0) {
                C b;

                try {
                    b = Objects.requireNonNull(bufferSupplier.get(), "The bufferSupplier returned a null buffer");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    onError(e);
                    return;
                }

                bs.offer(b);
            }

            C b = bs.peek();

            if (b.size() + 1 == size) {
                bs.poll();

                b.add(t);

                produced++;

                downstream.onNext(b);
            }

            for (C b0 : bs) {
                b0.add(t);
            }

            if (i == skip) {
                i = 0;
            }
            index = i;
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }

            done = true;
            buffers.clear();

            downstream.onError(t);
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            if (done) {
                return;
            }

            done = true;

            long p = produced;
            if (p != 0L) {
                BackpressureHelper.produced(this, p);
            }
            QueueDrainHelper.postComplete(downstream, buffers, this, this);
        }
    }
}
