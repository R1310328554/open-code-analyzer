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
 * 将上游第 index 个元素作为 {@link Single} 发射；
 * 不足时按 defaultValue 或 onError({@link NoSuchElementException}) 处理。
 * @param <T> 元素类型
 */
public final class FlowableElementAtSingle<T> extends Single<T> implements FuseToFlowable<T> {
    final Flowable<T> source;

    final long index;

    final T defaultValue;

    /**
     * @param source 上游 Flowable
     * @param index 目标索引（0 起）
     * @param defaultValue 元素不足时的默认值（null 则 onError）
     */
    public FlowableElementAtSingle(Flowable<T> source, long index, T defaultValue) {
        this.source = source;
        this.index = index;
        this.defaultValue = defaultValue;
    }

    /** 订阅 source 并在到达 index 时 onSuccess 目标元素。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new ElementAtSubscriber<>(observer, index, defaultValue));
    }

    /** 融合为 {@link FlowableElementAt} 以支持背压。 */
    @Override
    public Flowable<T> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableElementAt<>(source, index, defaultValue, true));
    }

    /** 计数上游元素并在 index 处 cancel 并 onSuccess。 */
    static final class ElementAtSubscriber<T> implements FlowableSubscriber<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final long index;
        final T defaultValue;

        Subscription upstream;

        long count;

        boolean done;

        ElementAtSubscriber(SingleObserver<? super T> actual, long index, T defaultValue) {
            this.downstream = actual;
            this.index = index;
            this.defaultValue = defaultValue;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(index + 1);
            }
        }

        /** count 达 index 时 cancel 上游并 onSuccess(t)。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            long c = count;
            if (c == index) {
                done = true;
                upstream.cancel();
                upstream = SubscriptionHelper.CANCELLED;
                downstream.onSuccess(t);
                return;
            }
            count = c + 1;
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

        /** 未达 index 时按 defaultValue 或 NoSuchElementException 完成。 */
        @Override
        public void onComplete() {
            upstream = SubscriptionHelper.CANCELLED;
            if (!done) {
                done = true;

                T v = defaultValue;

                if (v != null) {
                    downstream.onSuccess(v);
                } else {
                    downstream.onError(new NoSuchElementException());
                }
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
