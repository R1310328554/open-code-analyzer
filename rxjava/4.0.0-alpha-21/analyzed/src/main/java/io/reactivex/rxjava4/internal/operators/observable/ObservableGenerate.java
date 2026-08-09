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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 有状态生成器：初始状态由 stateSupplier 提供，
 * generator 在每轮通过 {@link Emitter} 推送元素并返回下一状态；
 * disposeState 在终止时清理状态。
 *
 * @param <T> 生成元素类型
 * @param <S> 状态类型
 */
public final class ObservableGenerate<T, S> extends Observable<T> {
    final Supplier<S> stateSupplier;
    final BiFunction<S, Emitter<T>, S> generator;
    final Consumer<? super S> disposeState;

    /**
     * @param stateSupplier 提供初始状态的 Supplier
     * @param generator 每轮 (state, emitter) -> 下一 state 的 BiFunction
     * @param disposeState 终止时清理状态的 Consumer
     */
    public ObservableGenerate(Supplier<S> stateSupplier, BiFunction<S, Emitter<T>, S> generator,
            Consumer<? super S> disposeState) {
        this.stateSupplier = stateSupplier;
        this.generator = generator;
        this.disposeState = disposeState;
    }

    /** 创建 GeneratorDisposable 并启动 run 循环。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        S state;

        try {
            state = stateSupplier.get();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, observer);
            return;
        }

        GeneratorDisposable<T, S> gd = new GeneratorDisposable<>(observer, generator, disposeState, state);
        observer.onSubscribe(gd);
        gd.run();
    }

    /** 实现 Emitter 与 Disposable，驱动 generator 状态机循环。 */
    static final class GeneratorDisposable<T, S>
    implements Emitter<T>, Disposable {

        final Observer<? super T> downstream;
        final BiFunction<S, ? super Emitter<T>, S> generator;
        final Consumer<? super S> disposeState;

        S state;

        volatile boolean cancelled;

        boolean terminate;

        boolean hasNext;

        GeneratorDisposable(Observer<? super T> actual,
                BiFunction<S, ? super Emitter<T>, S> generator,
                Consumer<? super S> disposeState, S initialState) {
            this.downstream = actual;
            this.generator = generator;
            this.disposeState = disposeState;
            this.state = initialState;
        }

        /** 循环调用 generator；terminate 或 cancel 时 dispose 状态并退出。 */
        public void run() {
            S s = state;

            if (cancelled) {
                state = null;
                dispose(s);
                return;
            }

            final BiFunction<S, ? super Emitter<T>, S> f = generator;

            for (;;) {

                if (cancelled) {
                    state = null;
                    dispose(s);
                    return;
                }

                hasNext = false;

                try {
                    s = f.apply(s, this);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    state = null;
                    cancelled = true;
                    onError(ex);
                    dispose(s);
                    return;
                }

                if (terminate) {
                    cancelled = true;
                    state = null;
                    dispose(s);
                    return;
                }
            }

        }

        /** 调用 disposeState.accept 清理状态（异常经 RxJavaPlugins）。 */
        private void dispose(S s) {
            try {
                disposeState.accept(s);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
            }
        }

        @Override
        public void dispose() {
            cancelled = true;
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        /** 每轮至多一次 onNext；null 或重复调用转 onError。 */
        @Override
        public void onNext(T t) {
            if (!terminate) {
                if (hasNext) {
                    onError(new IllegalStateException("onNext already called in this generate turn"));
                } else {
                    if (t == null) {
                        onError(ExceptionHelper.createNullPointerException("onNext called with a null value."));
                    } else {
                        hasNext = true;
                        downstream.onNext(t);
                    }
                }
            }
        }

        /** 终止序列并向下游转发 onError（已 terminate 则 RxJavaPlugins）。 */
        @Override
        public void onError(Throwable t) {
            if (terminate) {
                RxJavaPlugins.onError(t);
            } else {
                if (t == null) {
                    t = ExceptionHelper.createNullPointerException("onError called with a null Throwable.");
                }
                terminate = true;
                downstream.onError(t);
            }
        }

        /** 标记 terminate 并向下游 onComplete。 */
        @Override
        public void onComplete() {
            if (!terminate) {
                terminate = true;
                downstream.onComplete();
            }
        }
    }
}
