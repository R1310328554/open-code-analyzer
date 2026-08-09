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

package io.reactivex.rxjava4.internal.operators.completable;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Cancellable;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 通过 {@link CompletableOnSubscribe} 回调创建 {@link Completable} 的实现。
 */
public final class CompletableCreate extends Completable {

    final CompletableOnSubscribe source;

    /** @param source 订阅时调用的 CompletableOnSubscribe 回调 */
    public CompletableCreate(CompletableOnSubscribe source) {
        this.source = source;
    }

    /** 创建 Emitter 并调用 source.subscribe；异常时通过 emitter 报告错误。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        Emitter parent = new Emitter(observer);
        observer.onSubscribe(parent);

        try {
            source.subscribe(parent);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            parent.onError(ex);
        }
    }

    /** 向下游传递完成/错误信号并管理 disposable 的 CompletableEmitter 实现。 */
    static final class Emitter
    extends AtomicReference<Disposable>
    implements CompletableEmitter, Disposable {

        @Serial
        private static final long serialVersionUID = -2467358622224974244L;

        final CompletableObserver downstream;

        Emitter(CompletableObserver downstream) {
            this.downstream = downstream;
        }

        /** 通知下游完成并 dispose 关联的 disposable。 */
        @Override
        public void onComplete() {
            if (get() != DisposableHelper.DISPOSED) {
                Disposable d = getAndSet(DisposableHelper.DISPOSED);
                if (d != DisposableHelper.DISPOSED) {
                    try {
                        downstream.onComplete();
                    } finally {
                        if (d != null) {
                            d.dispose();
                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!tryOnError(t)) {
                RxJavaPlugins.onError(t);
            }
        }

        /** 尝试向 downstream 报告错误；若已 dispose 则返回 false。 */
        @Override
        public boolean tryOnError(Throwable t) {
            if (t == null) {
                t = ExceptionHelper.createNullPointerException("onError called with a null Throwable.");
            }
            if (get() != DisposableHelper.DISPOSED) {
                Disposable d = getAndSet(DisposableHelper.DISPOSED);
                if (d != DisposableHelper.DISPOSED) {
                    try {
                        downstream.onError(t);
                    } finally {
                        if (d != null) {
                            d.dispose();
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void setDisposable(Disposable d) {
            DisposableHelper.set(this, d);
        }

        @Override
        public void setCancellable(Cancellable c) {
            setDisposable(new CancellableDisposable(c));
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public String toString() {
            return String.format("%s{%s}", getClass().getSimpleName(), super.toString());
        }
    }
}
