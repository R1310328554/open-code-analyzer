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

package io.reactivex.rxjava4.internal.jdk8;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.parallel.ParallelFlowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 通过 Java 8 {@link Collector} 回调集将所有并行轨道归约为单一结果。
 *
 * @param <T> 值类型
 * @param <A> 累加器类型
 * @param <R> 结果类型
 * @since 3.0.0
 */
public final class ParallelCollector<T, A, R> extends Flowable<R> {

    final ParallelFlowable<? extends T> source;

    final Collector<T, A, R> collector;

    /** @param source 并行上游；@param collector Stream 收集器 */
    public ParallelCollector(ParallelFlowable<? extends T> source, Collector<T, A, R> collector) {
        this.source = source;
        this.collector = collector;
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        ParallelCollectorSubscriber<T, A, R> parent;
        try {
            parent = new ParallelCollectorSubscriber<>(s, source.parallelism(), collector);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptySubscription.error(ex, s);
            return;
        }
        s.onSubscribe(parent);

        source.subscribe(parent.subscribers);
    }

    /** 协调各并行轨道累加器并在全部完成后合并结果。 */
    static final class ParallelCollectorSubscriber<T, A, R> extends DeferredScalarSubscription<R> {

        @Serial
        private static final long serialVersionUID = -5370107872170712765L;

        final ParallelCollectorInnerSubscriber<T, A, R>[] subscribers;

        final AtomicReference<SlotPair<A>> current = new AtomicReference<>();

        final AtomicInteger remaining = new AtomicInteger();

        final AtomicThrowable error = new AtomicThrowable();

        final Function<A, R> finisher;

        ParallelCollectorSubscriber(Subscriber<? super R> subscriber, int n, Collector<T, A, R> collector) {
            super(subscriber);
            this.finisher = collector.finisher();
            @SuppressWarnings("unchecked")
            ParallelCollectorInnerSubscriber<T, A, R>[] a = new ParallelCollectorInnerSubscriber[n];
            for (int i = 0; i < n; i++) {
                a[i] = new ParallelCollectorInnerSubscriber<>(this, collector.supplier().get(), collector.accumulator(), collector.combiner());
            }
            this.subscribers = a;
            remaining.lazySet(n);
        }

        SlotPair<A> addValue(A value) {
            for (;;) {
                SlotPair<A> curr = current.get();

                if (curr == null) {
                    curr = new SlotPair<>();
                    if (!current.compareAndSet(null, curr)) {
                        continue;
                    }
                }

                int c = curr.tryAcquireSlot();
                if (c < 0) {
                    current.compareAndSet(curr, null);
                    continue;
                }
                if (c == 0) {
                    curr.first = value;
                } else {
                    curr.second = value;
                }

                if (curr.releaseSlot()) {
                    current.compareAndSet(curr, null);
                    return curr;
                }
                return null;
            }
        }

        @Override
        public void cancel() {
            for (ParallelCollectorInnerSubscriber<T, A, R> inner : subscribers) {
                inner.cancel();
            }
        }

        void innerError(Throwable ex) {
            if (error.compareAndSet(null, ex)) {
                cancel();
                downstream.onError(ex);
            } else {
                if (ex != error.get()) {
                    RxJavaPlugins.onError(ex);
                }
            }
        }

        void innerComplete(A value, BinaryOperator<A> combiner) {
            for (;;) {
                SlotPair<A> sp = addValue(value);

                if (sp != null) {

                    try {
                        value = combiner.apply(sp.first, sp.second);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        innerError(ex);
                        return;
                    }

                } else {
                    break;
                }
            }

            if (remaining.decrementAndGet() == 0) {
                SlotPair<A> sp = current.get();
                current.lazySet(null);

                R result;
                try {
                    result = Objects.requireNonNull(finisher.apply(sp.first), "The finisher returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    innerError(ex);
                    return;
                }

                complete(result);
            }
        }
    }

    /** 单条并行轨道的收集订阅者。 */
    static final class ParallelCollectorInnerSubscriber<T, A, R>
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<T> {

        @Serial
        private static final long serialVersionUID = -7954444275102466525L;

        final ParallelCollectorSubscriber<T, A, R> parent;

        final BiConsumer<A, T> accumulator;

        final BinaryOperator<A> combiner;

        A container;

        boolean done;

        ParallelCollectorInnerSubscriber(ParallelCollectorSubscriber<T, A, R> parent, A container, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner) {
            this.parent = parent;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.container = container;
        }

        @Override
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        @Override
        public void onNext(T t) {
            if (!done) {
                try {
                    accumulator.accept(container, t);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    get().cancel();
                    onError(ex);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            container = null;
            done = true;
            parent.innerError(t);
        }

        @Override
        public void onComplete() {
            if (!done) {
                A v = container;
                container = null;
                done = true;
                parent.innerComplete(v, combiner);
            }
        }

        void cancel() {
            SubscriptionHelper.cancel(this);
        }
    }

    /** 用于两两合并累加器的双槽位配对结构。 */
    static final class SlotPair<T> extends AtomicInteger {

        @Serial
        private static final long serialVersionUID = 473971317683868662L;

        T first;

        T second;

        final AtomicInteger releaseIndex = new AtomicInteger();

        int tryAcquireSlot() {
            for (;;) {
                int acquired = get();
                if (acquired >= 2) {
                    return -1;
                }

                if (compareAndSet(acquired, acquired + 1)) {
                    return acquired;
                }
            }
        }

        boolean releaseSlot() {
            return releaseIndex.incrementAndGet() == 2;
        }
    }
}
