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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 在上游 {@code onSubscribe} 与下游 {@code dispose} 时调用回调。
 *
 * @param <T> 元素类型
 * @since 3.0.0
 */
public final class MaybeDoOnLifecycle<T> extends AbstractMaybeWithUpstream<T, T> {

    final Consumer<? super Disposable> onSubscribe;

    final Action onDispose;

    /**
     * @param upstream 上游 Maybe
     * @param onSubscribe 收到上游 Disposable 时调用
     * @param onDispose 下游 dispose 时调用
     */
    public MaybeDoOnLifecycle(Maybe<T> upstream, Consumer<? super Disposable> onSubscribe,
            Action onDispose) {
        super(upstream);
        this.onSubscribe = onSubscribe;
        this.onDispose = onDispose;
    }

    /** 包装为 MaybeLifecycleObserver。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new MaybeLifecycleObserver<>(observer, onSubscribe, onDispose));
    }

    /** onSubscribe 回调先于 validate；dispose 时先 onDispose 再 cancel 上游。 */
    static final class MaybeLifecycleObserver<T> implements MaybeObserver<T>, Disposable {

        final MaybeObserver<? super T> downstream;

        final Consumer<? super Disposable> onSubscribe;

        final Action onDispose;

        Disposable upstream;

        MaybeLifecycleObserver(MaybeObserver<? super T> downstream, Consumer<? super Disposable> onSubscribe, Action onDispose) {
            this.downstream = downstream;
            this.onSubscribe = onSubscribe;
            this.onDispose = onDispose;
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            // 允许重复 onSubscribe，便于 doOnSubscribe 相关测试验证行为
            try {
                onSubscribe.accept(d);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                d.dispose();
                this.upstream = DisposableHelper.DISPOSED;
                EmptyDisposable.error(e, downstream);
                return;
            }
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(@NonNull T t) {
            if (upstream != DisposableHelper.DISPOSED) {
                upstream = DisposableHelper.DISPOSED;
                downstream.onSuccess(t);
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            if (upstream != DisposableHelper.DISPOSED) {
                upstream = DisposableHelper.DISPOSED;
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        @Override
        public void onComplete() {
            if (upstream != DisposableHelper.DISPOSED) {
                upstream = DisposableHelper.DISPOSED;
                downstream.onComplete();
            }
        }

        @Override
        public void dispose() {
            try {
                onDispose.run();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                RxJavaPlugins.onError(e);
            }
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }
}
