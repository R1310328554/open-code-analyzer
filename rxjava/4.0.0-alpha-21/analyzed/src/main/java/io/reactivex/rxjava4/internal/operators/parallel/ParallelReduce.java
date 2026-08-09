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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.subscribers.DeferredScalarSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.parallel.ParallelFlowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.io.Serial;
import java.util.Objects;

/**
 * 将每条并行轨道的元素序列归约为单个值（各轨道独立 reduce）。
 *
 * @param <T> 输入元素类型
 * @param <R> 归约结果类型
 */
public final class ParallelReduce<T, R> extends ParallelFlowable<R> {

    final ParallelFlowable<? extends T> source;

    final Supplier<R> initialSupplier;

    final BiFunction<R, ? super T, R> reducer;

    /**
     * @param source 并行上游
     * @param initialSupplier 每条轨道的初始累加器 Supplier
     * @param reducer 累加函数 (acc, value) -> acc
     */
    public ParallelReduce(ParallelFlowable<? extends T> source, Supplier<R> initialSupplier, BiFunction<R, ? super T, R> reducer) {
        this.source = source;
        this.initialSupplier = initialSupplier;
        this.reducer = reducer;
    }

    @Override
    public void subscribe(Subscriber<? super R>[] subscribers) {
        subscribers = RxJavaPlugins.onSubscribe(this, subscribers);

        if (!validate(subscribers)) {
            return;
        }

        int n = subscribers.length;
        @SuppressWarnings("unchecked")
        Subscriber<T>[] parents = new Subscriber[n];

        for (int i = 0; i < n; i++) {

            R initialValue;

            try {
                initialValue = Objects.requireNonNull(initialSupplier.get(), "The initialSupplier returned a null value");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                reportError(subscribers, ex);
                return;
            }

            parents[i] = new ParallelReduceSubscriber<>(subscribers[i], initialValue, reducer);
        }

        source.subscribe(parents);
    }

    /** initialSupplier 失败时向所有 Subscriber 发送 EmptySubscription.error。 */
    void reportError(Subscriber<?>[] subscribers, Throwable ex) {
        for (Subscriber<?> s : subscribers) {
            EmptySubscription.error(ex, s);
        }
    }

    @Override
    public int parallelism() {
        return source.parallelism();
    }

    /** 单轨道 reduce：逐 onNext 更新 accumulator，onComplete 时 complete。 */
    static final class ParallelReduceSubscriber<T, R> extends DeferredScalarSubscriber<T, R> {

        @Serial
        private static final long serialVersionUID = 8200530050639449080L;

        final BiFunction<R, ? super T, R> reducer;

        R accumulator;

        boolean done;

        ParallelReduceSubscriber(Subscriber<? super R> subscriber, R initialValue, BiFunction<R, ? super T, R> reducer) {
            super(subscriber);
            this.accumulator = initialValue;
            this.reducer = reducer;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        /** reducer.apply(accumulator, t) 更新累加器；异常 cancel 并 onError。 */
        @Override
        public void onNext(T t) {
            if (!done) {
                R v;

                try {
                    v = Objects.requireNonNull(reducer.apply(accumulator, t), "The reducer returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    cancel();
                    onError(ex);
                    return;
                }

                accumulator = v;
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            accumulator = null;
            downstream.onError(t);
        }

        /** 发射最终 accumulator 并 complete。 */
        @Override
        public void onComplete() {
            if (!done) {
                done = true;

                R a = accumulator;
                accumulator = null;
                complete(a);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            upstream.cancel();
        }
    }
}
