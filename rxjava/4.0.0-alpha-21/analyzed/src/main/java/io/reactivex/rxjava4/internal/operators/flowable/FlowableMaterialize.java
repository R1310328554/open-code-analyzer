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
import io.reactivex.rxjava4.internal.subscribers.SinglePostCompleteSubscriber;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.io.Serial;

/**
 * 将上游 onNext/onError/onComplete 包装为 {@link Notification} 向下游发射。
 * @param <T> 元素类型
 */
public final class FlowableMaterialize<T> extends AbstractFlowableWithUpstream<T, Notification<T>> {

    /** @param source 上游 Flowable */
    public FlowableMaterialize(Flowable<T> source) {
        super(source);
    }

    /** 订阅上游并将每个事件物化为 Notification。 */
    @Override
    protected void subscribeActual(Subscriber<? super Notification<T>> s) {
        source.subscribe(new MaterializeSubscriber<>(s));
    }

    /** 将三类事件转换为对应 Notification 的 subscriber。 */
    static final class MaterializeSubscriber<T> extends SinglePostCompleteSubscriber<T, Notification<T>> {

        @Serial
        private static final long serialVersionUID = -3740826063558713822L;

        MaterializeSubscriber(Subscriber<? super Notification<T>> downstream) {
            super(downstream);
        }

        /** 发射 {@link Notification#createOnNext}。 */
        @Override
        public void onNext(T t) {
            produced++;
            downstream.onNext(Notification.createOnNext(t));
        }

        /** 以 {@link Notification#createOnError} 完成序列。 */
        @Override
        public void onError(Throwable t) {
            complete(Notification.createOnError(t));
        }

        /** 以 {@link Notification#createOnComplete} 完成序列。 */
        @Override
        public void onComplete() {
            complete(Notification.createOnComplete());
        }

        /** 被丢弃的 onError Notification 通过 {@link RxJavaPlugins#onError} 上报。 */
        @Override
        protected void onDrop(Notification<T> n) {
            if (n.isOnError()) {
                RxJavaPlugins.onError(n.getError());
            }
        }
    }
}
