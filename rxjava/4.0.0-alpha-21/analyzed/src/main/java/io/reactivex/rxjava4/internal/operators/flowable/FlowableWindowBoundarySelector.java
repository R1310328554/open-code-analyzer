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

由 open 信号开启窗口，closingIndicator 返回的 Publisher 完成时关闭窗口。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.queue.MpscLinkedQueue;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SimplePlainQueue;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.processors.UnicastProcessor;

/* ===== [OCA 中文解析] =====
class FlowableWindowBoundarySelector — 意图说明

WindowBoundaryMainSubscriber 管理开/关边界与 UnicastProcessor。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * WindowBoundaryMainSubscriber 管理开/关边界与 UnicastProcessor。
 */
public final class FlowableWindowBoundarySelector<T, B, V> extends AbstractFlowableWithUpstream<T, Flowable<T>> {
    final Publisher<B> open;
    final Function<? super B, ? extends Publisher<V>> closingIndicator;
    final int bufferSize;

    /**

     * 构造 FlowableWindowBoundarySelector。

     * @param int int 参数

     */

    public FlowableWindowBoundarySelector(
            Flowable<T> source,
            Publisher<B> open, Function<? super B, ? extends Publisher<V>> closingIndicator,
            int bufferSize) {
        super(source);
        this.open = open;
        this.closingIndicator = closingIndicator;
        this.bufferSize = bufferSize;
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(Subscriber<? super Flowable<T>> s) {
        source.subscribe(new WindowBoundaryMainSubscriber<>(
                s, open, closingIndicator, bufferSize));
    }

    /** 内部 WindowBoundaryMainSubscriber。 */


    static final class WindowBoundaryMainSubscriber<T, B, V>
    extends AtomicInteger
    implements FlowableSubscriber<T>, Subscription, Runnable {
        @Serial
        private static final long serialVersionUID = 8646217640096099753L;

        final Subscriber<? super Flowable<T>> downstream;
        final Publisher<B> open;
        final Function<? super B, ? extends Publisher<V>> closingIndicator;
        final int bufferSize;
        final CompositeDisposable resources;

        final WindowStartSubscriber<B> startSubscriber;

        final List<UnicastProcessor<T>> windows;

        final SimplePlainQueue<Object> queue;

        final AtomicLong windowCount;

        final AtomicBoolean downstreamCancelled;

        final AtomicLong requested;
        long emitted;

        volatile boolean upstreamCancelled;

        volatile boolean upstreamDone;
        volatile boolean openDone;
        final AtomicThrowable error;

        Subscription upstream;

        WindowBoundaryMainSubscriber(Subscriber<? super Flowable<T>> actual,
                Publisher<B> open, Function<? super B, ? extends Publisher<V>> closingIndicator, int bufferSize) {
            this.downstream = actual;
            this.queue = new MpscLinkedQueue<>();
            this.open = open;
            this.closingIndicator = closingIndicator;
            this.bufferSize = bufferSize;
            this.resources = new CompositeDisposable();
            this.windows = new ArrayList<>();
            this.windowCount = new AtomicLong(1L);
            this.downstreamCancelled = new AtomicBoolean();
            this.error = new AtomicThrowable();
            this.startSubscriber = new WindowStartSubscriber<>(this);
            this.requested = new AtomicLong();
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                open.subscribe(startSubscriber);

                s.request(Long.MAX_VALUE);
            }
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(T t) {
            queue.offer(t);
            drain();
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            startSubscriber.cancel();
            resources.dispose();
            if (error.tryAddThrowableOrReport(t)) {
                upstreamDone = true;
                drain();
            }
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            startSubscriber.cancel();
            resources.dispose();
            upstreamDone = true;
            drain();
        }

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
            }
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            if (downstreamCancelled.compareAndSet(false, true)) {
                if (windowCount.decrementAndGet() == 0) {
                    upstream.cancel();
                    startSubscriber.cancel();
                    resources.dispose();
                    error.tryTerminateAndReport();
                    upstreamCancelled = true;
                    drain();
                } else {
                    startSubscriber.cancel();
                }
            }
        }

