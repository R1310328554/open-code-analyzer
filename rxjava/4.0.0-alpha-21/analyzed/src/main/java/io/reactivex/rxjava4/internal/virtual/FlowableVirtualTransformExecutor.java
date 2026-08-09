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

package io.reactivex.rxjava4.internal.virtual;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.Flow.*;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SpscArrayQueue;

/**
 * 在虚拟线程上对上游 Flowable 逐元素调用 {@link VirtualTransformer}，
 * 用 SPSC 队列缓冲上游，双 VirtualResumable 协调生产/消费背压。
 *
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 */
public final class FlowableVirtualTransformExecutor<T, R> extends Flowable<R> {

    final Flowable<T> source;

    final VirtualTransformer<T, R> transformer;

    final ExecutorService executor;

    final Scheduler scheduler;

    final int prefetch;

    /**
     * @param source 上游 Flowable
     * @param transformer 逐元素虚拟变换
     * @param executor 可选执行器
     * @param scheduler 无 executor 时的 Worker 来源
     * @param prefetch 上游预取与队列容量
     */
    public FlowableVirtualTransformExecutor(Flowable<T> source,
            VirtualTransformer<T, R> transformer,
            ExecutorService executor,
            Scheduler scheduler,
            int prefetch) {
        this.source = source;
        this.transformer = transformer;
        this.executor = executor;
        this.scheduler = scheduler;
        this.prefetch = prefetch;
    }

    /** 订阅上游并 submit/schedule 变换循环。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        var parent = new ExecutorVirtualTransformSubscriber<>(s, transformer, prefetch);
        source.subscribe(parent);
        if (executor != null) {
            executor.submit((Callable<Void>)parent);
        } else {
            var worker = scheduler.createWorker();
            parent.worker = worker;
            worker.schedule(parent);
        }
    }

    static final class ExecutorVirtualTransformSubscriber<T, R> extends AtomicLong
    implements FlowableSubscriber<T>, Subscription, VirtualEmitter<R>, Callable<Void>, Runnable, Disposable {

        @Serial
        private static final long serialVersionUID = -4702456711290258571L;

        Subscriber<? super R> downstream;

        final VirtualTransformer<T, R> transformer;

        final int prefetch;

        final AtomicLong requested;

        final VirtualResumable producerReady;

        final VirtualResumable consumerReady;

        final SpscArrayQueue<T> queue;

        Subscription upstream;

        volatile boolean done;
        Throwable error;

        volatile boolean cancelled;

        /** cancel 时 emit 抛出的哨兵。 */
        static final Throwable STOP = new Throwable("Downstream cancelled");

        long produced;

        Worker worker;

        final DisposableStreamerCancellation canceller;

        volatile boolean stopped;

        ExecutorVirtualTransformSubscriber(Subscriber<? super R> downstream,
                VirtualTransformer<T, R> transformer,
                int prefetch) {
            this.downstream = downstream;
            this.transformer = transformer;
            this.prefetch = prefetch;
            this.requested = new AtomicLong();
            this.producerReady = new VirtualResumable();
            this.consumerReady = new VirtualResumable();
            this.queue = new SpscArrayQueue<>(prefetch);
            this.canceller = new CompositeDisposable();
        }

        /** 保存 upstream、向下游 onSubscribe(this)、request(prefetch)。 */
        @Override
        public void onSubscribe(Subscription s) {
            upstream = s;
            downstream.onSubscribe(this);
            s.request(prefetch);
        }

        /** 入队 SpscArrayQueue，首次 wip 时 resume 生产线程。 */
        @Override
        public void onNext(T t) {
            queue.offer(t);
            if (getAndIncrement() == 0) {
                producerReady.resume();
            }
        }

        @Override
        public void onError(Throwable t) {
            error = t;
            onComplete();
        }

        @Override
        public void onComplete() {
            done = true;
            if (getAndIncrement() == 0) {
                producerReady.resume();
            }
        }

        /** 等待 requested>produced 后 downstream.onNext。 */
        @Override
        public void emit(R item) throws Throwable {
            Objects.requireNonNull(item, "item is null");

            var p = produced;
            while (requested.get() == p && !cancelled) {
                consumerReady.await();
            }

            if (cancelled) {
                throw STOP;
            }

            downstream.onNext(item);

            produced = p + 1;
        }

        @Override
        public void request(long n) {
            BackpressureHelper.add(requested, n);
            consumerReady.resume();
        }

        /** cancel 上游、dispose canceller/worker，双端 resume 唤醒 park。 */
        @Override
        public void cancel() {
            try {
                cancelled = true;
                upstream.cancel();
                canceller.dispose();
                // cleanup(); don't kill the worker

                var w = worker;
                worker = null;
                if (w != null) {
                    w.dispose();
                }
            } finally {
                producerReady.resume();
                consumerReady.resume();
            }
        }

        @Override
        public void run() {
            call();
        }

        /** 主循环：poll 上游元素、transformer.transform、75% prefetch 时再 request。 */
        @Override
        public Void call() {
            try {
                try {
                    var consumed = 0L;
                    var limit = prefetch - (prefetch >> 2);
                    var wip = 0L;

                    while (!cancelled) {
                        var d = done;
                        var v = queue.poll();
                        var empty = v == null;

                        if (d && empty) {
                            var ex = error;
                            if (ex != null) {
                                downstream.onError(ExceptionHelper.unwrap(ex));
                            } else {
                                downstream.onComplete();
                            }
                            break;
                        }

                        if (!empty) {

                            if (++consumed == limit) {
                                consumed = 0;
                                upstream.request(limit);
                            }

                            transformer.transform(v, this, this);

                            continue;
                        }

                        wip = addAndGet(-wip);
                        if (wip == 0L) {
                            producerReady.await();
                        }
                    }
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    if (ex != STOP && !cancelled) {
                        upstream.cancel();
                        downstream.onError(ExceptionHelper.unwrap(ex));
                    }
                    return null;
                }
            } finally {
                queue.clear();
                downstream = null;
            }
            return null;
        }

        @Override
        public DisposableStreamerCancellation canceller() {
            return canceller;
        }

        @Override
        public void dispose() {
            stopped = true;
            upstream.cancel();
        }

        @Override
        public boolean isDisposed() {
            return stopped;
        }
    }
}