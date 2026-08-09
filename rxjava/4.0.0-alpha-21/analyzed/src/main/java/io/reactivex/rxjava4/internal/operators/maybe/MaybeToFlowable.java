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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.fuseable.HasUpstreamMaybeSource;
import io.reactivex.rxjava4.internal.subscriptions.DeferredScalarSubscription;

import java.io.Serial;

/**
 * 将 {@link MaybeSource} 包装为 {@link Flowable}，
 * 以支持背压的方式转发信号，并贯通取消。
 *
 * @param <T> 元素类型
 */
public final class MaybeToFlowable<T> extends Flowable<T> implements HasUpstreamMaybeSource<T> {

    final MaybeSource<T> source;

    /** @param source 上游 MaybeSource */
    public MaybeToFlowable(MaybeSource<T> source) {
        this.source = source;
    }

    @Override
    public MaybeSource<T> source() {
        return source;
    }

    /** 订阅 MaybeToFlowableSubscriber 并转发 Maybe 信号。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new MaybeToFlowableSubscriber<>(s));
    }

    /** onSuccess 经 DeferredScalarSubscription 背压发射；cancel 时 dispose 上游。 */
    static final class MaybeToFlowableSubscriber<T> extends DeferredScalarSubscription<T>
    implements MaybeObserver<T> {

        @Serial
        private static final long serialVersionUID = 7603343402964826922L;

        Disposable upstream;

        MaybeToFlowableSubscriber(Subscriber<? super T> downstream) {
            super(downstream);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            complete(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void cancel() {
            super.cancel();
            upstream.dispose();
        }
    }
}
