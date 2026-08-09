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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.fuseable.FuseToFlowable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 判断上游 {@link Flowable} 是否存在任一元素满足 {@link Predicate}；
 * 存在则 {@link Single} 发出 true，否则在完成后发出 false。
 * @param <T> 上游元素类型
 */
public final class FlowableAnySingle<T> extends Single<Boolean> implements FuseToFlowable<Boolean> {
    final Flowable<T> source;

    final Predicate<? super T> predicate;

    /**
     * @param source 上游 Flowable
     * @param predicate 测试谓词
     */
    public FlowableAnySingle(Flowable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    /** 订阅 source 并在找到匹配或完成时发出 Boolean 结果。 */
    @Override
    protected void subscribeActual(SingleObserver<? super Boolean> observer) {
        source.subscribe(new AnySubscriber<>(observer, predicate));
    }

    /** 融合为 {@link FlowableAny} 以支持背压。 */
    @Override
    public Flowable<Boolean> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableAny<>(source, predicate));
    }

    /** 逐项测试 predicate；匹配时取消上游并发出 true。 */
    static final class AnySubscriber<T> implements FlowableSubscriber<T>, Disposable {

        final SingleObserver<? super Boolean> downstream;

        final Predicate<? super T> predicate;

        Subscription upstream;

        boolean done;

        AnySubscriber(SingleObserver<? super Boolean> actual, Predicate<? super T> predicate) {
            this.downstream = actual;
            this.predicate = predicate;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        /** predicate 为 true 时取消上游并 onSuccess(true)。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            boolean b;
            try {
                b = predicate.test(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                upstream.cancel();
                upstream = SubscriptionHelper.CANCELLED;
                onError(e);
                return;
            }
            if (b) {
                done = true;
                upstream.cancel();
                upstream = SubscriptionHelper.CANCELLED;
                downstream.onSuccess(true);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }

            done = true;
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onError(t);
        }

        /** 未找到匹配则 onSuccess(false)。 */
        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                upstream = SubscriptionHelper.CANCELLED;
                downstream.onSuccess(false);
            }
        }

        @Override
        public void dispose() {
            upstream.cancel();
            upstream = SubscriptionHelper.CANCELLED;
        }

        @Override
        public boolean isDisposed() {
            return upstream == SubscriptionHelper.CANCELLED;
        }
    }
}
