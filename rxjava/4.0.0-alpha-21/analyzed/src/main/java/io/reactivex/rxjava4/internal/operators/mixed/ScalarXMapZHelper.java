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

package io.reactivex.rxjava4.internal.operators.mixed;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.operators.maybe.MaybeToObservable;
import io.reactivex.rxjava4.internal.operators.single.SingleToObservable;

import java.util.Objects;

/**
 * 从标量源（实现 {@link Supplier} 的响应式类型）提取值，
 * 映射为 0-1 类型后直接订阅下游 consumer，跳过常规订阅通道开销。
 * <p>History: 2.1.11 - experimental
 * @since 2.2
 */
final class ScalarXMapZHelper {

    private ScalarXMapZHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 尝试从标量源（{@link Supplier}）映射并订阅 {@link CompletableSource}。
     * @param <T> 上游元素类型
     * @param source 可能实现 {@link Supplier} 的源（{@code Flowable} 或 {@code Observable}）
     * @param mapper 将标量值转为 {@link CompletableSource} 的函数
     * @param observer 订阅映射后 CompletableSource 的 consumer
     * @return 若已订阅且应跳过常规路径则为 true
     */
    static <T> boolean tryAsCompletable(Object source,
            Function<? super T, ? extends CompletableSource> mapper,
            CompletableObserver observer) {
        if (source instanceof Supplier) {
            @SuppressWarnings("unchecked")
            Supplier<T> supplier = (Supplier<T>) source;
            CompletableSource cs = null;
            try {
                T item = supplier.get();
                if (item != null) {
                    cs = Objects.requireNonNull(mapper.apply(item), "The mapper returned a null CompletableSource");
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }

            if (cs == null) {
                EmptyDisposable.complete(observer);
            } else {
                cs.subscribe(observer);
            }
            return true;
        }
        return false;
    }

    /**
     * 尝试从标量源（{@link Supplier}）映射并订阅 {@link MaybeSource}。
     * @param <T> 上游元素类型
     * @param <R> 下游元素类型
     * @param source 可能实现 {@link Supplier} 的源（{@code Flowable} 或 {@code Observable}）
     * @param mapper 将标量值转为 {@link MaybeSource} 的函数
     * @param observer 订阅映射后 MaybeSource 的 consumer
     * @return 若已订阅且应跳过常规路径则为 true
     */
    static <T, R> boolean tryAsMaybe(Object source,
            Function<? super T, ? extends MaybeSource<? extends R>> mapper,
            Observer<? super R> observer) {
        if (source instanceof Supplier) {
            @SuppressWarnings("unchecked")
            Supplier<T> supplier = (Supplier<T>) source;
            MaybeSource<? extends R> cs = null;
            try {
                T item = supplier.get();
                if (item != null) {
                    cs = Objects.requireNonNull(mapper.apply(item), "The mapper returned a null MaybeSource");
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }

            if (cs == null) {
                EmptyDisposable.complete(observer);
            } else {
                cs.subscribe(MaybeToObservable.create(observer));
            }
            return true;
        }
        return false;
    }

    /**
     * 尝试从标量源（{@link Supplier}）映射并订阅 {@link SingleSource}。
     * @param <T> 上游元素类型
     * @param <R> 下游元素类型
     * @param source 可能实现 {@link Supplier} 的源（{@code Flowable} 或 {@code Observable}）
     * @param mapper 将标量值转为 {@link SingleSource} 的函数
     * @param observer 订阅映射后 SingleSource 的 consumer
     * @return 若已订阅且应跳过常规路径则为 true
     */
    static <T, R> boolean tryAsSingle(Object source,
            Function<? super T, ? extends SingleSource<? extends R>> mapper,
            Observer<? super R> observer) {
        if (source instanceof Supplier) {
            @SuppressWarnings("unchecked")
            Supplier<T> supplier = (Supplier<T>) source;
            SingleSource<? extends R> cs = null;
            try {
                T item = supplier.get();
                if (item != null) {
                    cs = Objects.requireNonNull(mapper.apply(item), "The mapper returned a null SingleSource");
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }

            if (cs == null) {
                EmptyDisposable.complete(observer);
            } else {
                cs.subscribe(SingleToObservable.create(observer));
            }
            return true;
        }
        return false;
    }
}
