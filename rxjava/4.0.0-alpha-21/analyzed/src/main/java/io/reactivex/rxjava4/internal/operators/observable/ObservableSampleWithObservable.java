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
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.observers.SerializedObserver;

/**
 * 以 other Observable 的 onNext 为采样节拍，
 * 每次节拍发射上游最新缓存值；emitLast 控制主流完成时是否发射末值。
 *
 * @param <T> 元素类型
 */

    final ObservableSource<?> other;

    final boolean emitLast;

    /**
     * @param source 上游 ObservableSource
     * @param other 采样节拍源
     * @param emitLast 主流完成时是否发射最后一次缓存值
     */
    public ObservableSampleWithObservable(ObservableSource<T> source, ObservableSource<?> other, boolean emitLast) {
        super(source);
        this.other = other;
        this.emitLast = emitLast;
    }

    /** 按 emitLast 选择 SampleMainEmitLast 或 SampleMainNoLast。 */
    @Override
    public void subscribeActual(Observer<? super T> t) {
        SerializedObserver<T> serial = new SerializedObserver<>(t);
        if (emitLast) {
            source.subscribe(new SampleMainEmitLast<>(serial, other));
        } else {
            source.subscribe(new SampleMainNoLast<>(serial, other));
        }
    }

    /** 缓存上游最新值；sampler onNext 时 run/emit。 */
    abstract static class SampleMainObserver<T> extends AtomicReference<T>
    implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -3517602651313910099L;

        final Observer<? super T> downstream;
        final ObservableSource<?> sampler;

        final AtomicReference<Disposable> other = new AtomicReference<>();

        Disposable upstream;

        SampleMainObserver(Observer<? super T> actual, ObservableSource<?> other) {
            this.downstream = actual;
            this.sampler = other;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
                if (other.get() == null) {
                    sampler.subscribe(new SamplerObserver<>(this));
                }
            }
        }

        /** lazySet 缓存最新值，等待采样节拍 emit。 */
        @Override
        public void onNext(T t) {
            lazySet(t);
        }

        @Override
        public void onError(Throwable t) {
            DisposableHelper.dispose(other);
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            DisposableHelper.dispose(other);
            completion();
        }

        boolean setOther(Disposable o) {
            return DisposableHelper.setOnce(other, o);
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(other);
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return other.get() == DisposableHelper.DISPOSED;
        }

        public void error(Throwable e) {
            upstream.dispose();
            downstream.onError(e);
        }

        public void complete() {
            upstream.dispose();
            completion();
        }

        /** getAndSet(null) 取出缓存值并 onNext。 */
        void emit() {
            T value = getAndSet(null);
            if (value != null) {
                downstream.onNext(value);
            }
        }

        abstract void completion();

        abstract void run();
    }

    /** 采样源 Observer：onNext 触发 parent.run() 发射缓存值。 */
    record SamplerObserver<T>(SampleMainObserver<T> parent) implements Observer<Object> {

        @Override
            public void onSubscribe(Disposable d) {
                parent.setOther(d);
            }

            @Override
            public void onNext(Object t) {
                parent.run();
            }

            @Override
            public void onError(Throwable t) {
                parent.error(t);
            }

            @Override
            public void onComplete() {
                parent.complete();
            }
        }

    /** emitLast=false：主流完成即 onComplete，不强制 emit 末值。 */
    static final class SampleMainNoLast<T> extends SampleMainObserver<T> {

        @Serial
        private static final long serialVersionUID = -3029755663834015785L;

        SampleMainNoLast(Observer<? super T> actual, ObservableSource<?> other) {
            super(actual, other);
        }

        @Override
        void completion() {
            downstream.onComplete();
        }

        @Override
        void run() {
            emit();
        }
    }

    /** emitLast=true：主流完成时 done=true，run 循环中 emit 末值后 onComplete。 */
    static final class SampleMainEmitLast<T> extends SampleMainObserver<T> {

        @Serial
        private static final long serialVersionUID = -3029755663834015785L;

        final AtomicInteger wip;

        volatile boolean done;

        SampleMainEmitLast(Observer<? super T> actual, ObservableSource<?> other) {
            super(actual, other);
            this.wip = new AtomicInteger();
        }

        @Override
        void completion() {
            done = true;
            if (wip.getAndIncrement() == 0) {
                emit();
                downstream.onComplete();
            }
        }

        @Override
        void run() {
            if (wip.getAndIncrement() == 0) {
                do {
                    boolean d = done;
                    emit();
                    if (d) {
                        downstream.onComplete();
                        return;
                    }
                } while (wip.decrementAndGet() != 0);
            }
        }
    }
}
