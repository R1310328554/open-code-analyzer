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
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 当下游无 demand 时丢弃上游元素；可向 {@link Consumer} 通知被丢弃项。
 * @param <T> 元素类型
 */
public final class FlowableOnBackpressureDrop<T> extends AbstractFlowableWithUpstream<T, T> implements Consumer<T> {

    final Consumer<? super T> onDrop;

    /** @param source 上游 Flowable（丢弃时不回调） */
    public FlowableOnBackpressureDrop(Flowable<T> source) {
        super(source);
        this.onDrop = this;
    }

    /**
     * @param source 上游 Flowable
     * @param onDrop 元素被丢弃时的回调
     */
    public FlowableOnBackpressureDrop(Flowable<T> source, Consumer<? super T> onDrop) {
        super(source);
        this.onDrop = onDrop;
    }

    /** 默认 onDrop 实现：静默忽略被丢弃元素。 */
    @Override
    public void accept(T t) {
        // 故意忽略被丢弃的元素
    }

    /** 订阅上游；无 demand 时丢弃 onNext。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        this.source.subscribe(new BackpressureDropSubscriber<>(s, onDrop));
    }

    /** 以 AtomicLong 跟踪 demand，无余量时调用 onDrop。 */
    static final class BackpressureDropSubscriber<T>
    extends AtomicLong implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -6246093802440953054L;

        final Subscriber<? super T> downstream;
        final Consumer<? super T> onDrop;

        Subscription upstream;

        boolean done;

        BackpressureDropSubscriber(Subscriber<? super T> actual, Consumer<? super T> onDrop) {
            this.downstream = actual;
            this.onDrop = onDrop;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        /** demand > 0 时转发，否则调用 onDrop 丢弃。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            long r = get();
            if (r != 0L) {
                downstream.onNext(t);
                BackpressureHelper.produced(this, 1);
            } else {
                try {
                    onDrop.accept(t);
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    onError(e);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            downstream.onComplete();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this, n);
            }
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }
    }
}
