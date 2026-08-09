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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 基于可变状态与 {@link BiFunction} 生成器按需生成元素，支持背压。
 * @param <T> 生成元素类型
 * @param <S> 状态类型
 */
public final class FlowableGenerate<T, S> extends Flowable<T> {
    final Supplier<S> stateSupplier;
    final BiFunction<S, Emitter<T>, S> generator;
    final Consumer<? super S> disposeState;

    /**
     * @param stateSupplier 初始状态 Supplier
     * @param generator 接收状态与 {@link Emitter} 并返回下一状态的生成函数
     * @param disposeState 取消或终止时清理状态的回调
     */
    public FlowableGenerate(Supplier<S> stateSupplier, BiFunction<S, Emitter<T>, S> generator,
            Consumer<? super S> disposeState) {
        this.stateSupplier = stateSupplier;
        this.generator = generator;
        this.disposeState = disposeState;
    }

    /** 获取初始状态并安装 GeneratorSubscription。 */
    @Override
    public void subscribeActual(Subscriber<? super T> s) {
        S state;

        try {
            state = stateSupplier.get();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptySubscription.error(e, s);
            return;
        }

        s.onSubscribe(new GeneratorSubscription<>(s, generator, disposeState, state));
    }

    /** 背压感知的生成器 Subscription，实现 {@link Emitter} 回调接口。 */
    static final class GeneratorSubscription<T, S>
    extends AtomicLong
    implements Emitter<T>, Subscription {

        @Serial
        private static final long serialVersionUID = 7565982551505011832L;

        final Subscriber<? super T> downstream;
        final BiFunction<S, ? super Emitter<T>, S> generator;
        final Consumer<? super S> disposeState;

        S state;

        volatile boolean cancelled;

        boolean terminate;

        boolean hasNext;

        GeneratorSubscription(Subscriber<? super T> actual,
                BiFunction<S, ? super Emitter<T>, S> generator,
                Consumer<? super S> disposeState, S initialState) {
            this.downstream = actual;
            this.generator = generator;
            this.disposeState = disposeState;
            this.state = initialState;
        }

        /** 按请求量循环调用 generator 直至满足背压或终止。 */
        @Override
        public void request(long n) {
            if (!SubscriptionHelper.validate(n)) {
                return;
            }
            if (BackpressureHelper.add(this, n) != 0L) {
                return;
            }

            long e = 0L;

            S s = state;

            final BiFunction<S, ? super Emitter<T>, S> f = generator;

            for (;;) {
                while (e != n) {

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
                        cancelled = true;
                        state = null;
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

                    e++;
                }

                n = get();
                if (e == n) {
                    state = s;
                    n = addAndGet(-e);
                    if (n == 0L) {
                        break;
                    }
                    e = 0L;
                }
            }
        }

        /** 调用 disposeState 清理状态，异常路由至 RxJavaPlugins。 */
        private void dispose(S s) {
            try {
                disposeState.accept(s);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
            }
        }

        /** 标记取消；无活跃 request 时立即 dispose 状态。 */
        @Override
        public void cancel() {
            if (!cancelled) {
                cancelled = true;

                // 若无进行中的 request，直接清理状态
                if (BackpressureHelper.add(this, 1) == 0) {
                    S s = state;
                    state = null;
                    dispose(s);
                }
            }
        }

        /** 每轮 generator 至多一次 onNext；null 或重复调用报错。 */
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

        /** 终止序列并向下游传播错误。 */
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

        /** 正常终止并通知下游 onComplete。 */
        @Override
        public void onComplete() {
            if (!terminate) {
                terminate = true;
                downstream.onComplete();
            }
        }
    }
}
