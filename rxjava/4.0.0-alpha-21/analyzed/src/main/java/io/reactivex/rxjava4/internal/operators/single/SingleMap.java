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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;

import java.util.Objects;

/**
 * 对上游 onSuccess 值应用 mapper 后转发给 downstream。
 * mapper 返回 null 或抛异常时走 onError 路径。
 * @param <T> 上游元素类型
 * @param <R> 映射后元素类型
 */
public final class SingleMap<T, R> extends Single<R> {
    final SingleSource<? extends T> source;

    final Function<? super T, ? extends R> mapper;

    /**
     * @param source 上游 SingleSource
     * @param mapper 成功值的映射函数
     */
    public SingleMap(SingleSource<? extends T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    /** 订阅 MapSingleObserver 拦截 onSuccess 并应用 mapper。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super R> t) {
        source.subscribe(new MapSingleObserver<T, R>(t, mapper));
    }

    /** 成功时 mapper.apply；null 或异常转 onError；错误直接转发。 */
    record MapSingleObserver<T, R>(SingleObserver<? super R> t,
                                   Function<? super T, ? extends R> mapper) implements SingleObserver<T> {

        @Override
            public void onSubscribe(Disposable d) {
                t.onSubscribe(d);
            }

            @Override
            /** 应用 mapper，非 null 结果则 t.onSuccess(v)。 */
            public void onSuccess(T value) {
                R v;
                try {
                    v = Objects.requireNonNull(mapper.apply(value), "The mapper function returned a null value.");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    onError(e);
                    return;
                }

                t.onSuccess(v);
            }

            @Override
            public void onError(Throwable e) {
                t.onError(e);
            }
        }
}
