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

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.operators.QueueDisposable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 处理标量（Supplier）来源的 XMap 算子工具类（X 为 flat、concat、switch）。
 * 若上游已是标量，可短路映射并订阅，避免完整 Observable 链开销。
 */
public final class ObservableScalarXMap {

    /** 工具类，禁止实例化。 */
    private ObservableScalarXMap() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 尝试对可能为 {@link Supplier} 的上游做标量 XMap 订阅。
     * @param <T> 输入标量类型
     * @param <R> 映射后输出类型
     * @param source 上游 ObservableSource
     * @param observer 下游 Observer
     * @param mapper 将标量映射为 ObservableSource 的函数
     * @return 已走标量快路径时为 true；否则调用方应继续常规路径
     */
    @SuppressWarnings("unchecked")
    public static <T, R> boolean tryScalarXMapSubscribe(ObservableSource<T> source,
            Observer<? super R> observer,
            Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
        if (source instanceof Supplier) {
            T t;

            try {
                t = ((Supplier<T>)source).get();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }

            if (t == null) {
                EmptyDisposable.complete(observer);
                return true;
            }

            ObservableSource<? extends R> r;

            try {
                r = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null ObservableSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }

            if (r instanceof Supplier) {
                R u;

                try {
                    u = ((Supplier<R>)r).get();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    EmptyDisposable.error(ex, observer);
                    return true;
                }

                if (u == null) {
                    EmptyDisposable.complete(observer);
                    return true;
                }
                ScalarDisposable<R> sd = new ScalarDisposable<>(observer, u);
                observer.onSubscribe(sd);
                sd.run();
            } else {
                r.subscribe(observer);
            }

            return true;
        }
        return false;
    }

    /**
     * 将标量值映射为 Observable 并发射其元素。
     *
     * @param <T> 标量类型
     * @param <U> 输出元素类型
     * @param value 待映射标量
     * @param mapper 接收标量并返回待订阅 ObservableSource 的函数
     * @return 包装 {@link ScalarXMapObservable} 的新 Observable
     */
    public static <T, U> Observable<U> scalarXMap(T value,
            Function<? super T, ? extends ObservableSource<? extends U>> mapper) {
        return RxJavaPlugins.onAssembly(new ScalarXMapObservable<>(value, mapper));
    }

    /**
     * 将标量映射为 ObservableSource 并订阅；映射结果仍为 Supplier 时走 {@link ScalarDisposable}。
     *
     * @param <T> 标量类型
     * @param <R> 映射后 ObservableSource 元素类型
     */
    static final class ScalarXMapObservable<T, R> extends Observable<R> {

        final T value;

        final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

        ScalarXMapObservable(T value,
                Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
            this.value = value;
            this.mapper = mapper;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void subscribeActual(Observer<? super R> observer) {
            ObservableSource<? extends R> other;
            try {
                other = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null ObservableSource");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                EmptyDisposable.error(e, observer);
                return;
            }
            if (other instanceof Supplier) {
                R u;

                try {
                    u = ((Supplier<R>)other).get();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    EmptyDisposable.error(ex, observer);
                    return;
                }

                if (u == null) {
                    EmptyDisposable.complete(observer);
                    return;
                }
                ScalarDisposable<R> sd = new ScalarDisposable<>(observer, u);
                observer.onSubscribe(sd);
                sd.run();
            } else {
                other.subscribe(observer);
            }
        }
    }

    /**
     * 表示仅发射一次 onNext 后 onComplete 的 Disposable，支持同步融合。
     *
     * @param <T> 标量元素类型
     */
    public static final class ScalarDisposable<T>
    extends AtomicInteger
    implements QueueDisposable<T>, Runnable {

        @Serial
        private static final long serialVersionUID = 3880992722410194083L;

        final Observer<? super T> observer;

        final T value;

        static final int START = 0;
        static final int FUSED = 1;
        static final int ON_NEXT = 2;
        static final int ON_COMPLETE = 3;

        public ScalarDisposable(Observer<? super T> observer, T value) {
            this.observer = observer;
            this.value = value;
        }

        @Override
        public boolean offer(T value) {
            throw new UnsupportedOperationException("Should not be called!");
        }

        @Override
        public boolean offer(T v1, T v2) {
            throw new UnsupportedOperationException("Should not be called!");
        }

        @Nullable
        @Override
        public T poll() {
            if (get() == FUSED) {
                lazySet(ON_COMPLETE);
                return value;
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return get() != FUSED;
        }

        @Override
        public void clear() {
            lazySet(ON_COMPLETE);
        }

        @Override
        public void dispose() {
            set(ON_COMPLETE);
        }

        @Override
        public boolean isDisposed() {
            return get() == ON_COMPLETE;
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & SYNC) != 0) {
                lazySet(FUSED);
                return SYNC;
            }
            return NONE;
        }

        /** 在 START 状态下 CAS 到 ON_NEXT，发射标量后 onComplete。 */
        @Override
        public void run() {
            if (get() == START && compareAndSet(START, ON_NEXT)) {
                observer.onNext(value);
                if (get() == ON_NEXT) {
                    lazySet(ON_COMPLETE);
                    observer.onComplete();
                }
            }
        }
    }
}
