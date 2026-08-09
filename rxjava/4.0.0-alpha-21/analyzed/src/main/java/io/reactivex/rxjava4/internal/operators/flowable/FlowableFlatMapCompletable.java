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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;

/**
 * 将上游每个元素映射为 {@link CompletableSource} 并等待其全部终止；
 * 不向 downstream 发射元素。
 * @param <T> 上游元素类型
 */
public final class FlowableFlatMapCompletable<T> extends AbstractFlowableWithUpstream<T, T> {

    final Function<? super T, ? extends CompletableSource> mapper;

    final int maxConcurrency;

    final boolean delayErrors;

    /**
     * @param source 上游 Flowable
     * @param mapper 将元素映射为 CompletableSource 的函数
     * @param delayErrors 为 true 时收集所有错误后再终止
     * @param maxConcurrency 最大并行 inner 订阅数
     */
    public FlowableFlatMapCompletable(Flowable<T> source,
            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors,
            int maxConcurrency) {
        super(source);
        this.mapper = mapper;
        this.delayErrors = delayErrors;
        this.maxConcurrency = maxConcurrency;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        source.subscribe(new FlatMapCompletableMainSubscriber<>(subscriber, mapper, delayErrors, maxConcurrency));
    }

    /** 并行订阅 inner Completable 并在全部完成后终止 downstream。 */
    static final class FlatMapCompletableMainSubscriber<T> extends BasicIntQueueSubscription<T>
    implements FlowableSubscriber<T> {
        @Serial
        private static final long serialVersionUID = 8443155186132538303L;

        final Subscriber<? super T> downstream;

        final AtomicThrowable errors;

        final Function<? super T, ? extends CompletableSource> mapper;

        final boolean delayErrors;

        final CompositeDisposable set;

        final int maxConcurrency;

        Subscription upstream;

        volatile boolean cancelled;

        FlatMapCompletableMainSubscriber(Subscriber<? super T> subscriber,
                Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors,
                int maxConcurrency) {
            this.downstream = subscriber;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
            this.errors = new AtomicThrowable();
            this.set = new CompositeDisposable();
            this.maxConcurrency = maxConcurrency;
            this.lazySet(1);
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                int m = maxConcurrency;
                if (m == Integer.MAX_VALUE) {
                    s.request(Long.MAX_VALUE);
                } else {
                    s.request(m);
                }
            }
        }

        @Override
        public void onNext(T value) {
            CompletableSource cs;

            try {
                cs = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null CompletableSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.cancel();
                onError(ex);
                return;
            }

            getAndIncrement();

            InnerConsumer inner = new InnerConsumer();

            if (!cancelled && set.add(inner)) {
                cs.subscribe(inner);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (errors.tryAddThrowableOrReport(e)) {
                if (delayErrors) {
                    if (decrementAndGet() == 0) {
                        errors.tryTerminateConsumer(downstream);
                    } else {
                        if (maxConcurrency != Integer.MAX_VALUE) {
                            upstream.request(1);
                        }
                    }
                } else {
                    cancelled = true;
                    upstream.cancel();
                    set.dispose();
                    errors.tryTerminateConsumer(downstream);
                }
            }
        }

        @Override
        public void onComplete() {
            if (decrementAndGet() == 0) {
                errors.tryTerminateConsumer(downstream);
            } else {
                if (maxConcurrency != Integer.MAX_VALUE) {
                    upstream.request(1);
                }
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            upstream.cancel();
            set.dispose();
            errors.tryTerminateAndReport();
        }

        @Override
        public void request(long n) {
            // 忽略：不向 downstream 发射元素
        }

        @Nullable
        @Override
        public T poll() {
            return null; // 始终为空
        }

        @Override
        public boolean isEmpty() {
            return true; // 始终为空
        }

        @Override
        public void clear() {
            // 无内容可清空
        }

        @Override
        public int requestFusion(int mode) {
            return mode & ASYNC;
        }

        /** inner 完成时从 set 移除并递减活跃计数。 */
        void innerComplete(InnerConsumer inner) {
            set.delete(inner);
            onComplete();
        }

        /** inner 出错时从 set 移除并转发错误。 */
        void innerError(InnerConsumer inner, Throwable e) {
            set.delete(inner);
            onError(e);
        }

        /** 订阅单个 inner Completable 并报告完成/错误。 */
        final class InnerConsumer extends AtomicReference<Disposable> implements CompletableObserver, Disposable {
            @Serial
            private static final long serialVersionUID = 8606673141535671828L;

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onComplete() {
                innerComplete(this);
            }

            @Override
            public void onError(Throwable e) {
                innerError(this, e);
            }

            @Override
            public void dispose() {
                DisposableHelper.dispose(this);
            }

            @Override
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }
        }
    }
}
