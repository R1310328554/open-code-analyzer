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
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 转发主序列元素直至 {@code other} 发出任意信号（onNext/onComplete/onError）。
 * @param <T> 主序列元素类型
 * @param <U> other 序列元素类型
 */
public final class FlowableTakeUntil<T, U> extends AbstractFlowableWithUpstream<T, T> {
    final Publisher<? extends U> other;
    /**
     * @param source 主序列 Flowable
     * @param other 触发终止的 Publisher
     */
    public FlowableTakeUntil(Flowable<T> source, Publisher<? extends U> other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> child) {
        TakeUntilMainSubscriber<T> parent = new TakeUntilMainSubscriber<>(child);
        child.onSubscribe(parent);

        other.subscribe(parent.other);

        source.subscribe(parent);
    }

    /** HalfSerializer 协调主/other 两路终止。 */
    static final class TakeUntilMainSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -4945480365982832967L;

        final Subscriber<? super T> downstream;

        final AtomicLong requested;

        final AtomicReference<Subscription> upstream;

        final AtomicThrowable error;

        final OtherSubscriber other;

        TakeUntilMainSubscriber(Subscriber<? super T> downstream) {
            this.downstream = downstream;
            this.requested = new AtomicLong();
            this.upstream = new AtomicReference<>();
            this.other = new OtherSubscriber();
            this.error = new AtomicThrowable();
        }

        @Override
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.deferredSetOnce(this.upstream, requested, s);
        }

        @Override
        public void onNext(T t) {
            HalfSerializer.onNext(downstream, t, this, error);
        }

        @Override
        public void onError(Throwable t) {
            SubscriptionHelper.cancel(other);
            HalfSerializer.onError(downstream, t, this, error);
        }

        @Override
        public void onComplete() {
            SubscriptionHelper.cancel(other);
            HalfSerializer.onComplete(downstream, this, error);
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

        /** other 任一路径终止时 cancel 主序列并完成下游。 */
        final class OtherSubscriber extends AtomicReference<Subscription> implements FlowableSubscriber<Object> {

            @Serial
            private static final long serialVersionUID = -3592821756711087922L;

            @Override
            public void onSubscribe(Subscription s) {
                SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
            }

            /** other onNext 即视为终止信号。 */
            @Override
            public void onNext(Object t) {
                SubscriptionHelper.cancel(this);
                onComplete();
            }

            @Override
            public void onError(Throwable t) {
                SubscriptionHelper.cancel(upstream);
                HalfSerializer.onError(downstream, t, TakeUntilMainSubscriber.this, error);
            }

            @Override
            public void onComplete() {
                SubscriptionHelper.cancel(upstream);
                HalfSerializer.onComplete(downstream, TakeUntilMainSubscriber.this, error);
            }

        }
    }
}
