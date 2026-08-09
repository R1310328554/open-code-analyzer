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
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.subscribers.SinglePostCompleteSubscriber;

import java.io.Serial;
import java.util.Objects;

/**
 * 上游 onError 时，用 {@link Function} 计算替代值发射后 onComplete，
 * 而非向下游传播错误。
 * @param <T> 元素类型
 */
public final class FlowableOnErrorReturn<T> extends AbstractFlowableWithUpstream<T, T> {
    final Function<? super Throwable, ? extends T> valueSupplier;
    /**
     * @param source 上游 Flowable
     * @param valueSupplier 根据错误计算替代值的函数
     */
    public FlowableOnErrorReturn(Flowable<T> source, Function<? super Throwable, ? extends T> valueSupplier) {
        super(source);
        this.valueSupplier = valueSupplier;
    }

    /** 订阅 OnErrorReturnSubscriber。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new OnErrorReturnSubscriber<>(s, valueSupplier));
    }

    /** onError 时发射 valueSupplier 返回值并完成。 */
    static final class OnErrorReturnSubscriber<T>
    extends SinglePostCompleteSubscriber<T, T> {

        @Serial
        private static final long serialVersionUID = -3740826063558713822L;
        final Function<? super Throwable, ? extends T> valueSupplier;

        OnErrorReturnSubscriber(Subscriber<? super T> actual, Function<? super Throwable, ? extends T> valueSupplier) {
            super(actual);
            this.valueSupplier = valueSupplier;
        }

        @Override
        public void onNext(T t) {
            produced++;
            downstream.onNext(t);
        }

        /** 调用 valueSupplier；成功则 complete(v)，失败则 CompositeException。 */
        @Override
        public void onError(Throwable t) {
            T v;
            try {
                v = Objects.requireNonNull(valueSupplier.apply(t), "The valueSupplier returned a null value");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(t, ex));
                return;
            }
            complete(v);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
