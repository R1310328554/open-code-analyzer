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

package io.reactivex.rxjava4.internal.operators.mixed;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * {@link CompletableSource} 正常完成后，
 * 再订阅并转发 {@link Publisher} 的信号。
 *
 * @param <R> Publisher 及本算子的结果类型
 * @since 2.1.15
 */
public final class CompletableAndThenPublisher<R> extends Flowable<R> {

    final CompletableSource source;

    final Publisher<? extends R> other;

    /**
     * @param source 先执行的 CompletableSource
     * @param other source 完成后订阅的 Publisher
     */
    public CompletableAndThenPublisher(CompletableSource source,
            Publisher<? extends R> other) {
        this.source = source;
        this.other = other;
    }

    /** 先订阅 source，完成后再订阅 other。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new AndThenPublisherSubscriber<R>(s, other));
    }

    /** 先作为 CompletableObserver 等待 source 完成，再作为 Subscriber 转发 other 信号。 */
    static final class AndThenPublisherSubscriber<R>
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<R>, CompletableObserver, Subscription {

        @Serial
        private static final long serialVersionUID = -8948264376121066672L;

        final Subscriber<? super R> downstream;

        Publisher<? extends R> other;

        Disposable upstream;

        final AtomicLong requested;

        AndThenPublisherSubscriber(Subscriber<? super R> downstream, Publisher<? extends R> other) {
            this.downstream = downstream;
            this.other = other;
            this.requested = new AtomicLong();
        }

        @Override
        public void onNext(R t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        /** Completable 完成时订阅 other；other 为 null 则直接 onComplete。 */
        @Override
        public void onComplete() {
            Publisher<? extends R> p = other;
            if (p == null) {
                downstream.onComplete();
            } else {
                other = null;
                p.subscribe(this);
            }
        }

        @Override
        public void request(long n) {
            SubscriptionHelper.deferredRequest(this, requested, n);
        }

        @Override
        public void cancel() {
            upstream.dispose();
            SubscriptionHelper.cancel(this);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.deferredSetOnce(this, requested, s);
        }
    }
}
