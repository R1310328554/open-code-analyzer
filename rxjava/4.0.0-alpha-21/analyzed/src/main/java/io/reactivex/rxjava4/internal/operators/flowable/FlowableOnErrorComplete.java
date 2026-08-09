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
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 若上游 onError 且 {@link Predicate} 对该 {@link Throwable} 返回 true，
 * 则转为向下游发射 onComplete 而非 onError。
 * 
 * @param <T> 值类型
 * @since 3.0.0
 */
public final class FlowableOnErrorComplete<T> extends AbstractFlowableWithUpstream<T, T> {

    final Predicate<? super Throwable> predicate;

    /**
     * @param source 上游 Flowable
     * @param predicate 返回 true 时将错误转为完成
     */
    public FlowableOnErrorComplete(Flowable<T> source,
            Predicate<? super Throwable> predicate) {
        super(source);
        this.predicate = predicate;
    }

    /** 订阅上游并按 predicate 过滤错误。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> observer) {
        source.subscribe(new OnErrorCompleteSubscriber<>(observer, predicate));
    }

    /** 根据 predicate 决定转发错误或转为 onComplete 的 subscriber。 */
    public static final class OnErrorCompleteSubscriber<T>
    implements FlowableSubscriber<T>, Subscription {

        final Subscriber<? super T> downstream;

        final Predicate<? super Throwable> predicate;

        Subscription upstream;

        public OnErrorCompleteSubscriber(Subscriber<? super T> actual, Predicate<? super Throwable> predicate) {
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

        @Override
        public void onNext(T value) {
            downstream.onNext(value);
        }

        /** predicate 为 true 时 onComplete，否则转发 onError。 */
        @Override
        public void onError(Throwable e) {
            boolean b;

            try {
                b = predicate.test(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(e, ex));
                return;
            }

            if (b) {
                downstream.onComplete();
            } else {
                downstream.onError(e);
            }
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }
    }
}
