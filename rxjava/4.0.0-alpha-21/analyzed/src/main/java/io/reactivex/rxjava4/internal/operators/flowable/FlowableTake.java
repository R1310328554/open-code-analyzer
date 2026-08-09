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
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 仅向下游转发前 n 个元素，随后 cancel 上游并 onComplete。
 * @param <T> 元素类型
 */
public final class FlowableTake<T> extends AbstractFlowableWithUpstream<T, T> {

    final long n;

    /**
     * @param source 上游 Flowable
     * @param n 最多发射的元素个数
     */
    public FlowableTake(Flowable<T> source, long n) {
        super(source);
        this.n = n;
    }

    /** 订阅 TakeSubscriber。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new TakeSubscriber<>(s, n));
    }

    /** 维护 remaining 计数；request 不超过 remaining。 */
    static final class TakeSubscriber<T>
    extends AtomicLong
    implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = 2288246011222124525L;

        final Subscriber<? super T> downstream;

        long remaining;

        Subscription upstream;

        TakeSubscriber(Subscriber<? super T> actual, long remaining) {
            this.downstream = actual;
            this.remaining = remaining;
            lazySet(remaining);
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                if (remaining == 0L) {
                    s.cancel();
                    EmptySubscription.complete(downstream);
                } else {
                    this.upstream = s;
                    downstream.onSubscribe(this);
                }
            }
        }

        /** 递减 remaining；归零时 cancel 并完成。 */
        @Override
        public void onNext(T t) {
            long r = remaining;
            if (r > 0L) {
                remaining = --r;
                downstream.onNext(t);
                if (r == 0L) {
                    upstream.cancel();
                    downstream.onComplete();
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (remaining > 0L) {
                remaining = 0L;
                downstream.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (remaining > 0L) {
                remaining = 0L;
                downstream.onComplete();
            }
        }

        /** 将请求量限制在 remaining 以内转发上游。 */
        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                for (;;) {
                    long r = get();
                    if (r == 0L) {
                        break;
                    }
                    long toRequest = Math.min(r, n);
                    long u = r - toRequest;
                    if (compareAndSet(r, u)) {
                        upstream.request(toRequest);
                        break;
                    }
                }
            }
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }

    }
}
