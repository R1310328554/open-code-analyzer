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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.BiFunction;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.util.Objects;

/**
 * 累积 scan：首元素直接发射，后续用 {@link BiFunction} 累积并发射。
 * @param <T> 元素类型
 */
public final class FlowableScan<T> extends AbstractFlowableWithUpstream<T, T> {
    final BiFunction<T, T, T> accumulator;
    /**
     * @param source 上游 Flowable
     * @param accumulator 累积函数（前一累积值, 当前元素）→ 新累积值
     */
    public FlowableScan(Flowable<T> source, BiFunction<T, T, T> accumulator) {
        super(source);
        this.accumulator = accumulator;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new ScanSubscriber<>(s, accumulator));
    }

    /** 维护累积值 value，首元素直接发射，其后 apply accumulator。 */
    static final class ScanSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super T> downstream;
        final BiFunction<T, T, T> accumulator;

        Subscription upstream;

        T value;

        boolean done;

        ScanSubscriber(Subscriber<? super T> actual, BiFunction<T, T, T> accumulator) {
            this.downstream = actual;
            this.accumulator = accumulator;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
            }
        }

        /** value 为 null 时直接发射首元素，否则累积后发射。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            final Subscriber<? super T> a = downstream;
            T v = value;
            if (v == null) {
                value = t;
                a.onNext(t);
            } else {
                T u;

                try {
                    u = Objects.requireNonNull(accumulator.apply(v, t), "The value returned by the accumulator is null");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    upstream.cancel();
                    onError(e);
                    return;
                }

                value = u;
                a.onNext(u);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            downstream.onComplete();
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }
    }
}
