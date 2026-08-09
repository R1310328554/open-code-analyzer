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
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 消费上游 {@link Publisher} 并发射最后一项；空流时发射 defaultItem 或 NoSuchElementException。
 * 
 * @param <T> 元素类型
 */
public final class FlowableLastSingle<T> extends Single<T> {

    final Publisher<T> source;

    final T defaultItem;

    /**
     * @param source 上游 Publisher
     * @param defaultItem 空流时的默认值（可为 null）
     */
    public FlowableLastSingle(Publisher<T> source, T defaultItem) {
        this.source = source;
        this.defaultItem = defaultItem;
    }

    // TODO 融合回 Flowable

    /** 请求全部元素，onComplete 时发射最后一项或默认值。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new LastSubscriber<>(observer, defaultItem));
    }

    /** 保留最后一项并在终止时通知 SingleObserver。 */
    static final class LastSubscriber<T> implements FlowableSubscriber<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final T defaultItem;

        Subscription upstream;

        T item;

        LastSubscriber(SingleObserver<? super T> actual, T defaultItem) {
            this.downstream = actual;
            this.defaultItem = defaultItem;
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
            item = t;
        }

        @Override
        public void onError(Throwable t) {
            upstream = SubscriptionHelper.CANCELLED;
            item = null;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            upstream = SubscriptionHelper.CANCELLED;
            T v = item;
            if (v != null) {
                item = null;
                downstream.onSuccess(v);
            } else {
                v = defaultItem;

                if (v != null) {
                    downstream.onSuccess(v);
                } else {
                    downstream.onError(new NoSuchElementException());
                }
            }
        }
    }
}
