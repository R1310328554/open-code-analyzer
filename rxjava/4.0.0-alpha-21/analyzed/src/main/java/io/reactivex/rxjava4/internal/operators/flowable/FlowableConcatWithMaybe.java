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
import io.reactivex.rxjava4.internal.subscribers.SinglePostCompleteSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 先订阅主 {@link Flowable}，正常完成后订阅 {@link MaybeSource}，
 * 发出其成功值后完成，或按原样转发错误/完成信号。
 * <p>History: 2.1.10 - experimental
 * @param <T> 主源及输出元素类型
 * @since 2.2
 */
public final class FlowableConcatWithMaybe<T> extends AbstractFlowableWithUpstream<T, T> {

    final MaybeSource<? extends T> other;

    /**
     * @param source 主 Flowable 源
     * @param other 主源完成后订阅的 MaybeSource
     */
    public FlowableConcatWithMaybe(Flowable<T> source, MaybeSource<? extends T> other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new ConcatWithSubscriber<>(s, other));
    }

    /** 主 Flowable 完成后切换订阅 Maybe 并转发成功值。 */
    static final class ConcatWithSubscriber<T>
    extends SinglePostCompleteSubscriber<T, T>
    implements MaybeObserver<T> {

        @Serial
        private static final long serialVersionUID = -7346385463600070225L;

        final AtomicReference<Disposable> otherDisposable;

        MaybeSource<? extends T> other;

        boolean inMaybe;

        ConcatWithSubscriber(Subscriber<? super T> actual, MaybeSource<? extends T> other) {
            super(actual);
            this.other = other;
            this.otherDisposable = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(otherDisposable, d);
        }

        @Override
        public void onNext(T t) {
            produced++;
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        /** 将 Maybe 成功值作为最后一个元素发出后完成。 */
        @Override
        public void onSuccess(T t) {
            complete(t);
        }

        @Override
        public void onComplete() {
            if (inMaybe) {
                downstream.onComplete();
            } else {
                inMaybe = true;
                upstream = SubscriptionHelper.CANCELLED;
                MaybeSource<? extends T> ms = other;
                other = null;
                ms.subscribe(this);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            DisposableHelper.dispose(otherDisposable);
        }
    }
}
