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

package io.reactivex.rxjava4.internal.operators.observable;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 将 Reactive Streams {@link Publisher} 适配为 Observable：
 * 订阅后 request(Long.MAX_VALUE) 并无背压地转发信号。
 *
 * @param <T> 元素类型
 */
public final class ObservableFromPublisher<T> extends Observable<T> {

    final Publisher<? extends T> source;

    /** @param publisher 上游 Publisher */
    public ObservableFromPublisher(Publisher<? extends T> publisher) {
        this.source = publisher;
    }

    /** 订阅 PublisherSubscriber 桥接 Publisher 与 Observer。 */
    @Override
    protected void subscribeActual(final Observer<? super T> o) {
        source.subscribe(new PublisherSubscriber<T>(o));
    }

    /** 桥接 Subscription 与 Disposable，无背压转发 Publisher 事件。 */
    static final class PublisherSubscriber<T>
    implements FlowableSubscriber<T>, Disposable {

        final Observer<? super T> downstream;
        Subscription upstream;

        PublisherSubscriber(Observer<? super T> o) {
            this.downstream = o;
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        /** 校验 Subscription 后 request(MAX) 并将自身作为 Disposable 下发。 */
        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        /** cancel 上游 Subscription 并标记 CANCELLED。 */
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
