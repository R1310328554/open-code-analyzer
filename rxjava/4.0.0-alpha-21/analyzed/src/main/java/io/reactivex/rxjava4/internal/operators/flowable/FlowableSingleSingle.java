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

import java.util.NoSuchElementException;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.fuseable.FuseToFlowable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将上游 0 或 1 个元素转为 {@link Single}；多于一个则 onError，无元素则 defaultValue 或 onError。
 * @param <T> 元素类型
 */
public final class FlowableSingleSingle<T> extends Single<T> implements FuseToFlowable<T> {

    final Flowable<T> source;

    final T defaultValue;

    /**
     * @param source 上游 Flowable
     * @param defaultValue 无元素时的默认值（null 则 onError）
     */
    public FlowableSingleSingle(Flowable<T> source, T defaultValue) {
        this.source = source;
        this.defaultValue = defaultValue;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new SingleElementSubscriber<>(observer, defaultValue));
    }

    /** 融合为 {@link FlowableSingle}（failOnEmpty=true）。 */
    @Override
    public Flowable<T> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableSingle<>(source, defaultValue, true));
    }

    /** 收集唯一元素；无元素时用 defaultValue 或 NoSuchElementException。 */
    static final class SingleElementSubscriber<T>
    implements FlowableSubscriber<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final T defaultValue;

        Subscription upstream;

        boolean done;

        T value;

        SingleElementSubscriber(SingleObserver<? super T> actual, T defaultValue) {
            this.downstream = actual;
            this.defaultValue = defaultValue;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            if (value != null) {
                done = true;
                upstream.cancel();
                upstream = SubscriptionHelper.CANCELLED;
                downstream.onError(new IllegalArgumentException("Sequence contains more than one element!"));
                return;
            }
            value = t;
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

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            upstream = SubscriptionHelper.CANCELLED;
            T v = value;
            value = null;
            if (v == null) {
                v = defaultValue;
            }

            if (v != null) {
                downstream.onSuccess(v);
            } else {
                downstream.onError(new NoSuchElementException());
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
