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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 等待多个 {@link MaybeSource} 均 onSuccess 后，
 * 用 zipper 将各值组合为单个结果；任一 onError/onComplete 则终止。
 *
 * @param <T> 各源元素类型
 * @param <R> 组合结果类型
 */
public final class MaybeZipArray<T, R> extends Maybe<R> {

    final MaybeSource<? extends T>[] sources;

    final Function<? super Object[], ? extends R> zipper;

    /**
     * @param sources 待 zip 的 MaybeSource 数组
     * @param zipper 将 Object[] 映射为结果的函数
     */
    public MaybeZipArray(MaybeSource<? extends T>[] sources, Function<? super Object[], ? extends R> zipper) {
        this.sources = sources;
        this.zipper = zipper;
    }

    /** 单源时退化为 map；多源时用 ZipCoordinator 等待全部 onSuccess。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super R> observer) {
        MaybeSource<? extends T>[] sources = this.sources;
        int n = sources.length;

        if (n == 1) {
            sources[0].subscribe(new MaybeMap.MapMaybeObserver<>(observer, new SingletonArrayFunc()));
            return;
        }

        ZipCoordinator<T, R> parent = new ZipCoordinator<>(observer, n, zipper);

        observer.onSubscribe(parent);

        for (int i = 0; i < n; i++) {
            if (parent.isDisposed()) {
                return;
            }

            MaybeSource<? extends T> source = sources[i];

            if (source == null) {
                parent.innerError(new NullPointerException("One of the sources is null"), i);
                return;
            }
            source.subscribe(parent.observers[i]);
        }
    }

    /** 协调多个 ZipMaybeObserver，全部 onSuccess 后应用 zipper。 */
    static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {

        @Serial
        private static final long serialVersionUID = -5556924161382950569L;

        final MaybeObserver<? super R> downstream;

        final Function<? super Object[], ? extends R> zipper;

        final ZipMaybeObserver<T>[] observers;

        Object[] values;

        @SuppressWarnings("unchecked")
        ZipCoordinator(MaybeObserver<? super R> observer, int n, Function<? super Object[], ? extends R> zipper) {
            super(n);
            this.downstream = observer;
            this.zipper = zipper;
            ZipMaybeObserver<T>[] o = new ZipMaybeObserver[n];
            for (int i = 0; i < n; i++) {
                o[i] = new ZipMaybeObserver<>(this, i);
            }
            this.observers = o;
            this.values = new Object[n];
        }

        @Override
        public boolean isDisposed() {
            return get() <= 0;
        }

        @Override
        public void dispose() {
            if (getAndSet(0) > 0) {
                for (ZipMaybeObserver<?> d : observers) {
                    d.dispose();
                }

                values = null;
            }
        }

        /** 记录第 index 个值；全部到齐后应用 zipper 并 onSuccess。 */
        void innerSuccess(T value, int index) {
            Object[] values = this.values;
            if (values != null) {
                values[index] = value;
            }
            if (decrementAndGet() == 0) {
                R v;

                try {
                    v = Objects.requireNonNull(zipper.apply(values), "The zipper returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    this.values = null;
                    downstream.onError(ex);
                    return;
                }

                this.values = null;
                downstream.onSuccess(v);
            }
        }

        void disposeExcept(int index) {
            ZipMaybeObserver<T>[] observers = this.observers;
            int n = observers.length;
            for (int i = 0; i < index; i++) {
                observers[i].dispose();
            }
            for (int i = index + 1; i < n; i++) {
                observers[i].dispose();
            }
        }

        void innerError(Throwable ex, int index) {
            if (getAndSet(0) > 0) {
                disposeExcept(index);
                values = null;
                downstream.onError(ex);
            } else {
                RxJavaPlugins.onError(ex);
            }
        }

        /** 任一源 onComplete 则取消其余并向下游 onComplete。 */
        void innerComplete(int index) {
            if (getAndSet(0) > 0) {
                disposeExcept(index);
                values = null;
                downstream.onComplete();
            }
        }
    }

    /** 单个源的 observer，将信号转发给 ZipCoordinator。 */
    static final class ZipMaybeObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T> {

        @Serial
        private static final long serialVersionUID = 3323743579927613702L;

        final ZipCoordinator<T, ?> parent;

        final int index;

        ZipMaybeObserver(ZipCoordinator<T, ?> parent, int index) {
            this.parent = parent;
            this.index = index;
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onSuccess(T value) {
            parent.innerSuccess(value, index);
        }

        @Override
        public void onError(Throwable e) {
            parent.innerError(e, index);
        }

        @Override
        public void onComplete() {
            parent.innerComplete(index);
        }
    }

    final class SingletonArrayFunc implements Function<T, R> {
        @Override
        public R apply(T t) throws Throwable {
            return Objects.requireNonNull(zipper.apply(new Object[] { t }), "The zipper returned a null value");
        }
    }
}
