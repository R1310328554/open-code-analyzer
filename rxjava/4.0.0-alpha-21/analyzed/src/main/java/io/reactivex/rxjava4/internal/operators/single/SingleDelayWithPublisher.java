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

package io.reactivex.rxjava4.internal.operators.single;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 先订阅 other {@link Publisher}，收到首个 onNext 或 onComplete 后再订阅 source Single。
 * 通过 request(Long.MAX_VALUE) 无界拉取 other；dispose 时 cancel 上游。
 * @param <T> 目标 Single 元素类型
 * @param <U> other Publisher 元素类型
 */
public final class SingleDelayWithPublisher<T, U> extends Single<T> {

    final SingleSource<T> source;

    final Publisher<U> other;

    /**
     * @param source 待延迟订阅的 SingleSource
     * @param other 必须先发出或完成的 Publisher
     */
    public SingleDelayWithPublisher(SingleSource<T> source, Publisher<U> other) {
        this.source = source;
        this.other = other;
    }

    /** 订阅 OtherSubscriber 并 request 无界等待 other 首事件。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        other.subscribe(new OtherSubscriber<>(observer, source));
    }

    /** other 的 FlowableSubscriber：onNext 时 cancel 并触发完成逻辑。 */
    static final class OtherSubscriber<T, U>
    extends AtomicReference<Disposable>
    implements FlowableSubscriber<U>, Disposable {

        @Serial
        private static final long serialVersionUID = -8565274649390031272L;

        final SingleObserver<? super T> downstream;

        final SingleSource<T> source;

        boolean done;

        Subscription upstream;

        OtherSubscriber(SingleObserver<? super T> actual, SingleSource<T> source) {
            this.downstream = actual;
            this.source = source;
        }

        /** 校验 Subscription 后 request(Long.MAX_VALUE) 无界拉取。 */
        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(U value) {
            upstream.cancel();
            onComplete();
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaPlugins.onError(e);
                return;
            }
            done = true;
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            source.subscribe(new ResumeSingleObserver<>(this, downstream));
        }

        /** cancel 上游 Subscription 并 dispose 自身 Disposable。 */
        @Override
        public void dispose() {
            upstream.cancel();
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
