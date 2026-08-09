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
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.util.Objects;

/**
 * 处理标量源 XMap 算子（X 为 flat、concat、switch）的工具类。
 */
public final class FlowableScalarXMap {

    /** 工具类，禁止实例化。 */
    private FlowableScalarXMap() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 尝试订阅可能为 {@link Supplier} 的源经 mapper 映射后的 {@link Publisher}。
     * @param <T> 输入值类型
     * @param <R> 输出值类型
     * @param source 源 Publisher
     * @param subscriber 下游 subscriber
     * @param mapper 将标量值映射为 Publisher 的函数
     * @return 成功处理标量路径时 true，否则调用方应走常规路径
     */
    @SuppressWarnings("unchecked")
    public static <T, R> boolean tryScalarXMapSubscribe(Publisher<T> source,
            Subscriber<? super R> subscriber,
            Function<? super T, ? extends Publisher<? extends R>> mapper) {
        if (source instanceof Supplier) {
            T t;

            try {
                t = ((Supplier<T>)source).get();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptySubscription.error(ex, subscriber);
                return true;
            }

            if (t == null) {
                EmptySubscription.complete(subscriber);
                return true;
            }

            Publisher<? extends R> r;

            try {
                r = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Publisher");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptySubscription.error(ex, subscriber);
                return true;
            }

            if (r instanceof Supplier) {
                R u;

                try {
                    u = ((Supplier<R>)r).get();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    EmptySubscription.error(ex, subscriber);
                    return true;
                }

                if (u == null) {
                    EmptySubscription.complete(subscriber);
                    return true;
                }
                subscriber.onSubscribe(new ScalarSubscription<>(subscriber, u));
            } else {
                r.subscribe(subscriber);
            }

            return true;
        }
        return false;
    }

    /**
     * 将标量值映射为 {@link Publisher} 并发射其元素。
     *
     * @param <T> 标量值类型
     * @param <U> 输出元素类型
     * @param value 待映射的标量值
     * @param mapper 接收标量值并返回待订阅 Publisher 的函数
     * @return 新的 {@link Flowable} 实例
     */
    public static <T, U> Flowable<U> scalarXMap(final T value, final Function<? super T, ? extends Publisher<? extends U>> mapper) {
        return RxJavaPlugins.onAssembly(new ScalarXMapFlowable<>(value, mapper));
    }

    /**
     * 将标量值映射为 {@link Publisher} 并订阅。
     *
     * @param <T> 标量值类型
     * @param <R> 映射后 Publisher 的元素类型
     */
    static final class ScalarXMapFlowable<T, R> extends Flowable<R> {

        final T value;

        final Function<? super T, ? extends Publisher<? extends R>> mapper;

        ScalarXMapFlowable(T value,
                Function<? super T, ? extends Publisher<? extends R>> mapper) {
            this.value = value;
            this.mapper = mapper;
        }

        /** 应用 mapper；若结果为 Supplier 则走标量订阅，否则直接 subscribe。 */
        @SuppressWarnings("unchecked")
        @Override
        public void subscribeActual(Subscriber<? super R> s) {
            Publisher<? extends R> other;
            try {
                other = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null Publisher");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                EmptySubscription.error(e, s);
                return;
            }
            if (other instanceof Supplier) {
                R u;

                try {
                    u = ((Supplier<R>)other).get();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    EmptySubscription.error(ex, s);
                    return;
                }

                if (u == null) {
                    EmptySubscription.complete(s);
                    return;
                }
                s.onSubscribe(new ScalarSubscription<>(s, u));
            } else {
                other.subscribe(s);
            }
        }
    }
}
