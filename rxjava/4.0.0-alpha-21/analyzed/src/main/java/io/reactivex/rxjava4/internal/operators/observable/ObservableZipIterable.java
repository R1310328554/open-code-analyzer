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

import java.util.Iterator;
import java.util.Objects;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.BiFunction;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将 Observable 每项与 Iterable 迭代器当前项 zip 后发射；
 * Iterable 耗尽时 onComplete。
 * @param <T> Observable 元素类型
 * @param <U> Iterable 元素类型
 * @param <V> zipper 输出类型
 */
public final class ObservableZipIterable<T, U, V> extends Observable<V> {
    final Observable<? extends T> source;
    final Iterable<U> other;
    final BiFunction<? super T, ? super U, ? extends V> zipper;

    /**
     * @param source 主 Observable
     * @param other 同步 Iterable
     * @param zipper (T, U) -> V
     */
    public ObservableZipIterable(
            Observable<? extends T> source,
            Iterable<U> other, BiFunction<? super T, ? super U, ? extends V> zipper) {
        this.source = source;
        this.other = other;
        this.zipper = zipper;
    }

    /** 校验 iterator/hasNext；空 Iterable 直接 complete。 */
    @Override
    public void subscribeActual(Observer<? super V> t) {
        Iterator<U> it;

        try {
            it = Objects.requireNonNull(other.iterator(), "The iterator returned by other is null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, t);
            return;
        }

        boolean b;

        try {
            b = it.hasNext();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, t);
            return;
        }

        if (!b) {
            EmptyDisposable.complete(t);
            return;
        }

        source.subscribe(new ZipIterableObserver<T, U, V>(t, it, zipper));
    }

    /** 每项 iterator.next 与 zipper 合并；无下一项时 dispose 并 onComplete。 */
    static final class ZipIterableObserver<T, U, V> implements Observer<T>, Disposable {
        final Observer<? super V> downstream;
        final Iterator<U> iterator;
        final BiFunction<? super T, ? super U, ? extends V> zipper;

        Disposable upstream;

        boolean done;

        ZipIterableObserver(Observer<? super V> actual, Iterator<U> iterator,
                BiFunction<? super T, ? super U, ? extends V> zipper) {
            this.downstream = actual;
            this.iterator = iterator;
            this.zipper = zipper;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** next+zipper；hasNext 为 false 时结束流。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            U u;

            try {
                u = Objects.requireNonNull(iterator.next(), "The iterator returned a null value");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                fail(e);
                return;
            }

            V v;
            try {
                v = Objects.requireNonNull(zipper.apply(t, u), "The zipper function returned a null value");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                fail(e);
                return;
            }

            downstream.onNext(v);

            boolean b;

            try {
                b = iterator.hasNext();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                fail(e);
                return;
            }

            if (!b) {
                done = true;
                upstream.dispose();
                downstream.onComplete();
            }
        }

        void fail(Throwable e) {
            done = true;
            upstream.dispose();
            downstream.onError(e);
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

    }
}
