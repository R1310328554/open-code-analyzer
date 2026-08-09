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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.io.Serial;
import java.util.Objects;

/**
 * 上游 onError 时，根据 {@link Function} 切换到备用 {@link Publisher} 继续发射。
 * 使用 {@link SubscriptionArbiter} 协调主序列与备用序列的背压。
 * @param <T> 元素类型
 */
public final class FlowableOnErrorNext<T> extends AbstractFlowableWithUpstream<T, T> {
    final Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier;

    /**
     * @param source 上游 Flowable
     * @param nextSupplier 接收错误并返回备用 Publisher 的函数
     */
    public FlowableOnErrorNext(Flowable<T> source,
            Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier) {
        super(source);
        this.nextSupplier = nextSupplier;
    }

    /** 安装 OnErrorNextSubscriber 并订阅上游。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        OnErrorNextSubscriber<T> parent = new OnErrorNextSubscriber<>(s, nextSupplier);
        s.onSubscribe(parent);
        source.subscribe(parent);
    }

    /** 首次 onError 时切换备用 Publisher；后续错误直接传播。 */
    static final class OnErrorNextSubscriber<T>
    extends SubscriptionArbiter
    implements FlowableSubscriber<T> {
        @Serial
        private static final long serialVersionUID = 4063763155303814625L;

        final Subscriber<? super T> downstream;

        final Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier;

        boolean once;

        boolean done;

        long produced;

        OnErrorNextSubscriber(Subscriber<? super T> actual, Function<? super Throwable, ? extends Publisher<? extends T>> nextSupplier) {
            super(false);
            this.downstream = actual;
            this.nextSupplier = nextSupplier;
        }

        @Override
        public void onSubscribe(Subscription s) {
            setSubscription(s);
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            if (!once) {
                produced++;
            }
            downstream.onNext(t);
        }

        /** 调用 nextSupplier 订阅备用流；supplier 异常则 CompositeException。 */
        @Override
        public void onError(Throwable t) {
            if (once) {
                if (done) {
                    RxJavaPlugins.onError(t);
                    return;
                }
                downstream.onError(t);
                return;
            }
            once = true;

            Publisher<? extends T> p;

            try {
                p = Objects.requireNonNull(nextSupplier.apply(t), "The nextSupplier returned a null Publisher");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                downstream.onError(new CompositeException(t, e));
                return;
            }

            long mainProduced = produced;
            if (mainProduced != 0L) {
                produced(mainProduced);
            }

            p.subscribe(this);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            once = true;
            downstream.onComplete();
        }
    }
}
