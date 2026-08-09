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
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 发射元素直至 {@link Predicate} 对某元素返回 true（含该元素），随后 onComplete。
 * @param <T> 元素类型
 */
public final class FlowableTakeUntilPredicate<T> extends AbstractFlowableWithUpstream<T, T> {
    final Predicate<? super T> predicate;
    /**
     * @param source 上游 Flowable
     * @param predicate 为 true 时终止（该元素已发射）
     */
    public FlowableTakeUntilPredicate(Flowable<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new InnerSubscriber<>(s, predicate));
    }

    /** 先 onNext 再测 predicate；true 则 cancel 并完成。 */
    static final class InnerSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super T> downstream;
        final Predicate<? super T> predicate;
        Subscription upstream;
        boolean done;
        InnerSubscriber(Subscriber<? super T> actual, Predicate<? super T> predicate) {
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

        /** 发射后若 predicate 为 true 则结束序列。 */
        @Override
        public void onNext(T t) {
            if (!done) {
                downstream.onNext(t);
                boolean b;
                try {
                    b = predicate.test(t);
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    upstream.cancel();
                    onError(e);
                    return;
                }
                if (b) {
                    done = true;
                    upstream.cancel();
                    downstream.onComplete();
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!done) {
                done = true;
                downstream.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                downstream.onComplete();
            }
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
