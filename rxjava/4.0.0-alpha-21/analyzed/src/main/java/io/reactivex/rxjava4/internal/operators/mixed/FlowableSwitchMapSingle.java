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

package io.reactivex.rxjava4.internal.operators.mixed;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将上游元素映射为 {@link SingleSource} 并 switch 到最新 inner，
 * dispose 旧 inner 后发射最新 onSuccess 值；可选延迟主流或 inner 错误。
 * <p>History: 2.1.11 - experimental
 * @param <T> 上游元素类型
 * @param <R> 下游元素类型
 * @since 2.2
 */
public final class FlowableSwitchMapSingle<T, R> extends Flowable<R> {

    final Flowable<T> source;

    final Function<? super T, ? extends SingleSource<? extends R>> mapper;

    final boolean delayErrors;

    /**
     * @param source 上游 Flowable
     * @param mapper 由 T 映射 SingleSource 的函数
     * @param delayErrors 是否延迟合并错误
     */
    public FlowableSwitchMapSingle(Flowable<T> source,
            Function<? super T, ? extends SingleSource<? extends R>> mapper,
            boolean delayErrors) {
        this.source = source;
        this.mapper = mapper;
        this.delayErrors = delayErrors;
    }

    /** 订阅 SwitchMapSingleSubscriber，背压下发射 inner onSuccess。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new SwitchMapSingleSubscriber<>(s, mapper, delayErrors));
    }

    /** 切换 inner Single、背压 drain 发射 item。 */
    static final class SwitchMapSingleSubscriber<T, R> extends AtomicInteger
    implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -5402190102429853762L;

        final Subscriber<? super R> downstream;

        final Function<? super T, ? extends SingleSource<? extends R>> mapper;

        final boolean delayErrors;

        final AtomicThrowable errors;

        final AtomicLong requested;

        final AtomicReference<SwitchMapSingleObserver<R>> inner;

        static final SwitchMapSingleObserver<Object> INNER_DISPOSED =
                new SwitchMapSingleObserver<>(null);

        Subscription upstream;

        volatile boolean done;

        volatile boolean cancelled;

        long emitted;

        SwitchMapSingleSubscriber(Subscriber<? super R> downstream,
                Function<? super T, ? extends SingleSource<? extends R>> mapper,
                boolean delayErrors) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
            this.errors = new AtomicThrowable();
            this.requested = new AtomicLong();
            this.inner = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(upstream, s)) {
                upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        /** dispose 当前 inner，映射并 CAS 订阅新 SingleSource。 */
        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void onNext(T t) {
            SwitchMapSingleObserver<R> current = inner.get();
            if (current != null) {
                current.dispose();
            }

            SingleSource<? extends R> ss;

            try {
                ss = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null SingleSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.cancel();
                inner.getAndSet((SwitchMapSingleObserver)INNER_DISPOSED);
                onError(ex);
                return;
            }

            SwitchMapSingleObserver<R> observer = new SwitchMapSingleObserver<>(this);

            for (;;) {
                current = inner.get();
                if (current == INNER_DISPOSED) {
                    break;
                }
                if (inner.compareAndSet(current, observer)) {
                    ss.subscribe(observer);
                    break;
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (errors.tryAddThrowableOrReport(t)) {
                if (!delayErrors) {
                    disposeInner();
                }
                done = true;
                drain();
            }
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        void disposeInner() {
            SwitchMapSingleObserver<R> current = inner.getAndSet((SwitchMapSingleObserver)INNER_DISPOSED);
            if (current != null && current != INNER_DISPOSED) {
                current.dispose();
            }
        }

        @Override
        public void request(long n) {
            BackpressureHelper.add(requested, n);
            drain();
        }

        @Override
        public void cancel() {
            cancelled = true;
            upstream.cancel();
            disposeInner();
            errors.tryTerminateAndReport();
        }

        /** inner onError：按 delayErrors 决定 cancel 或继续 drain。 */
        void innerError(SwitchMapSingleObserver<R> sender, Throwable ex) {
            if (inner.compareAndSet(sender, null)) {
                if (errors.tryAddThrowableOrReport(ex)) {
                    if (!delayErrors) {
                        upstream.cancel();
                        disposeInner();
                    }
                    drain();
                }
            } else {
                RxJavaPlugins.onError(ex);
            }
        }

        /** 背压下从 inner.item 取值的 missed-drain 循环。 */
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Subscriber<? super R> downstream = this.downstream;
            AtomicThrowable errors = this.errors;
            AtomicReference<SwitchMapSingleObserver<R>> inner = this.inner;
            AtomicLong requested = this.requested;
            long emitted = this.emitted;

            for (;;) {

                for (;;) {
                    if (cancelled) {
                        return;
                    }

                    if (errors.get() != null) {
                        if (!delayErrors) {
                            errors.tryTerminateConsumer(downstream);
                            return;
                        }
                    }

                    boolean d = done;
                    SwitchMapSingleObserver<R> current = inner.get();
                    boolean empty = current == null;

                    if (d && empty) {
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    if (empty || current.item == null || emitted == requested.get()) {
                        break;
                    }

                    inner.compareAndSet(current, null);

                    downstream.onNext(current.item);

                    emitted++;
                }

                this.emitted = emitted;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /** 缓存 onSuccess 的 item 并触发 parent.drain。 */
        static final class SwitchMapSingleObserver<R>
        extends AtomicReference<Disposable> implements SingleObserver<R> {

            @Serial
            private static final long serialVersionUID = 8042919737683345351L;

            final SwitchMapSingleSubscriber<?, R> parent;

            volatile R item;

            SwitchMapSingleObserver(SwitchMapSingleSubscriber<?, R> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onSuccess(R t) {
                item = t;
                parent.drain();
            }

            @Override
            public void onError(Throwable e) {
                parent.innerError(this, e);
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
