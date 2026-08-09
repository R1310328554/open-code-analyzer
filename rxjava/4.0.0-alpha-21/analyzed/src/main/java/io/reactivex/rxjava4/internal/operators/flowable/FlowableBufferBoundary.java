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

由 boundary Publisher 的开/关信号界定缓冲窗口，窗口关闭时将 Collection 批量发射。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/* ===== [OCA 中文解析] =====
class FlowableBufferBoundary — 意图说明

BoundarySubscriber 管理开/关边界与 buffer 发射。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * BoundarySubscriber 管理开/关边界与 buffer 发射。
 */
public final class FlowableBufferBoundary<T, U extends Collection<? super T>, Open, Close>
extends AbstractFlowableWithUpstream<T, U> {
    final Supplier<U> bufferSupplier;
    final Publisher<? extends Open> bufferOpen;
    final Function<? super Open, ? extends Publisher<? extends Close>> bufferClose;

    public FlowableBufferBoundary(Flowable<T> source, Publisher<? extends Open> bufferOpen,
            Function<? super Open, ? extends Publisher<? extends Close>> bufferClose, Supplier<U> bufferSupplier) {
        super(source);
        this.bufferOpen = bufferOpen;
        this.bufferClose = bufferClose;
        this.bufferSupplier = bufferSupplier;
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(Subscriber<? super U> s) {
        BufferBoundarySubscriber<T, U, Open, Close> parent =
            new BufferBoundarySubscriber<>(
                s, bufferOpen, bufferClose, bufferSupplier
            );
        s.onSubscribe(parent);
        source.subscribe(parent);
    }

    /** 内部 BufferBoundarySubscriber。 */


    static final class BufferBoundarySubscriber<T, C extends Collection<? super T>, Open, Close>
    extends AtomicInteger implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -8466418554264089604L;

        final Subscriber<? super C> downstream;

        final Supplier<C> bufferSupplier;

        final Publisher<? extends Open> bufferOpen;

        final Function<? super Open, ? extends Publisher<? extends Close>> bufferClose;

        final CompositeDisposable subscribers;

        final AtomicLong requested;

        final AtomicReference<Subscription> upstream;

        final AtomicThrowable errors;

        volatile boolean done;

        final SpscLinkedArrayQueue<C> queue;

        volatile boolean cancelled;

        long index;

        Map<Long, C> buffers;

        long emitted;

        BufferBoundarySubscriber(Subscriber<? super C> actual,
                Publisher<? extends Open> bufferOpen,
                Function<? super Open, ? extends Publisher<? extends Close>> bufferClose,
                Supplier<C> bufferSupplier
        ) {
            this.downstream = actual;
            this.bufferSupplier = bufferSupplier;
            this.bufferOpen = bufferOpen;
            this.bufferClose = bufferClose;
            this.queue = new SpscLinkedArrayQueue<>(bufferSize());
            this.subscribers = new CompositeDisposable();
            this.requested = new AtomicLong();
            this.upstream = new AtomicReference<>();
            this.buffers = new LinkedHashMap<>();
            this.errors = new AtomicThrowable();
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.setOnce(this.upstream, s)) {

                BufferOpenSubscriber<Open> open = new BufferOpenSubscriber<>(this);
                subscribers.add(open);

                bufferOpen.subscribe(open);

                s.request(Long.MAX_VALUE);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            synchronized (this) {
                Map<Long, C> bufs = buffers;
                if (bufs == null) {
                    return;
                }
                for (C b : bufs.values()) {
                    b.add(t);
                }
            }
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            if (errors.tryAddThrowableOrReport(t)) {
                subscribers.dispose();
                synchronized (this) {
                    buffers = null;
                }
                done = true;
                drain();
            }
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            subscribers.dispose();
            synchronized (this) {
                Map<Long, C> bufs = buffers;
                if (bufs == null) {
                    return;
                }
                for (C b : bufs.values()) {
                    queue.offer(b);
                }
                buffers = null;
            }
            done = true;
            drain();
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            BackpressureHelper.add(requested, n);
            drain();
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            if (SubscriptionHelper.cancel(upstream)) {
                cancelled = true;
                subscribers.dispose();
                synchronized (this) {
                    buffers = null;
                }
                if (getAndIncrement() != 0) {
                    queue.clear();
                }
            }
        }

        void open(Open token) {
            Publisher<? extends Close> p;
            C buf;
            try {
                buf = Objects.requireNonNull(bufferSupplier.get(), "The bufferSupplier returned a null Collection");
                p = Objects.requireNonNull(bufferClose.apply(token), "The bufferClose returned a null Publisher");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                SubscriptionHelper.cancel(upstream);
                onError(ex);
                return;
            }

            long idx = index;
            index = idx + 1;
            synchronized (this) {
                Map<Long, C> bufs = buffers;
                if (bufs == null) {
                    return;
                }
                bufs.put(idx, buf);
            }

            BufferCloseSubscriber<T, C> bc = new BufferCloseSubscriber<>(this, idx);
            subscribers.add(bc);
            p.subscribe(bc);
        }

        void openComplete(BufferOpenSubscriber<Open> os) {
            subscribers.delete(os);
            if (subscribers.size() == 0) {
                SubscriptionHelper.cancel(upstream);
                done = true;
                drain();
            }
        }

        void close(BufferCloseSubscriber<T, C> closer, long idx) {
            subscribers.delete(closer);
            boolean makeDone = false;
            if (subscribers.size() == 0) {
                makeDone = true;
                SubscriptionHelper.cancel(upstream);
            }
            synchronized (this) {
                Map<Long, C> bufs = buffers;
                if (bufs == null) {
                    return;
                }
                queue.offer(buffers.remove(idx));
            }
            if (makeDone) {
                done = true;
            }
            drain();
        }

        void boundaryError(Disposable subscriber, Throwable ex) {
            SubscriptionHelper.cancel(upstream);
            subscribers.delete(subscriber);
            onError(ex);
        }

        /** drain 循环：按 request 从队列取元素发射。 */


        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            long e = emitted;
            Subscriber<? super C> a = downstream;
            SpscLinkedArrayQueue<C> q = queue;

            for (;;) {
                long r = requested.get();

                while (e != r) {
                    if (cancelled) {
                        q.clear();
                        return;
                    }

                    boolean d = done;
                    if (d && errors.get() != null) {
                        q.clear();
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    C v = q.poll();
                    boolean empty = v == null;

                    if (d && empty) {
                        a.onComplete();
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
                        q.clear();
                        return;
                    }

                    if (done) {
                        if (errors.get() != null) {
                            q.clear();
                            errors.tryTerminateConsumer(a);
                            return;
                        } else if (q.isEmpty()) {
                            a.onComplete();
                            return;
                        }
                    }
                }

                emitted = e;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /** 内部 BufferOpenSubscriber。 */


        static final class BufferOpenSubscriber<Open>
        extends AtomicReference<Subscription>
        implements FlowableSubscriber<Open>, Disposable {

            @Serial
            private static final long serialVersionUID = -8498650778633225126L;

            final BufferBoundarySubscriber<?, ?, Open, ?> parent;

            BufferOpenSubscriber(BufferBoundarySubscriber<?, ?, Open, ?> parent) {
                this.parent = parent;
            }

            /** 校验 Subscription 并初始化内部状态。 */


            @Override


            public void onSubscribe(Subscription s) {
                SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
            }

            /** 处理上游 onNext 并转发或缓存。 */


            @Override


            public void onNext(Open t) {
                parent.open(t);
            }

            /** 处理上游/onError 并按策略终止或延迟错误。 */


            @Override


            public void onError(Throwable t) {
                lazySet(SubscriptionHelper.CANCELLED);
                parent.boundaryError(this, t);
            }

            /** 上游完成：清理资源并向下游发送 onComplete。 */


            @Override


            public void onComplete() {
                lazySet(SubscriptionHelper.CANCELLED);
                parent.openComplete(this);
            }

            /** dispose 连接/inner 并清理状态。 */


            @Override


            public void dispose() {
                SubscriptionHelper.cancel(this);
            }

            /** 返回是否已 dispose。 */


            @Override


            public boolean isDisposed() {
                return get() == SubscriptionHelper.CANCELLED;
            }
        }
    }

    /** 内部 BufferCloseSubscriber。 */


    static final class BufferCloseSubscriber<T, C extends Collection<? super T>>
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<Object>, Disposable {

        @Serial
        private static final long serialVersionUID = -8498650778633225126L;

        final BufferBoundarySubscriber<T, C, ?, ?> parent;

        final long index;

        BufferCloseSubscriber(BufferBoundarySubscriber<T, C, ?, ?> parent, long index) {
            this.parent = parent;
            this.index = index;
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(Object t) {
            Subscription s = get();
            if (s != SubscriptionHelper.CANCELLED) {
                lazySet(SubscriptionHelper.CANCELLED);
                s.cancel();
                parent.close(this, index);
            }
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            if (get() != SubscriptionHelper.CANCELLED) {
                lazySet(SubscriptionHelper.CANCELLED);
                parent.boundaryError(this, t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            if (get() != SubscriptionHelper.CANCELLED) {
                lazySet(SubscriptionHelper.CANCELLED);
                parent.close(this, index);
            }
        }

        /** dispose 连接/inner 并清理状态。 */


        @Override


        public void dispose() {
            SubscriptionHelper.cancel(this);
        }

        /** 返回是否已 dispose。 */


        @Override


        public boolean isDisposed() {
            return get() == SubscriptionHelper.CANCELLED;
        }
    }
}
