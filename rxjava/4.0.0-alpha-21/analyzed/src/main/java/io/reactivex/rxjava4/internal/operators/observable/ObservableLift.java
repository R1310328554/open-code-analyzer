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

package io.reactivex.rxjava4.internal.operators.observable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.util.Objects;

/**
 * 将 {@link ObservableOperator} 提升（lift）进 Observable 链。
 *
 * <p>相比 lambda 版 lift，具体 {@link ObservableSource} 包装使 operator fusion
 * 可通过类型转换同时识别上游与内部操作。
 *
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 */
public final class ObservableLift<R, T> extends AbstractObservableWithUpstream<T, R> {
    /** 实际应用的 {@link ObservableOperator}。 */
    final ObservableOperator<? extends R, ? super T> operator;

    /**
     * @param source 上游 ObservableSource
     * @param operator 对下游 Observer 进行变换的算子
     */
    public ObservableLift(ObservableSource<T> source, ObservableOperator<? extends R, ? super T> operator) {
        super(source);
        this.operator = operator;
    }

    /** 调用 operator.apply 得到 lifted Observer 后订阅上游。 */
    @Override
    public void subscribeActual(Observer<? super R> observer) {
        Observer<? super T> liftedObserver;
        try {
            liftedObserver = Objects.requireNonNull(operator.apply(observer), "Operator " + operator + " returned a null Observer");
        } catch (NullPointerException e) { // NOPMD — operator 返回 null Observer
            throw e;
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // 无法 onError：尚不确定 Disposable 是否已设置
            // 无法 onSubscribe：apply 可能已设置 Disposable
            RxJavaPlugins.onError(e);

            NullPointerException npe = new NullPointerException("Actually not, but can't throw other exceptions due to RS");
            npe.initCause(e);
            throw npe;
        }

        source.subscribe(liftedObserver);
    }
}
