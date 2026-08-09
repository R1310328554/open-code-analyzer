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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.operators.flowable.FlowableRepeatWhen.*;
import io.reactivex.rxjava4.internal.subscriptions.EmptySubscription;
import io.reactivex.rxjava4.processors.*;
import io.reactivex.rxjava4.subscribers.SerializedSubscriber;

import java.io.Serial;
import java.util.Objects;

/**
 * 上游 onError 时将错误推入 handler 的 {@link FlowableProcessor}，
 * 由 handler 返回的 Publisher 决定是否重新订阅。
 * @param <T> 元素类型
 */
public final class FlowableRetryWhen<T> extends AbstractFlowableWithUpstream<T, T> {
    final Function<? super Flowable<Throwable>, ? extends Publisher<?>> handler;

    /**
     * @param source 上游 Flowable
     * @param handler 接收错误流并返回控制 Publisher
     */
    public FlowableRetryWhen(Flowable<T> source,
            Function<? super Flowable<Throwable>, ? extends Publisher<?>> handler) {
        super(source);
        this.handler = handler;
    }

    /** 复用 {@link FlowableRepeatWhen} 的 WhenReceiver/WhenSourceSubscriber 机制。 */
    @Override
    public void subscribeActual(Subscriber<? super T> s) {
        SerializedSubscriber<T> z = new SerializedSubscriber<>(s);

        FlowableProcessor<Throwable> processor = UnicastProcessor.<Throwable>create(8).toSerialized();

        Publisher<?> when;

        try {
            when = Objects.requireNonNull(handler.apply(processor), "handler returned a null Publisher");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptySubscription.error(ex, s);
            return;
        }

        WhenReceiver<T, Throwable> receiver = new WhenReceiver<>(source);

        RetryWhenSubscriber<T> subscriber = new RetryWhenSubscriber<>(z, processor, receiver);

        receiver.subscriber = subscriber;

        s.onSubscribe(subscriber);

        when.subscribe(receiver);

        receiver.onNext(0);
    }

    /** onError 时 again(t) 请求重试；onComplete 正常结束。 */
    static final class RetryWhenSubscriber<T> extends WhenSourceSubscriber<T, Throwable> {

        @Serial
        private static final long serialVersionUID = -2680129890138081029L;

        RetryWhenSubscriber(Subscriber<? super T> actual, FlowableProcessor<Throwable> processor,
                Subscription receiver) {
            super(actual, processor, receiver);
        }

        /** 将错误作为重复信号交给 handler 链。 */
        @Override
        public void onError(Throwable t) {
            again(t);
        }

        @Override
        public void onComplete() {
            receiver.cancel();
            downstream.onComplete();
        }
    }

}
