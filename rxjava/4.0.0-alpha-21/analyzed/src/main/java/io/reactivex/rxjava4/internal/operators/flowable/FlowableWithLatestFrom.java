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
import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.BiFunction;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.operators.ConditionalSubscriber;
import io.reactivex.rxjava4.subscribers.SerializedSubscriber;

/**
 * 主序列每个元素与 other 序列最新值经 combiner 合并后发射。
 * other 尚无值时跳过该主序列元素并 request(1)。
 * @param <T> 主序列类型
 * @param <U> other 序列类型
 * @param <R> 合并结果类型
 */
public final class FlowableWithLatestFrom<T, U, R> extends AbstractFlowableWithUpstream<T, R> {
    final BiFunction<? super T, ? super U, ? extends R> combiner;
    final Publisher<? extends U> other;
    /**
     * @param source 主序列 Flowable
     * @param combiner 合并函数
     * @param other 提供最新值的 Publisher
     */
    public FlowableWithLatestFrom(Flowable<T> source, BiFunction<? super T, ? super U, ? extends R> combiner, Publisher<? extends U> other) {
        super(source);
        this.combiner = combiner;
        this.other = other;
    }

    /** SerializedSubscriber 包装；先订阅 other 再订阅 source。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        final SerializedSubscriber<R> serial = new SerializedSubscriber<>(s);
        final WithLatestFromSubscriber<T, U, R> wlf = new WithLatestFromSubscriber<>(serial, combiner);

        serial.onSubscribe(wlf);

        other.subscribe(new FlowableWithLatestSubscriber(wlf));

        source.subscribe(wlf);
    }

    /** AtomicReference 存 other 最新值；tryOnNext 有值才合并。 */
    static final class WithLatestFromSubscriber<T, U, R> extends AtomicReference<U>
    implements ConditionalSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -312246233408980075L;

        final Subscriber<? super R> downstream;

        final BiFunction<? super T, ? super U, ? extends R> combiner;

        final AtomicReference<Subscription> upstream = new AtomicReference<>();

        final AtomicLong requested = new AtomicLong();

        final AtomicReference<Subscription> other = new AtomicReference<>();

        WithLatestFromSubscriber(Subscriber<? super R> actual, BiFunction<? super T, ? super U, ? extends R> combiner) {
            this.downstream = actual;
            this.combiner = combiner;
        }

        @Override
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.deferredSetOnce(this.upstream, requested, s);
        }

        @Override
        public void onNext(T t) {
            if (!tryOnNext(t)) {
                upstream.get().request(1);
            }
        }

        @Override
        public boolean tryOnNext(T t) {
            U u = get();
            if (u != null) {
                R r;
                try {
                    r = Objects.requireNonNull(combiner.apply(t, u), "The combiner returned a null value");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    downstream.onError(e);
                    return false;
                }
                downstream.onNext(r);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onError(Throwable t) {
            SubscriptionHelper.cancel(other);
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            SubscriptionHelper.cancel(other);
            downstream.onComplete();
        }

        @Override
        public void request(long n) {
            SubscriptionHelper.deferredRequest(upstream, requested, n);
        }

        @Override
        public void cancel() {
            SubscriptionHelper.cancel(upstream);
            SubscriptionHelper.cancel(other);
        }

        public boolean setOther(Subscription o) {
            return SubscriptionHelper.setOnce(other, o);
        }

        public void otherError(Throwable e) {
            SubscriptionHelper.cancel(upstream);
            downstream.onError(e);
        }
    }

    /** 订阅 other 并 lazySet 最新值。 */
    final class FlowableWithLatestSubscriber implements FlowableSubscriber<U> {
        private final WithLatestFromSubscriber<T, U, R> wlf;

        FlowableWithLatestSubscriber(WithLatestFromSubscriber<T, U, R> wlf) {
            this.wlf = wlf;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (wlf.setOther(s)) {
                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(U t) {
            wlf.lazySet(t);
        }

        @Override
        public void onError(Throwable t) {
            wlf.otherError(t);
        }

        @Override
        public void onComplete() {
            // other 完成不影响主序列，由 wlf 自行终止
        }
    }
}
