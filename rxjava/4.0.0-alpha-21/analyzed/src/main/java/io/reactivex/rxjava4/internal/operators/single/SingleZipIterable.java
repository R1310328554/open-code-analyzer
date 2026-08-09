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

import java.util.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.operators.single.SingleZipArray.ZipCoordinator;

/**
 * 将 Iterable 中的 SingleSource 收集为数组后 zip：
 * 全部 onSuccess 后以 zipper 合并为 R。
 *
 * @param <T> 各上游元素类型
 * @param <R> 合并结果类型
 */
public final class SingleZipIterable<T, R> extends Single<R> {

    final Iterable<? extends SingleSource<? extends T>> sources;

    final Function<? super Object[], ? extends R> zipper;

    /**
     * @param sources 上游 SingleSource 的 Iterable
     * @param zipper 将 Object[] 合并为 R 的函数
     */
    public SingleZipIterable(Iterable<? extends SingleSource<? extends T>> sources, Function<? super Object[], ? extends R> zipper) {
        this.sources = sources;
        this.zipper = zipper;
    }

    /** 迭代收集 sources 到数组，复用 ZipCoordinator 并行订阅。 */
    @Override
    protected void subscribeActual(SingleObserver<? super R> observer) {
        @SuppressWarnings("unchecked")
        SingleSource<? extends T>[] a = new SingleSource[8];
        int n = 0;

        try {
            for (SingleSource<? extends T> source : sources) {
                if (source == null) {
                    EmptyDisposable.error(new NullPointerException("One of the sources is null"), observer);
                    return;
                }
                if (n == a.length) {
                    a = Arrays.copyOf(a, n + (n >> 2));
                }
                a[n++] = source;
            }
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        if (n == 0) {
            EmptyDisposable.error(new NoSuchElementException(), observer);
            return;
        }

        if (n == 1) {
            a[0].subscribe(new SingleMap.MapSingleObserver<>(observer, new SingletonArrayFunc()));
            return;
        }

        ZipCoordinator<T, R> parent = new ZipCoordinator<>(observer, n, zipper);

        observer.onSubscribe(parent);

        for (int i = 0; i < n; i++) {
            if (parent.isDisposed()) {
                return;
            }

            a[i].subscribe(parent.observers[i]);
        }
    }

    final class SingletonArrayFunc implements Function<T, R> {
        @Override
        public R apply(T t) throws Throwable {
            return Objects.requireNonNull(zipper.apply(new Object[] { t }), "The zipper returned a null value");
        }
    }
}
