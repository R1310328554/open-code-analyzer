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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;
import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.MissingBackpressureException;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.subscribers.SerializedSubscriber;

/**
 * 在指定静默间隔内无新元素时才向下游发射最后一项；新元素到来会重置计时器。
 * 被丢弃的值可通过 {@code onDropped} 回调处理。
 * @param <T> 上游元素类型
 */
public final class FlowableDebounceTimed<T> extends AbstractFlowableWithUpstream<T, T> {
    final long timeout;
    final TimeUnit unit;
    final Scheduler scheduler;
    final Consumer<? super T> onDropped;

    /**
     * @param source 上游 Flowable
     * @param timeout 静默超时量
     * @param unit 时间单位
     * @param scheduler 调度计时任务的 Scheduler
     * @param onDropped 被 debounce 丢弃元素时的回调（可为 null）
     */
    public FlowableDebounceTimed(Flowable<T> source, long timeout, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {
        super(source);
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
        this.onDropped = onDropped;
    }

    /** 以 SerializedSubscriber 包装下游并订阅 DebounceTimedSubscriber。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new DebounceTimedSubscriber<>(
                new SerializedSubscriber<>(s), timeout, unit, scheduler.createWorker(), onDropped));
    }

    static final class DebounceTimedSubscriber<T> extends AtomicLong
    implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -9102637559663639004L;
        final Subscriber<? super T> downstream;
        final long timeout;
        final TimeUnit unit;
        final Scheduler.Worker worker;
        final Consumer<? super T> onDropped;

        Subscription upstream;

        DebounceEmitter<T> timer;

        volatile long index;

        boolean done;

        DebounceTimedSubscriber(Subscriber<? super T> actual, long timeout, TimeUnit unit, Worker worker, Consumer<? super T> onDropped) {
            this.downstream = actual;
            this.timeout = timeout;
            this.unit = unit;
            this.worker = worker;
            this.onDropped = onDropped;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        /** 递增 index、取消旧计时器，必要时调用 onDropped，再调度新 DebounceEmitter。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            long idx = index + 1;
            index = idx;

            DebounceEmitter<T> currentEmitter = timer;
            if (currentEmitter != null) {
                currentEmitter.dispose();
            }

            if (onDropped != null && currentEmitter != null) {
                try {
                    onDropped.accept(currentEmitter.value);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.cancel();
                    done = true;
                    downstream.onError(ex);
                    worker.dispose();
                }
            }

            DebounceEmitter<T> newEmitter = new DebounceEmitter<>(t, idx, this);
            timer = newEmitter;
            newEmitter.setResource(worker.schedule(newEmitter, timeout, unit));
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            Disposable d = timer;
            if (d != null) {
                d.dispose();
            }
            downstream.onError(t);
            worker.dispose();
        }

        /** 终止时立即 emit 当前待发射项，再 onComplete 并 dispose worker。 */
        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;

            DebounceEmitter<T> d = timer;
            if (d != null) {
                d.dispose();
            }

            if (d != null) {
                d.emit();
            }

            downstream.onComplete();
            worker.dispose();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this, n);
            }
        }

        @Override
        public void cancel() {
            upstream.cancel();
            worker.dispose();
        }

        /** index 匹配且背压允许时向下游 onNext 并 produced。 */
        void emit(long idx, T t, DebounceEmitter<T> emitter) {
            if (idx == index) {
                long r = get();
                if (r != 0L) {
                    downstream.onNext(t);
                    BackpressureHelper.produced(this, 1);

                    emitter.dispose();
                } else {
                    cancel();
                    downstream.onError(MissingBackpressureException.createDefault());
                }
            }
        }
    }

    static final class DebounceEmitter<T> extends AtomicReference<Disposable> implements Runnable, Disposable {

        @Serial
        private static final long serialVersionUID = 6812032969491025141L;

        final T value;
        final long idx;
        final DebounceTimedSubscriber<T> parent;

        final AtomicBoolean once = new AtomicBoolean();

        DebounceEmitter(T value, long idx, DebounceTimedSubscriber<T> parent) {
            this.value = value;
            this.idx = idx;
            this.parent = parent;
        }

        /** 计时到期，触发 emit。 */
        @Override
        public void run() {
            emit();
        }

        void emit() {
            if (once.compareAndSet(false, true)) {
                parent.emit(idx, value, this);
            }
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return get() == DisposableHelper.DISPOSED;
        }

        public void setResource(Disposable d) {
            DisposableHelper.replace(this, d);
        }
    }
}
