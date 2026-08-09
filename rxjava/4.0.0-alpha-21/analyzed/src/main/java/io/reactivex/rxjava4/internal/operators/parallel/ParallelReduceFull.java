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

package io.reactivex.rxjava4.internal.operators.parallel;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.BiFunction;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.parallel.ParallelFlowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将所有并行轨道归约为单个值：各轨道先局部 reduce，
 * 再以 SlotPair 两两合并，最终发射至单一 Flowable 下游。
 *
 * @param <T> 元素类型
 */
public final class ParallelReduceFull<T> extends Flowable<T> {

    final ParallelFlowable<? extends T> source;

    final BiFunction<T, T, T> reducer;

    /**
     * @param source 并行上游
     * @param reducer 两值合并函数
     */
    public ParallelReduceFull(ParallelFlowable<? extends T> source, BiFunction<T, T, T> reducer) {
        this.source = source;
        this.reducer = reducer;
    }

    /** 创建 MainSubscriber 并订阅各 inner 轨道。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        ParallelReduceFullMainSubscriber<T> parent = new ParallelReduceFullMainSubscriber<>(s, source.parallelism(), reducer);
        s.onSubscribe(parent);

        source.subscribe(parent.subscribers);
    }

    /** 协调各 inner 完成值，经 addValue 两两归并后 complete。 */
    static final class ParallelReduceFullMainSubscriber<T> extends DeferredScalarSubscription<T> {

        @Serial
        private static final long serialVersionUID = -5370107872170712765L;

        final ParallelReduceFullInnerSubscriber<T>[] subscribers;

        final BiFunction<T, T, T> reducer;

        final AtomicReference<SlotPair<T>> current = new AtomicReference<>();

        final AtomicInteger remaining = new AtomicInteger();

        final AtomicThrowable error = new AtomicThrowable();

        ParallelReduceFullMainSubscriber(Subscriber<? super T> subscriber, int n, BiFunction<T, T, T> reducer) {
            super(subscriber);
            @SuppressWarnings("unchecked")
            ParallelReduceFullInnerSubscriber<T>[] a = new ParallelReduceFullInnerSubscriber[n];
            for (int i = 0; i < n; i++) {
                a[i] = new ParallelReduceFullInnerSubscriber<>(this, reducer);
            }
            this.subscribers = a;
            this.reducer = reducer;
            remaining.lazySet(n);
        }

        /** CAS 获取 SlotPair 双槽，凑齐一对后返回供 reducer 合并。 */
        SlotPair<T> addValue(T value) {
            for (;;) {
                SlotPair<T> curr = current.get();

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
            for (ParallelReduceFullInnerSubscriber<T> inner : subscribers) {
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

        /** 接收轨道局部结果，循环 addValue+reducer 直至 remaining 归零。 */
        void innerComplete(T value) {
            if (value != null) {
                for (;;) {
                    SlotPair<T> sp = addValue(value);

                    if (sp != null) {

                        try {
                            value = Objects.requireNonNull(reducer.apply(sp.first, sp.second), "The reducer returned a null value");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            innerError(ex);
                            return;
                        }

                    } else {
                        break;
                    }
                }
            }

            if (remaining.decrementAndGet() == 0) {
                SlotPair<T> sp = current.get();
                current.lazySet(null);

                if (sp != null) {
                    complete(sp.first);
                } else {
                    downstream.onComplete();
                }
            }
        }
    }

    /** 单轨道局部 reduce：逐 onNext 累加，onComplete 时 innerComplete(value)。 */
    static final class ParallelReduceFullInnerSubscriber<T>
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<T> {

        @Serial
        private static final long serialVersionUID = -7954444275102466525L;

        final ParallelReduceFullMainSubscriber<T> parent;

        final BiFunction<T, T, T> reducer;

        T value;

        boolean done;

        ParallelReduceFullInnerSubscriber(ParallelReduceFullMainSubscriber<T> parent, BiFunction<T, T, T> reducer) {
            this.parent = parent;
            this.reducer = reducer;
        }

        @Override
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        /** 首值缓存，其后 reducer.apply 累加至 value。 */
        @Override
        public void onNext(T t) {
            if (!done) {
                T v = value;

                if (v == null) {
                    value = t;
                } else {

                    try {
                        v = Objects.requireNonNull(reducer.apply(v, t), "The reducer returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        get().cancel();
                        onError(ex);
                        return;
                    }

                    value = v;
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            parent.innerError(t);
        }

        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                parent.innerComplete(value);
            }
        }

        void cancel() {
            SubscriptionHelper.cancel(this);
        }
    }

    /** 双槽配对结构：tryAcquireSlot 占槽，releaseSlot 凑齐后可供合并。 */
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
