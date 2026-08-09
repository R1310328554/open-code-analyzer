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
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 跳过上游前 n 个元素，之后原样转发。
 * @param <T> 元素类型
 */
public final class FlowableSkip<T> extends AbstractFlowableWithUpstream<T, T> {
    final long n;
    /**
     * @param source 上游 Flowable
     * @param n 要跳过的元素个数
     */
    public FlowableSkip(Flowable<T> source, long n) {
        super(source);
        this.n = n;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new SkipSubscriber<>(s, n));
    }

    /** 递减 remaining 计数，为 0 后开始转发 onNext。 */
    static final class SkipSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super T> downstream;
        long remaining;

        Subscription upstream;

        SkipSubscriber(Subscriber<? super T> actual, long n) {
            this.downstream = actual;
            this.remaining = n;
        }

        /** 向 upstream 预 request remaining 个以跳过前 n 个元素。 */
        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                long n = remaining;
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(n);
            }
        }

        @Override
        public void onNext(T t) {
            if (remaining != 0L) {
                remaining--;
            } else {
                downstream.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
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
