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
import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;

/**
 * 将 Single 的 onSuccess 值经 mapper 映射为 Publisher，
 * 订阅 inner Publisher 并以 Flowable 形式转发其元素（支持背压）。
 * <p>
 * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/Single.flatMapPublisher.v3.png" alt="">
 * <dl>
 *  <dt><b>Backpressure:</b></dt>
 *  <dd>返回的 {@code Flowable} 遵守下游背压，mapper 返回的 {@code Publisher} 亦应遵守。</dd>
 * <dt><b>Scheduler:</b></dt>
 * <dd>{@code flatMapPublisher} 默认不在特定 {@link Scheduler} 上运行。</dd>
 * </dl>
 * 
 * @param <T> 上游成功值类型
 * @param <R> 下游元素类型
 * 
 * @see <a href="http://reactivex.io/documentation/operators/flatmap.html">ReactiveX operators documentation: FlatMap</a>
 * @since 2.1.15
 */
public final class SingleFlatMapPublisher<T, R> extends Flowable<R> {

    final SingleSource<T> source;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;

    /**
     * @param source 上游 SingleSource
     * @param mapper 将成功值映射为 Publisher 的函数
     */
    public SingleFlatMapPublisher(SingleSource<T> source,
            Function<? super T, ? extends Publisher<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    /** 订阅 SingleFlatMapPublisherObserver 在 onSuccess 时 flatMap inner Publisher。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> downstream) {
        source.subscribe(new SingleFlatMapPublisherObserver<>(downstream, mapper));
    }

    /** 兼作 SingleObserver 与 FlowableSubscriber：deferredSetOnce 协调背压 request。 */
    static final class SingleFlatMapPublisherObserver<S, T> extends AtomicLong
            implements SingleObserver<S>, FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = 7759721921468635667L;

        final Subscriber<? super T> downstream;
        final Function<? super S, ? extends Publisher<? extends T>> mapper;
        final AtomicReference<Subscription> parent;
        Disposable disposable;

        SingleFlatMapPublisherObserver(Subscriber<? super T> actual,
                Function<? super S, ? extends Publisher<? extends T>> mapper) {
            this.downstream = actual;
            this.mapper = mapper;
            this.parent = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            this.disposable = d;
            downstream.onSubscribe(this);
        }

        /** mapper 获取 Publisher 并 subscribe(this) 转发 onNext/onComplete/onError。 */
        @Override
        public void onSuccess(S value) {
            Publisher<? extends T> f;
            try {
                f = Objects.requireNonNull(mapper.apply(value), "the mapper returned a null Publisher");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                downstream.onError(e);
                return;
            }
            if (parent.get() != SubscriptionHelper.CANCELLED) {
                f.subscribe(this);
            }
        }

        @Override
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.deferredSetOnce(parent, this, s);
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        /** SubscriptionHelper.deferredRequest 将 request 转发至 inner Subscription。 */
        @Override
        public void request(long n) {
            SubscriptionHelper.deferredRequest(parent, this, n);
        }

        @Override
        public void cancel() {
            disposable.dispose();
            SubscriptionHelper.cancel(parent);
        }
    }

}
