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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 延迟对主 {@link Maybe} 的订阅，
 * 直到 other {@link Publisher} 发射元素或完成。
 *
 * @param <T> 主 Maybe 元素类型
 * @param <U> other Publisher 元素类型
 */
public final class MaybeDelaySubscriptionOtherPublisher<T, U> extends AbstractMaybeWithUpstream<T, T> {

    final Publisher<U> other;

    /**
     * @param source 主 Maybe
     * @param other 触发订阅时机的 Publisher
     */
    public MaybeDelaySubscriptionOtherPublisher(MaybeSource<T> source, Publisher<U> other) {
        super(source);
        this.other = other;
    }

    /** 先订阅 other，就绪后再订阅主 Maybe。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        other.subscribe(new OtherSubscriber<>(observer, source));
    }

    /** 等待 other 首项或 onComplete 后订阅主 Maybe。 */
    static final class OtherSubscriber<T> implements FlowableSubscriber<Object>, Disposable {
        final DelayMaybeObserver<T> main;

        MaybeSource<T> source;

        Subscription upstream;

        OtherSubscriber(MaybeObserver<? super T> actual, MaybeSource<T> source) {
            this.main = new DelayMaybeObserver<>(actual);
            this.source = source;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                main.downstream.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(Object t) {
            if (upstream != SubscriptionHelper.CANCELLED) {
                upstream.cancel();
                upstream = SubscriptionHelper.CANCELLED;

                subscribeNext();
            }
        }

        @Override
        public void onError(Throwable t) {
            if (upstream != SubscriptionHelper.CANCELLED) {
                upstream = SubscriptionHelper.CANCELLED;

                main.downstream.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (upstream != SubscriptionHelper.CANCELLED) {
                upstream = SubscriptionHelper.CANCELLED;

                subscribeNext();
            }
        }

        /** other 就绪后订阅主 MaybeSource。 */
        void subscribeNext() {
            MaybeSource<T> src = source;
            source = null;

            src.subscribe(main);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(main.get());
        }

        @Override
        public void dispose() {
            upstream.cancel();
            upstream = SubscriptionHelper.CANCELLED;
            DisposableHelper.dispose(main);
        }
    }

    /** 透传主 Maybe 的 onSuccess/onError/onComplete。 */
    static final class DelayMaybeObserver<T> extends AtomicReference<Disposable>
    implements MaybeObserver<T> {

        @Serial
        private static final long serialVersionUID = 706635022205076709L;

        final MaybeObserver<? super T> downstream;

        DelayMaybeObserver(MaybeObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
