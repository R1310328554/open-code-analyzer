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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

import java.util.Objects;

/**
 * 将上游 onNext/onError/onComplete 分别映射为新的 {@link ObservableSource} 并作为
 * 下游 onNext 发射（onError 映射后还会 onComplete）。
 *
 * @param <T> 上游元素类型
 * @param <R> 映射后 ObservableSource 的元素类型
 */
public final class ObservableMapNotification<T, R> extends AbstractObservableWithUpstream<T, ObservableSource<? extends R>> {

    final Function<? super T, ? extends ObservableSource<? extends R>> onNextMapper;
    final Function<? super Throwable, ? extends ObservableSource<? extends R>> onErrorMapper;
    final Supplier<? extends ObservableSource<? extends R>> onCompleteSupplier;

    /**
     * @param source 上游 ObservableSource
     * @param onNextMapper onNext 时的 ObservableSource 映射
     * @param onErrorMapper onError 时的 ObservableSource 映射
     * @param onCompleteSupplier onComplete 时提供 ObservableSource 的 Supplier
     */
    public ObservableMapNotification(
            ObservableSource<T> source,
            Function<? super T, ? extends ObservableSource<? extends R>> onNextMapper,
            Function<? super Throwable, ? extends ObservableSource<? extends R>> onErrorMapper,
            Supplier<? extends ObservableSource<? extends R>> onCompleteSupplier) {
        super(source);
        this.onNextMapper = onNextMapper;
        this.onErrorMapper = onErrorMapper;
        this.onCompleteSupplier = onCompleteSupplier;
    }

    @Override
    public void subscribeActual(Observer<? super ObservableSource<? extends R>> t) {
        source.subscribe(new MapNotificationObserver<>(t, onNextMapper, onErrorMapper, onCompleteSupplier));
    }

    /** 按事件类型调用对应 mapper 并将结果 ObservableSource 作为 onNext 转发。 */
    static final class MapNotificationObserver<T, R>
    implements Observer<T>, Disposable {
        final Observer<? super ObservableSource<? extends R>> downstream;
        final Function<? super T, ? extends ObservableSource<? extends R>> onNextMapper;
        final Function<? super Throwable, ? extends ObservableSource<? extends R>> onErrorMapper;
        final Supplier<? extends ObservableSource<? extends R>> onCompleteSupplier;

        Disposable upstream;

        MapNotificationObserver(Observer<? super ObservableSource<? extends R>> actual,
                Function<? super T, ? extends ObservableSource<? extends R>> onNextMapper,
                Function<? super Throwable, ? extends ObservableSource<? extends R>> onErrorMapper,
                Supplier<? extends ObservableSource<? extends R>> onCompleteSupplier) {
            this.downstream = actual;
            this.onNextMapper = onNextMapper;
            this.onErrorMapper = onErrorMapper;
            this.onCompleteSupplier = onCompleteSupplier;
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

        /** onNextMapper 求值后以 ObservableSource 形式 onNext。 */
        @Override
        public void onNext(T t) {
            ObservableSource<? extends R> p;

            try {
                p = Objects.requireNonNull(onNextMapper.apply(t), "The onNext ObservableSource returned is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                downstream.onError(e);
                return;
            }

            downstream.onNext(p);
        }

        /** onErrorMapper 求值后 onNext 映射源并 onComplete；mapper 异常则 CompositeException。 */
        @Override
        public void onError(Throwable t) {
            ObservableSource<? extends R> p;

            try {
                p = Objects.requireNonNull(onErrorMapper.apply(t), "The onError ObservableSource returned is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                downstream.onError(new CompositeException(t, e));
                return;
            }

            downstream.onNext(p);
            downstream.onComplete();
        }

        /** onCompleteSupplier 求值后 onNext 映射源并 onComplete。 */
        @Override
        public void onComplete() {
            ObservableSource<? extends R> p;

            try {
                p = Objects.requireNonNull(onCompleteSupplier.get(), "The onComplete ObservableSource returned is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                downstream.onError(e);
                return;
            }

            downstream.onNext(p);
            downstream.onComplete();
        }
    }
}
