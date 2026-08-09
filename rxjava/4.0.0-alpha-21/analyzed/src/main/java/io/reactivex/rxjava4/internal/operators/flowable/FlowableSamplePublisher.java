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
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.MissingBackpressureException;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;
import io.reactivex.rxjava4.subscribers.SerializedSubscriber;

/**
 * 由另一 {@link Publisher} 的 onNext 触发，采样上游最新值并向下游发射。
 * @param <T> 上游元素类型
 */
public final class FlowableSamplePublisher<T> extends Flowable<T> {
    final Publisher<T> source;
    final Publisher<?> other;

    final boolean emitLast;

    /**
     * @param source 被采样的上游 Publisher
     * @param other 采样触发源（其 onNext 触发一次采样）
     * @param emitLast 上游完成时是否发射最后一个缓存样本
     */
    public FlowableSamplePublisher(Publisher<T> source, Publisher<?> other, boolean emitLast) {
        this.source = source;
        this.other = other;
        this.emitLast = emitLast;
    }

    /** 按 emitLast 选择是否在上游结束时发射最后一个样本值。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        SerializedSubscriber<T> serial = new SerializedSubscriber<>(s);
        if (emitLast) {
            source.subscribe(new SampleMainEmitLast<>(serial, other));
        } else {
            source.subscribe(new SampleMainNoLast<>(serial, other));
        }
    }

    /** 缓存上游最新值，由采样源 onNext 触发 emit 的 subscriber 基类。 */
    abstract static class SamplePublisherSubscriber<T> extends AtomicReference<T> implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -3517602651313910099L;

        final Subscriber<? super T> downstream;
        final Publisher<?> sampler;

        final AtomicLong requested = new AtomicLong();

        final AtomicReference<Subscription> other = new AtomicReference<>();

        Subscription upstream;

        SamplePublisherSubscriber(Subscriber<? super T> actual, Publisher<?> other) {
            this.downstream = actual;
            this.sampler = other;
        }

        /** 订阅上游并启动采样源；上游以 MAX 请求。 */
        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                if (other.get() == null) {
                    sampler.subscribe(new SamplerSubscriber<>(this));
                    s.request(Long.MAX_VALUE);
                }
            }

        }

        /** 以 lazySet 缓存上游最新样本值。 */
        @Override
        public void onNext(T t) {
            lazySet(t);
        }

        @Override
        public void onError(Throwable t) {
            SubscriptionHelper.cancel(other);
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            SubscriptionHelper.cancel(other);
            completion();
        }

        void setOther(Subscription o) {
            SubscriptionHelper.setOnce(other, o, Long.MAX_VALUE);
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
            }
        }

        @Override
        public void cancel() {
            SubscriptionHelper.cancel(other);
            upstream.cancel();
        }

        public void error(Throwable e) {
            upstream.cancel();
            downstream.onError(e);
        }

        public void complete() {
            upstream.cancel();
            completion();
        }

        /** 取出并发射缓存样本；无背压则 MissingBackpressureException。 */
        void emit() {
            T value = getAndSet(null);
            if (value != null) {
                long r = requested.get();
                if (r != 0L) {
                    downstream.onNext(value);
                    BackpressureHelper.produced(requested, 1);
                } else {
                    cancel();
                    downstream.onError(MissingBackpressureException.createDefault());
                }
            }
        }

        abstract void completion();

        abstract void run();
    }

    /** 采样触发源 subscriber：onNext 时调用 parent.run()。 */
    record SamplerSubscriber<T>(SamplePublisherSubscriber<T> parent) implements FlowableSubscriber<Object> {

        @Override
            public void onSubscribe(Subscription s) {
                parent.setOther(s);
            }

            @Override
            public void onNext(Object t) {
                parent.run();
            }

            @Override
            public void onError(Throwable t) {
                parent.error(t);
            }

            @Override
            public void onComplete() {
                parent.complete();
            }
        }

    /** emitLast=false：上游完成时不发射最后一个缓存样本。 */
    static final class SampleMainNoLast<T> extends SamplePublisherSubscriber<T> {

        @Serial
        private static final long serialVersionUID = -3029755663834015785L;

        SampleMainNoLast(Subscriber<? super T> actual, Publisher<?> other) {
            super(actual, other);
        }

        @Override
        void completion() {
            downstream.onComplete();
        }

        @Override
        void run() {
            emit();
        }
    }

    /** emitLast=true：上游完成时发射最后一个缓存样本后再 onComplete。 */
    static final class SampleMainEmitLast<T> extends SamplePublisherSubscriber<T> {

        @Serial
        private static final long serialVersionUID = -3029755663834015785L;

        final AtomicInteger wip;

        volatile boolean done;

        SampleMainEmitLast(Subscriber<? super T> actual, Publisher<?> other) {
            super(actual, other);
            this.wip = new AtomicInteger();
        }

        @Override
        void completion() {
            done = true;
            if (wip.getAndIncrement() == 0) {
                emit();
                downstream.onComplete();
            }
        }

        @Override
        void run() {
            if (wip.getAndIncrement() == 0) {
                do {
                    boolean d = done;
                    emit();
                    if (d) {
                        downstream.onComplete();
                        return;
                    }
                } while (wip.decrementAndGet() != 0);
            }
        }
    }
}
