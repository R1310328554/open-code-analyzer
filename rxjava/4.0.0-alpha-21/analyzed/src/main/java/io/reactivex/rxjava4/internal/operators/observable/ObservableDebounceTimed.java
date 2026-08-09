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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.observers.SerializedObserver;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 定时防抖：每次 onNext 重置计时器，超时后转发最新值；
 * 可选 {@link Consumer} 在丢弃旧值时回调。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableDebounceTimed<T> extends AbstractObservableWithUpstream<T, T> {
    final long timeout;
    final TimeUnit unit;
    final Scheduler scheduler;
    final Consumer<? super T> onDropped;

    /**
     * @param source 上游 ObservableSource
     * @param timeout 防抖超时量
     * @param unit 时间单位
     * @param scheduler 调度计时任务的 Scheduler
     * @param onDropped 被新值顶替时丢弃的旧值回调（可为 null）
     */
    public ObservableDebounceTimed(ObservableSource<T> source, long timeout, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {
        super(source);
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
        this.onDropped = onDropped;
    }

    /** 在 scheduler Worker 上创建 DebounceTimedObserver。 */
    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new DebounceTimedObserver<>(
                new SerializedObserver<>(t), timeout, unit, scheduler.createWorker(), onDropped));
    }

    /** 维护 timer 与 index，每次 onNext  reschedule DebounceEmitter。 */
    static final class DebounceTimedObserver<T>
    implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        final long timeout;
        final TimeUnit unit;
        final Scheduler.Worker worker;
        final Consumer<? super T> onDropped;

        Disposable upstream;

        DebounceEmitter<T> timer;

        volatile long index;

        boolean done;

        DebounceTimedObserver(Observer<? super T> actual, long timeout, TimeUnit unit, Worker worker, Consumer<? super T> onDropped) {
            this.downstream = actual;
            this.timeout = timeout;
            this.unit = unit;
            this.worker = worker;
            this.onDropped = onDropped;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            long idx = index + 1;
            index = idx;

            DebounceEmitter<T> currentEmitter = timer;
            if (currentEmitter != null) {
                currentEmitter.dispose();
            }

            if (onDropped != null && currentEmitter != null) {
                try {
                    onDropped.accept(timer.value);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.dispose();
                    downstream.onError(ex);
                    done = true;
                }
            }

            DebounceEmitter<T> newEmitter = new DebounceEmitter<>(t, idx, this);
            timer = newEmitter;
            newEmitter.setResource(worker.schedule(newEmitter, timeout, unit));
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            Disposable d = timer;
            if (d != null) {
                d.dispose();
            }
            done = true;
            downstream.onError(t);
            worker.dispose();
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;

            DebounceEmitter<T> d = timer;
            if (d != null) {
                d.dispose();
            }

            if (d != null) {
                d.run();
            }
            downstream.onComplete();
            worker.dispose();
        }

        @Override
        public void dispose() {
            upstream.dispose();
            worker.dispose();
        }

        @Override
        public boolean isDisposed() {
            return worker.isDisposed();
        }

        /** index 匹配时转发值并 dispose 对应 emitter。 */
        void emit(long idx, T t, DebounceEmitter<T> emitter) {
            if (idx == index) {
                downstream.onNext(t);
                emitter.dispose();
            }
        }
    }

    /** 定时 Runnable，到期后通知 parent emit 缓存值。 */
    static final class DebounceEmitter<T> extends AtomicReference<Disposable> implements Runnable, Disposable {

        @Serial
        private static final long serialVersionUID = 6812032969491025141L;

        final T value;
        final long idx;
        final DebounceTimedObserver<T> parent;

        final AtomicBoolean once = new AtomicBoolean();

        DebounceEmitter(T value, long idx, DebounceTimedObserver<T> parent) {
            this.value = value;
            this.idx = idx;
            this.parent = parent;
        }

        @Override
        public void run() {
            if (once.compareAndSet(false, true)) {
                parent.emit(idx, value, this);
            }
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return get() == DisposableHelper.DISPOSED;
        }

        public void setResource(Disposable d) {
            DisposableHelper.replace(this, d);
        }
    }
}
