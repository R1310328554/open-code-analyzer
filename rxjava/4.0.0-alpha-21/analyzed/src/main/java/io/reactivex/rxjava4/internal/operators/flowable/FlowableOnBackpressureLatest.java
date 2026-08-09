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

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;

import java.io.Serial;

import static java.util.concurrent.Flow.*;

/**
 * 当下游无 demand 时仅保留最新上游值，丢弃较早未消费的元素。
 * @param <T> 元素类型
 */
public final class FlowableOnBackpressureLatest<T> extends AbstractFlowableWithUpstream<T, T> {

    final Consumer<? super T> onDropped;

    /**
     * @param source 上游 Flowable
     * @param onDropped 被覆盖的旧值回调（可为 null）
     */
    public FlowableOnBackpressureLatest(Flowable<T> source, Consumer<? super T> onDropped) {
        super(source);
        this.onDropped = onDropped;
    }

    /** 订阅上游并以 latest 策略节流背压。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new BackpressureLatestSubscriber<>(s, onDropped));
    }

    /** 用 AtomicReference 保存最新值并在有 demand 时 drain。 */
    static final class BackpressureLatestSubscriber<T> extends AbstractBackpressureThrottlingSubscriber<T, T> {

        @Serial
        private static final long serialVersionUID = 163080509307634843L;

        final Consumer<? super T> onDropped;

        BackpressureLatestSubscriber(Subscriber<? super T> downstream,
                                     Consumer<? super T> onDropped) {
            super(downstream);
            this.onDropped = onDropped;
        }

        /** 替换 current 中的旧值（可选 onDropped）并触发 drain。 */
        @Override
        public void onNext(T t) {
            T oldValue = current.getAndSet(t);
            if (onDropped != null && oldValue != null) {
                try {
                    onDropped.accept(oldValue);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.cancel();
                    downstream.onError(ex);
                }
            }
            drain();
        }
    }
}
