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
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 跳过 {@link Predicate} 为 true 的连续前缀元素，
 * 从首个 false 元素起向下游转发。
 * @param <T> 元素类型
 */
public final class FlowableSkipWhile<T> extends AbstractFlowableWithUpstream<T, T> {
    final Predicate<? super T> predicate;
    /**
     * @param source 上游 Flowable
     * @param predicate 为 true 时跳过该元素
     */
    public FlowableSkipWhile(Flowable<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    /** 订阅 SkipWhileSubscriber。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new SkipWhileSubscriber<>(s, predicate));
    }

    /** 跳过阶段 request(1) 逐元素测试；结束后透传 onNext。 */
    static final class SkipWhileSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super T> downstream;
        final Predicate<? super T> predicate;
        Subscription upstream;
        boolean notSkipping;
        SkipWhileSubscriber(Subscriber<? super T> actual, Predicate<? super T> predicate) {
            this.downstream = actual;
            this.predicate = predicate;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
            }
        }

        /** predicate 为 true 则丢弃并 request(1)；false 起转发。 */
        @Override
        public void onNext(T t) {
            if (notSkipping) {
                downstream.onNext(t);
            } else {
                boolean b;
                try {
                    b = predicate.test(t);
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    upstream.cancel();
                    downstream.onError(e);
                    return;
                }
                if (b) {
                    upstream.request(1);
                } else {
                    notSkipping = true;
                    downstream.onNext(t);
                }
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
