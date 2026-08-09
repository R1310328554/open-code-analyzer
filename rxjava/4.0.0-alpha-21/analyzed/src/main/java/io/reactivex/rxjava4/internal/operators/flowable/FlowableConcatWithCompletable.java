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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 先订阅主 {@link Flowable}，正常完成后订阅 {@link CompletableSource}，
 * 并在 Completable 终止时结束序列。
 * <p>History: 2.1.10 - experimental
 * @param <T> 主源及输出元素类型
 * @since 2.2
 */
public final class FlowableConcatWithCompletable<T> extends AbstractFlowableWithUpstream<T, T> {

    final CompletableSource other;

    /**
     * @param source 主 Flowable 源
     * @param other 主源完成后订阅的 CompletableSource
     */
    public FlowableConcatWithCompletable(Flowable<T> source, CompletableSource other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new ConcatWithSubscriber<>(s, other));
    }

    /** 主 Flowable 完成后切换订阅 Completable 并转发终止事件。 */
    static final class ConcatWithSubscriber<T>
    extends AtomicReference<Disposable>
    implements FlowableSubscriber<T>, CompletableObserver, Subscription {

        @Serial
        private static final long serialVersionUID = -7346385463600070225L;

        final Subscriber<? super T> downstream;

        Subscription upstream;

        CompletableSource other;

        boolean inCompletable;

        ConcatWithSubscriber(Subscriber<? super T> actual, CompletableSource other) {
            this.downstream = actual;
            this.other = other;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        /** 主源完成时订阅 other Completable，Completable 完成后再通知下游。 */
        @Override
        public void onComplete() {
            if (inCompletable) {
                downstream.onComplete();
            } else {
                inCompletable = true;
                upstream = SubscriptionHelper.CANCELLED;
                CompletableSource cs = other;
                other = null;
                cs.subscribe(this);
            }
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
            DisposableHelper.dispose(this);
        }
    }
}
