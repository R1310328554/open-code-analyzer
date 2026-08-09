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

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 按上游信号分支映射：onSuccess 走 onSuccessMapper，
 * onError 走 onErrorMapper，再订阅返回的 SingleSource 并转发。
 *
 * @param <T> 上游值类型
 * @param <R> 下游结果类型
 * @since 3.0.0
 */
public final class SingleFlatMapNotification<T, R> extends Single<R> {

    final SingleSource<T> source;

    final Function<? super T, ? extends SingleSource<? extends R>> onSuccessMapper;

    final Function<? super Throwable, ? extends SingleSource<? extends R>> onErrorMapper;

    /**
     * @param source 上游 SingleSource
     * @param onSuccessMapper 成功值映射为 SingleSource 的函数
     * @param onErrorMapper 错误映射为 SingleSource 的函数
     */
    public SingleFlatMapNotification(SingleSource<T> source,
            Function<? super T, ? extends SingleSource<? extends R>> onSuccessMapper,
            Function<? super Throwable, ? extends SingleSource<? extends R>> onErrorMapper) {
        this.source = source;
        this.onSuccessMapper = onSuccessMapper;
        this.onErrorMapper = onErrorMapper;
    }

    /** 订阅 FlatMapSingleObserver 按成功/错误路径选择 mapper。 */
    @Override
    protected void subscribeActual(SingleObserver<? super R> observer) {
        source.subscribe(new FlatMapSingleObserver<>(observer, onSuccessMapper, onErrorMapper));
    }

    /** 分支 flatMap：onSuccess/onError 分别应用对应 mapper 并订阅 inner。 */
    static final class FlatMapSingleObserver<T, R>
    extends AtomicReference<Disposable>
    implements SingleObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 4375739915521278546L;

        final SingleObserver<? super R> downstream;

        final Function<? super T, ? extends SingleSource<? extends R>> onSuccessMapper;

        final Function<? super Throwable, ? extends SingleSource<? extends R>> onErrorMapper;

        Disposable upstream;

        FlatMapSingleObserver(SingleObserver<? super R> actual,
                Function<? super T, ? extends SingleSource<? extends R>> onSuccessMapper,
                Function<? super Throwable, ? extends SingleSource<? extends R>> onErrorMapper) {
            this.downstream = actual;
            this.onSuccessMapper = onSuccessMapper;
            this.onErrorMapper = onErrorMapper;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** onSuccessMapper 获取 SingleSource 并订阅 InnerObserver。 */
        @Override
        public void onSuccess(T value) {
            SingleSource<? extends R> source;

            try {
                source = Objects.requireNonNull(onSuccessMapper.apply(value), "The onSuccessMapper returned a null SingleSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            if (!isDisposed()) {
                source.subscribe(new InnerObserver());
            }
        }

        /** onErrorMapper 获取 SingleSource；mapper 异常合并为 CompositeException。 */
        @Override
        public void onError(Throwable e) {
            SingleSource<? extends R> source;

            try {
                source = Objects.requireNonNull(onErrorMapper.apply(e), "The onErrorMapper returned a null SingleSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(e, ex));
                return;
            }

            if (!isDisposed()) {
                source.subscribe(new InnerObserver());
            }
        }

        /** inner SingleSource 的 Observer：DisposableHelper.setOnce 管理订阅。 */
        final class InnerObserver implements SingleObserver<R> {

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(FlatMapSingleObserver.this, d);
            }

            @Override
            public void onSuccess(R value) {
                downstream.onSuccess(value);
            }

            @Override
            public void onError(Throwable e) {
                downstream.onError(e);
            }
        }
    }
}
