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

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/**
 * 组合多个 Observable 的最新值：各源均至少 emit 一次后才调用 combiner，
 * 之后任一源 emit 都会触发新的组合结果。
 * @param <T> 各源元素类型
 * @param <R> 组合结果类型
 */
public final class ObservableCombineLatest<T, R> extends Observable<R> {
    final ObservableSource<? extends T>[] sources;
    final Iterable<? extends ObservableSource<? extends T>> sourcesIterable;
    final Function<? super Object[], ? extends R> combiner;
    final int bufferSize;
    final boolean delayError;

    /**
     * @param sources Observable 数组（可为 null，则用 sourcesIterable）
     * @param sourcesIterable 可迭代的 Observable 源
     * @param combiner 组合 latest 数组的函数
     * @param bufferSize 组合结果队列容量
     * @param delayError 是否延迟错误至所有源终止
     */
    public ObservableCombineLatest(ObservableSource<? extends T>[] sources,
            Iterable<? extends ObservableSource<? extends T>> sourcesIterable,
            Function<? super Object[], ? extends R> combiner, int bufferSize,
            boolean delayError) {
        this.sources = sources;
        this.sourcesIterable = sourcesIterable;
        this.combiner = combiner;
        this.bufferSize = bufferSize;
        this.delayError = delayError;
    }

    /** 解析源列表并订阅 LatestCoordinator。 */
    @Override
    @SuppressWarnings("unchecked")
    public void subscribeActual(Observer<? super R> observer) {
        ObservableSource<? extends T>[] sources = this.sources;
        int count = 0;
        if (sources == null) {
            sources = new ObservableSource[8];
            try {
                for (ObservableSource<? extends T> p : sourcesIterable) {
                    if (count == sources.length) {
                        ObservableSource<? extends T>[] b = new ObservableSource[count + (count >> 2)];
                        System.arraycopy(sources, 0, b, 0, count);
                        sources = b;
                    }
                    sources[count++] = Objects.requireNonNull(p, "The Iterator returned a null ObservableSource");
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return;
            }
        } else {
            count = sources.length;
        }

        if (count == 0) {
            EmptyDisposable.complete(observer);
            return;
        }

        LatestCoordinator<T, R> lc = new LatestCoordinator<>(observer, combiner, count, bufferSize, delayError);
        lc.subscribe(sources);
    }

    /** 维护各源 latest 数组、组合队列与 drain 循环。 */
    static final class LatestCoordinator<T, R> extends AtomicInteger implements Disposable {

        @Serial
        private static final long serialVersionUID = 8567835998786448817L;
        final Observer<? super R> downstream;
        final Function<? super Object[], ? extends R> combiner;
        final CombinerObserver<T, R>[] observers;
        Object[] latest;
        final SpscLinkedArrayQueue<Object[]> queue;
        final boolean delayError;

        volatile boolean cancelled;

        volatile boolean done;

        final AtomicThrowable errors = new AtomicThrowable();

        int active;
        int complete;

        @SuppressWarnings("unchecked")
        LatestCoordinator(Observer<? super R> actual,
                Function<? super Object[], ? extends R> combiner,
                int count, int bufferSize, boolean delayError) {
            this.downstream = actual;
            this.combiner = combiner;
            this.delayError = delayError;
            this.latest = new Object[count];
            CombinerObserver<T, R>[] as = new CombinerObserver[count];
            for (int i = 0; i < count; i++) {
                as[i] = new CombinerObserver<>(this, i);
            }
            this.observers = as;
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
        }

        public void subscribe(ObservableSource<? extends T>[] sources) {
            Observer<T>[] as = observers;
            int len = as.length;
            downstream.onSubscribe(this);
            for (int i = 0; i < len; i++) {
                if (done || cancelled) {
                    return;
                }
                sources[i].subscribe(as[i]);
            }
        }

        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;
                cancelSources();
                drain();
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        void cancelSources() {
            for (CombinerObserver<T, R> observer : observers) {
                observer.dispose();
            }
        }

        void clear(SpscLinkedArrayQueue<?> q) {
            synchronized (this) {
                latest = null;
            }
            q.clear();
        }

        /** 从 queue 取 latest 快照、apply combiner 并 onNext。 */
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            final SpscLinkedArrayQueue<Object[]> q = queue;
            final Observer<? super R> a = downstream;
            final boolean delayError = this.delayError;

            int missed = 1;
            for (;;) {

                for (;;) {
                    if (cancelled) {
                        clear(q);
                        errors.tryTerminateAndReport();
                        return;
                    }

                    if (!delayError && errors.get() != null) {
                        cancelSources();
                        clear(q);
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    boolean d = done;
                    Object[] s = q.poll();
                    boolean empty = s == null;

                    if (d && empty) {
                        clear(q);
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    R v;

                    try {
                        v = Objects.requireNonNull(combiner.apply(s), "The combiner returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        errors.tryAddThrowableOrReport(ex);
                        cancelSources();
                        clear(q);
                        errors.tryTerminateConsumer(a);
                        return;
                    }

                    a.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /** 更新 index 处 latest；全部就绪时 clone 入队并 drain。 */
        void innerNext(int index, T item) {
            boolean shouldDrain = false;
            synchronized (this) {
                Object[] latest = this.latest;
                if (latest == null) {
                    return;
                }
                Object o = latest[index];
                int a = active;
                if (o == null) {
                    active = ++a;
                }
                latest[index] = item;
                if (a == latest.length) {
                    queue.offer(latest.clone());
                    shouldDrain = true;
                }
            }
            if (shouldDrain) {
                drain();
            }
        }

        void innerError(int index, Throwable ex) {
            if (errors.tryAddThrowableOrReport(ex)) {
                boolean cancelOthers = true;
                if (delayError) {
                    synchronized (this) {
                        Object[] latest = this.latest;
                        if (latest == null) {
                            return;
                        }
                        cancelOthers = latest[index] == null;
                        if (cancelOthers || ++complete == latest.length) {
                            done = true;
                        }
                    }
                }
                if (cancelOthers) {
                    cancelSources();
                }
                drain();
            }
        }

        void innerComplete(int index) {
            boolean cancelOthers = false;
            synchronized (this) {
                Object[] latest = this.latest;
                if (latest == null) {
                    return;
                }

                cancelOthers = latest[index] == null;
                if (cancelOthers || ++complete == latest.length) {
                    done = true;
                }
            }
            if (cancelOthers) {
                cancelSources();
            }
            drain();
        }

    }

    /** 订阅单个源并将信号 relay 到 LatestCoordinator。 */
    static final class CombinerObserver<T, R> extends AtomicReference<Disposable> implements Observer<T> {
        @Serial
        private static final long serialVersionUID = -4823716997131257941L;

        final LatestCoordinator<T, R> parent;

        final int index;

        CombinerObserver(LatestCoordinator<T, R> parent, int index) {
            this.parent = parent;
            this.index = index;
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onNext(T t) {
            parent.innerNext(index, t);
        }

        @Override
        public void onError(Throwable t) {
            parent.innerError(index, t);
        }

        @Override
        public void onComplete() {
            parent.innerComplete(index);
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }
    }
}
