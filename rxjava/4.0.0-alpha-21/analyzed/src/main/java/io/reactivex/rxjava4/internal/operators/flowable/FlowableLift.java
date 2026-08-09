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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将自定义算子提升（lift）进 Publisher 链。
 *
 * <p>使用具体 Publisher 作为 lift 载体后，算子融合可通过类型转换识别源与内部操作，
 * 优于基于 lambda 的实现。
 *
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 */
public final class FlowableLift<R, T> extends AbstractFlowableWithUpstream<T, R> {
    /** 实际 lift 算子。 */
    final FlowableOperator<? extends R, ? super T> operator;

    /**
     * @param source 上游 Flowable
     * @param operator 将下游 Subscriber 转换为上游 Subscriber 的算子
     */
    public FlowableLift(Flowable<T> source, FlowableOperator<? extends R, ? super T> operator) {
        super(source);
        this.operator = operator;
    }

    /** 应用 operator 得到上游 Subscriber 并订阅 source。 */
    @Override
    public void subscribeActual(Subscriber<? super R> s) {
        try {
            Subscriber<? super T> st = operator.apply(s);

            if (st == null) {
                throw new NullPointerException("Operator " + operator + " returned a null Subscriber");
            }

            source.subscribe(st);
        } catch (NullPointerException e) { // NOPMD
            throw e;
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // 无法调用 onError，因不确定 Subscription 是否已设置
            // 无法调用 onSubscribe，因 apply 可能已设置 Subscription
            RxJavaPlugins.onError(e);

            NullPointerException npe = new NullPointerException("Actually not, but can't throw other exceptions due to RS");
            npe.initCause(e);
            throw npe;
        }
    }
}
