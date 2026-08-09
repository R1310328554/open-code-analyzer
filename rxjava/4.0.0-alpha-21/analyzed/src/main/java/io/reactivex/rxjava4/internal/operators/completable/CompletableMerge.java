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
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;

/**
 * 合并 {@link Publisher} 发出的多个 {@link CompletableSource}，
 * 受 maxConcurrency 限制并行订阅数量。
 */
public final class CompletableMerge extends Completable {
    final Publisher<? extends CompletableSource> source;
    final int maxConcurrency;
    final boolean delayErrors;

    /**
     * @param source 发出 CompletableSource 的 Publisher
     * @param maxConcurrency 最大并行内部源数量
     * @param delayErrors 为 true 时收集所有错误后再终止
     */
    public CompletableMerge(Publisher<? extends CompletableSource> source, int maxConcurrency, boolean delayErrors) {
        this.source = source;
        this.maxConcurrency = maxConcurrency;
        this.delayErrors = delayErrors;
    }

    /** 订阅 source Publisher 并合并各 CompletableSource。 */
    @Override
    public void subscribeActual(CompletableObserver observer) {
        CompletableMergeSubscriber parent = new CompletableMergeSubscriber(observer, maxConcurrency, delayErrors);
        source.subscribe(parent);
    }

    /** 从 Publisher 取源并管理并行内部订阅的内部 subscriber。 */
    static final class CompletableMergeSubscriber
    extends AtomicInteger
    implements FlowableSubscriber<CompletableSource>, Disposable {

        @Serial
        private static final long serialVersionUID = -2108443387387077490L;

        final CompletableObserver downstream;
        final int maxConcurrency;
        final boolean delayErrors;

        final AtomicThrowable errors;

        final CompositeDisposable set;

        Subscription upstream;

        CompletableMergeSubscriber(CompletableObserver actual, int maxConcurrency, boolean delayErrors) {
            this.downstream = actual;
            this.maxConcurrency = maxConcurrency;
            this.delayErrors = delayErrors;
            this.set = new CompositeDisposable();
            this.errors = new AtomicThrowable();
            lazySet(1);
        }

        @Override
        public void dispose() {
            upstream.cancel();
            set.dispose();
            errors.tryTerminateAndReport();
        }

        @Override
        public boolean isDisposed() {
            return set.isDisposed();
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                if (maxConcurrency == Integer.MAX_VALUE) {
                    s.request(Long.MAX_VALUE);
                } else {
                    s.request(maxConcurrency);
                }
            }
        }

        /** 订阅新到达的 CompletableSource 并加入合并集合。 */
        @Override
        public void onNext(CompletableSource t) {
            getAndIncrement();

            MergeInnerObserver inner = new MergeInnerObserver();
            set.add(inner);
            t.subscribe(inner);
        }

        @Override
        public void onError(Throwable t) {
            if (!delayErrors) {
                set.dispose();

                if (errors.tryAddThrowableOrReport(t)) {
                    if (getAndSet(0) > 0) {
                        errors.tryTerminateConsumer(downstream);
                    }
                }
            } else {
                if (errors.tryAddThrowableOrReport(t)) {
                    if (decrementAndGet() == 0) {
                        errors.tryTerminateConsumer(downstream);
                    }
                }
            }
        }

        @Override
        public void onComplete() {
            if (decrementAndGet() == 0) {
                errors.tryTerminateConsumer(downstream);
            }
        }

        /** 处理内部源错误；按 delayErrors 策略终止或继续。 */
        void innerError(MergeInnerObserver inner, Throwable t) {
            set.delete(inner);
            if (!delayErrors) {
                upstream.cancel();
                set.dispose();

                if (errors.tryAddThrowableOrReport(t)) {
                    if (getAndSet(0) > 0) {
                        errors.tryTerminateConsumer(downstream);
                    }
                }
            } else {
                if (errors.tryAddThrowableOrReport(t)) {
                    if (decrementAndGet() == 0) {
                        errors.tryTerminateConsumer(downstream);
                    } else {
                        if (maxConcurrency != Integer.MAX_VALUE) {
                            upstream.request(1);
                        }
                    }
                }
            }
        }

        /** 内部源完成；全部完成后通知下游。 */
        void innerComplete(MergeInnerObserver inner) {
            set.delete(inner);
            if (decrementAndGet() == 0) {
                errors.tryTerminateConsumer(downstream);
            } else {
                if (maxConcurrency != Integer.MAX_VALUE) {
                    upstream.request(1);
                }
            }
        }

        /** 表示单个被合并 CompletableSource 的内部 observer。 */
        final class MergeInnerObserver
        extends AtomicReference<Disposable>
        implements CompletableObserver, Disposable {
            @Serial
            private static final long serialVersionUID = 251330541679988317L;

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onError(Throwable e) {
                innerError(this, e);
            }

            @Override
            public void onComplete() {
                innerComplete(this);
            }

            @Override
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }

            @Override
            public void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
