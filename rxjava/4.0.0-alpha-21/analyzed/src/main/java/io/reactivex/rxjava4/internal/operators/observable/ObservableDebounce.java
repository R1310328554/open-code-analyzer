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

import java.util.Objects;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.observers.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 防抖：每个上游 onNext 映射为辅助 ObservableSource，
 * 仅当该内部源发出信号（或上游完成时最后一个内部源终止）才转发该值。
 *
 * @param <T> 上游元素类型
 * @param <U> 防抖选择器返回的内部源元素类型
 */
public final class ObservableDebounce<T, U> extends AbstractObservableWithUpstream<T, T> {
    final Function<? super T, ? extends ObservableSource<U>> debounceSelector;

    /**
     * @param source 上游 ObservableSource
     * @param debounceSelector 将元素映射为防抖用内部 ObservableSource 的函数
     */
    public ObservableDebounce(ObservableSource<T> source, Function<? super T, ? extends ObservableSource<U>> debounceSelector) {
        super(source);
        this.debounceSelector = debounceSelector;
    }

    /** 经 SerializedObserver 订阅 DebounceObserver。 */
    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new DebounceObserver<>(new SerializedObserver<>(t), debounceSelector));
    }

    /** 管理 index 与 debouncer，协调内部防抖订阅。 */
    static final class DebounceObserver<T, U>
    implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        final Function<? super T, ? extends ObservableSource<U>> debounceSelector;

        Disposable upstream;

        final AtomicReference<Disposable> debouncer = new AtomicReference<>();

        volatile long index;

        boolean done;

        DebounceObserver(Observer<? super T> actual,
                Function<? super T, ? extends ObservableSource<U>> debounceSelector) {
            this.downstream = actual;
            this.debounceSelector = debounceSelector;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        /** 递增 index、dispose 旧 debouncer、订阅新 DebounceInnerObserver。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            long idx = index + 1;
            index = idx;

            Disposable d = debouncer.get();
            if (d != null) {
                d.dispose();
            }

            ObservableSource<U> p;

            try {
                p = Objects.requireNonNull(debounceSelector.apply(t), "The ObservableSource supplied is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                dispose();
                downstream.onError(e);
                return;
            }

            DebounceInnerObserver<T, U> dis = new DebounceInnerObserver<>(this, idx, t);

            if (debouncer.compareAndSet(d, dis)) {
                p.subscribe(dis);
            }
        }

        @Override
        public void onError(Throwable t) {
            DisposableHelper.dispose(debouncer);
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            Disposable d = debouncer.get();
            if (d != DisposableHelper.DISPOSED) {
                @SuppressWarnings("unchecked")
                DebounceInnerObserver<T, U> dis = (DebounceInnerObserver<T, U>)d;
                if (dis != null) {
                    dis.emit();
                }
                DisposableHelper.dispose(debouncer);
                downstream.onComplete();
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
            DisposableHelper.dispose(debouncer);
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** index 仍匹配时向下游转发缓存值。 */
        void emit(long idx, T value) {
            if (idx == index) {
                downstream.onNext(value);
            }
        }

    /** 订阅 debounceSelector 返回的内部源，终止时 emit 对应上游值。 */
        static final class DebounceInnerObserver<T, U> extends DisposableObserver<U> {
            final DebounceObserver<T, U> parent;
            final long index;
            final T value;

            boolean done;

            final AtomicBoolean once = new AtomicBoolean();

            DebounceInnerObserver(DebounceObserver<T, U> parent, long index, T value) {
                this.parent = parent;
                this.index = index;
                this.value = value;
            }

            @Override
            public void onNext(U t) {
                if (done) {
                    return;
                }
                done = true;
                dispose();
                emit();
            }

            /** CAS 保证至多一次 emit 至 parent。 */
            void emit() {
                if (once.compareAndSet(false, true)) {
                    parent.emit(index, value);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (done) {
                    RxJavaPlugins.onError(t);
                    return;
                }
                done = true;
                parent.onError(t);
            }

            @Override
            public void onComplete() {
                if (done) {
                    return;
                }
                done = true;
                emit();
            }
        }
    }
}
