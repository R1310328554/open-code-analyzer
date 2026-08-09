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
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.io.Serial;

/**
 * 判断上游是否存在任一元素满足 {@link Predicate}；存在 emit true，否则 emit false。
 * @param <T> 上游元素类型
 */
public final class FlowableAny<T> extends AbstractFlowableWithUpstream<T, Boolean> {
    final Predicate<? super T> predicate;
    /**
     * @param source 上游 Flowable
     * @param predicate 测试谓词
     */
    public FlowableAny(Flowable<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    @Override
    protected void subscribeActual(Subscriber<? super Boolean> s) {
        source.subscribe(new AnySubscriber<>(s, predicate));
    }

    static final class AnySubscriber<T> extends DeferredScalarSubscription<Boolean> implements FlowableSubscriber<T> {

        @Serial
        private static final long serialVersionUID = -2311252482644620661L;

        final Predicate<? super T> predicate;

        Subscription upstream;

        boolean done;

        AnySubscriber(Subscriber<? super Boolean> actual, Predicate<? super T> predicate) {
            super(actual);
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

        /** predicate 为 true 时取消上游并 emit true。 */
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
                onError(e);
                return;
            }
            if (b) {
                done = true;
                upstream.cancel();
                complete(true);
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

        /** 未找到匹配则 emit false。 */
        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                complete(false);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            upstream.cancel();
        }
    }
}
