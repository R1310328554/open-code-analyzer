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
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;

/**
 * 将 Single 成功值经 mapper 映射为 Iterable，
 * 以带背压的 Flowable 逐元素向下游发射。
 *
 * @param <T> 上游成功值类型
 * @param <R> Iterable 元素类型
 */
public final class SingleFlatMapIterableFlowable<T, R> extends Flowable<R> {

    final SingleSource<T> source;

    final Function<? super T, ? extends Iterable<? extends R>> mapper;

    /**
     * @param source 上游 SingleSource
     * @param mapper 将成功值映射为 Iterable 的函数
     */
    public SingleFlatMapIterableFlowable(SingleSource<T> source,
            Function<? super T, ? extends Iterable<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    /** 订阅 FlatMapIterableObserver 将 Iterable 展开为 Flowable 序列。 */
    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new FlatMapIterableObserver<>(s, mapper));
    }

    /** 背压 Iterable 展开：onSuccess 后 drain 按 requested 逐元素 onNext。 */
    static final class FlatMapIterableObserver<T, R>
    extends BasicIntQueueSubscription<R>
    implements SingleObserver<T> {

        @Serial
        private static final long serialVersionUID = -8938804753851907758L;

        final Subscriber<? super R> downstream;

        final Function<? super T, ? extends Iterable<? extends R>> mapper;

        final AtomicLong requested;

        Disposable upstream;

        volatile Iterator<? extends R> it;

        volatile boolean cancelled;

        boolean outputFused;

        FlatMapIterableObserver(Subscriber<? super R> actual,
                Function<? super T, ? extends Iterable<? extends R>> mapper) {
            this.downstream = actual;
            this.mapper = mapper;
            this.requested = new AtomicLong();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** mapper 获取 Iterator；空 Iterable 则 onComplete，否则 drain。 */
        @Override
        public void onSuccess(T value) {
            Iterator<? extends R> iterator;
            boolean has;
            try {
                iterator = mapper.apply(value).iterator();

                has = iterator.hasNext();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            if (!has) {
                downstream.onComplete();
                return;
            }

            this.it = iterator;
            drain();
        }

        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;
            downstream.onError(e);
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
                drain();
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
        }

        /** wip 门控：按 requested 从 iterator 逐 next 并 onNext/onComplete。 */
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            Subscriber<? super R> a = downstream;
            Iterator<? extends R> iterator = this.it;

            if (outputFused && iterator != null) {
                a.onNext(null);
                a.onComplete();
                return;
            }

            int missed = 1;

            for (;;) {

                if (iterator != null) {
                    long r = requested.get();
                    long e = 0L;

                    if (r == Long.MAX_VALUE) {
                        fastPath(a, iterator);
                        return;
                    }

                    while (e != r) {
                        if (cancelled) {
                            return;
                        }

                        R v;

                        try {
                            v = Objects.requireNonNull(iterator.next(), "The iterator returned a null value");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            a.onError(ex);
                            return;
                        }

                        a.onNext(v);

                        if (cancelled) {
                            return;
                        }

                        e++;

                        boolean b;

                        try {
                            b = iterator.hasNext();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            a.onError(ex);
                            return;
                        }

                        if (!b) {
                            a.onComplete();
                            return;
                        }
                    }

                    if (e != 0L) {
                        BackpressureHelper.produced(requested, e);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }

                if (iterator == null) {
                    iterator = it;
                }
            }
        }

        /** requested==MAX 时无背压限制地逐元素发射。 */
        void fastPath(Subscriber<? super R> a, Iterator<? extends R> iterator) {
            for (;;) {
                if (cancelled) {
                    return;
                }

                R v;

                try {
                    v = iterator.next();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    a.onError(ex);
                    return;
                }

                a.onNext(v);

                if (cancelled) {
                    return;
                }

                boolean b;

                try {
                    b = iterator.hasNext();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    a.onError(ex);
                    return;
                }

                if (!b) {
                    a.onComplete();
                    return;
                }
            }
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & ASYNC) != 0) {
                outputFused = true;
                return ASYNC;
            }
            return NONE;
        }

        @Override
        public void clear() {
            it = null;
        }

        @Override
        public boolean isEmpty() {
            return it == null;
        }

        @Nullable
        @Override
        public R poll() {
            Iterator<? extends R> iterator = it;

            if (iterator != null) {
                R v = Objects.requireNonNull(iterator.next(), "The iterator returned a null value");
                if (!iterator.hasNext()) {
                    it = null;
                }
                return v;
            }
            return null;
        }

    }
}