        @Override
        public void run() {
            if (windowCount.decrementAndGet() == 0) {
                upstream.cancel();
                startSubscriber.cancel();
                resources.dispose();
                error.tryTerminateAndReport();
                upstreamCancelled = true;
                drain();
            }
        }

        void open(B startValue) {
            queue.offer(new WindowStartItem<>(startValue));
            drain();
        }

        void openError(Throwable t) {
            upstream.cancel();
            resources.dispose();
            if (error.tryAddThrowableOrReport(t)) {
                upstreamDone = true;
                drain();
            }
        }

        void openComplete() {
            openDone = true;
            drain();
        }

        void close(WindowEndSubscriberIntercept<T, V> what) {
            queue.offer(what);
            drain();
        }

        void closeError(Throwable t) {
            upstream.cancel();
            startSubscriber.cancel();
            resources.dispose();
            if (error.tryAddThrowableOrReport(t)) {
                upstreamDone = true;
                drain();
            }
        }

        /** drain 循环：按 request 从队列取元素发射。 */


        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            final Subscriber<? super Flowable<T>> downstream = this.downstream;
            final SimplePlainQueue<Object> queue = this.queue;
            final List<UnicastProcessor<T>> windows = this.windows;

            for (;;) {
                if (upstreamCancelled) {
                    queue.clear();
                    windows.clear();
                } else {
                    boolean isDone = upstreamDone;
                    Object o = queue.poll();
                    boolean isEmpty = o == null;

                    if (isDone) {
                        if (isEmpty || error.get() != null) {
                            terminateDownstream(downstream);
                            upstreamCancelled = true;
                            continue;
                        }
                    }

                    if (!isEmpty) {
                        if (o instanceof WindowStartItem) {
                            if (!downstreamCancelled.get()) {
                                long emitted = this.emitted;
                                if (requested.get() != emitted) {
                                    this.emitted = ++emitted;

                                    @SuppressWarnings("unchecked")
                                    B startItem = ((WindowStartItem<B>)o).item;

                                    Publisher<V> endSource;
                                    try {
                                        endSource = Objects.requireNonNull(closingIndicator.apply(startItem), "The closingIndicator returned a null Publisher");
                                    } catch (Throwable ex) {
                                        Exceptions.throwIfFatal(ex);
                                        upstream.cancel();
                                        startSubscriber.cancel();
                                        resources.dispose();
                                        Exceptions.throwIfFatal(ex);
                                        error.tryAddThrowableOrReport(ex);
                                        upstreamDone = true;
                                        continue;
                                    }

                                    windowCount.getAndIncrement();
                                    UnicastProcessor<T> newWindow = UnicastProcessor.create(bufferSize, this);
                                    WindowEndSubscriberIntercept<T, V> endSubscriber = new WindowEndSubscriberIntercept<>(this, newWindow);

                                    downstream.onNext(endSubscriber);

                                    if (endSubscriber.tryAbandon()) {
                                        newWindow.onComplete();
                                    } else {
                                        windows.add(newWindow);
                                        resources.add(endSubscriber);
                                        endSource.subscribe(endSubscriber);
                                    }
                                } else {
                                    upstream.cancel();
                                    startSubscriber.cancel();
                                    resources.dispose();
                                    error.tryAddThrowableOrReport(FlowableWindowTimed.missingBackpressureMessage(emitted));
                                    upstreamDone = true;
                                }
                            }
                        }
                        else if (o instanceof WindowEndSubscriberIntercept) {
                            @SuppressWarnings("unchecked")
                            UnicastProcessor<T> w = ((WindowEndSubscriberIntercept<T, V>)o).window;

                            windows.remove(w);
                            resources.delete((Disposable)o);
                            w.onComplete();
                        } else {
                            @SuppressWarnings("unchecked")
                            T item = (T)o;

                            for (UnicastProcessor<T> w : windows) {
                                w.onNext(item);
                            }
                        }

                        continue;
                    }
                    else if (openDone && windows.isEmpty()) {
                        upstream.cancel();
                        startSubscriber.cancel();
                        resources.dispose();
                        terminateDownstream(downstream);
                        upstreamCancelled = true;
                        continue;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        void terminateDownstream(Subscriber<?> downstream) {
            Throwable ex = error.terminate();
            if (ex == null) {
                for (UnicastProcessor<T> w : windows) {
                    w.onComplete();
                }
                downstream.onComplete();
            } else if (ex != ExceptionHelper.TERMINATED) {
                for (UnicastProcessor<T> w : windows) {
                    w.onError(ex);
                }
                downstream.onError(ex);
            }
        }

        record WindowStartItem<B>(B item) {

        }

        /** 内部 WindowStartSubscriber。 */


        static final class WindowStartSubscriber<B> extends AtomicReference<Subscription>
        implements FlowableSubscriber<B> {

            @Serial
            private static final long serialVersionUID = -3326496781427702834L;

            final WindowBoundaryMainSubscriber<?, B, ?> parent;

            WindowStartSubscriber(WindowBoundaryMainSubscriber<?, B, ?> parent) {
                this.parent = parent;
            }

            /** 校验 Subscription 并初始化内部状态。 */


            @Override


            public void onSubscribe(Subscription s) {
                if (SubscriptionHelper.setOnce(this, s)) {
                    s.request(Long.MAX_VALUE);
                }
            }

            /** 处理上游 onNext 并转发或缓存。 */


            @Override


            public void onNext(B t) {
                parent.open(t);
            }

            /** 处理上游/onError 并按策略终止或延迟错误。 */


            @Override


            public void onError(Throwable t) {
                parent.openError(t);
            }

            /** 上游完成：清理资源并向下游发送 onComplete。 */


            @Override


            public void onComplete() {
                parent.openComplete();
            }

            void cancel() {
                SubscriptionHelper.cancel(this);
            }
        }

        /** 内部 WindowEndSubscriberIntercept。 */


        static final class WindowEndSubscriberIntercept<T, V> extends Flowable<T>
        implements FlowableSubscriber<V>, Disposable {

            final WindowBoundaryMainSubscriber<T, ?, V> parent;

            final UnicastProcessor<T> window;

            final AtomicReference<Subscription> upstream;

            final AtomicBoolean once;

            WindowEndSubscriberIntercept(WindowBoundaryMainSubscriber<T, ?, V> parent, UnicastProcessor<T> window) {
                this.parent = parent;
                this.window = window;
                this.upstream = new AtomicReference<>();
                this.once = new AtomicBoolean();
            }

            /** 校验 Subscription 并初始化内部状态。 */


            @Override


            public void onSubscribe(Subscription s) {
                if (SubscriptionHelper.setOnce(upstream, s)) {
                    s.request(Long.MAX_VALUE);
                }
            }

            /** 处理上游 onNext 并转发或缓存。 */


            @Override


            public void onNext(V t) {
                if (SubscriptionHelper.cancel(upstream)) {
                    parent.close(this);
                }
            }

            /** 处理上游/onError 并按策略终止或延迟错误。 */


            @Override


            public void onError(Throwable t) {
                if (isDisposed()) {
                    RxJavaPlugins.onError(t);
                } else {
                    parent.closeError(t);
                }
            }

            /** 上游完成：清理资源并向下游发送 onComplete。 */


            @Override


            public void onComplete() {
                parent.close(this);
            }

            /** dispose 连接/inner 并清理状态。 */


            @Override


            public void dispose() {
                SubscriptionHelper.cancel(upstream);
            }

            /** 返回是否已 dispose。 */


            @Override


            public boolean isDisposed() {
                return upstream.get() == SubscriptionHelper.CANCELLED;
            }

            /** 组装内部 Subscriber/Observer 并订阅上游。 */


            @Override


            protected void subscribeActual(Subscriber<? super T> s) {
                window.subscribe(s);
                once.set(true);
            }

            boolean tryAbandon() {
                return !once.get() && once.compareAndSet(false, true);
            }
        }
    }

}
