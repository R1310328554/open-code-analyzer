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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.observers.SerializedObserver;

/**
 * 每个 timeout 窗口内仅转发首个 onNext（gate 门控），
 * 窗口内其余值可选经 onDropped 回调丢弃。
 * @param <T> 元素类型
 */
public final class ObservableThrottleFirstTimed<T> extends AbstractObservableWithUpstream<T, T> {
    final long timeout;
    final TimeUnit unit;
    final Scheduler scheduler;
    final Consumer<? super T> onDropped;

    /**
     * @param source 上游 ObservableSource
     * @param timeout 节流窗口长度
     * @param unit 时间单位
     * @param scheduler 调度窗口 reset 的 Scheduler
     * @param onDropped 窗口内被丢弃值的回调（可为 null）
     */
    public ObservableThrottleFirstTimed(
            ObservableSource<T> source,
            long timeout,
            TimeUnit unit,
            Scheduler scheduler,
            Consumer<? super T> onDropped) {
        super(source);
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
        this.onDropped = onDropped;
    }

    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new DebounceTimedObserver<>(
                new SerializedObserver<>(t),
                timeout, unit, scheduler.createWorker(),
                onDropped));
    }

    /** gate 为 true 时阻塞转发；run 重置 gate 开启下一窗口。 */
    static final class DebounceTimedObserver<T>
    extends AtomicReference<Disposable>
    implements Observer<T>, Disposable, Runnable {
        @Serial
        private static final long serialVersionUID = 786994795061867455L;

        final Observer<? super T> downstream;
        final long timeout;
        final TimeUnit unit;
        final Scheduler.Worker worker;
        final Consumer<? super T> onDropped;
        Disposable upstream;
        volatile boolean gate;

        DebounceTimedObserver(
                Observer<? super T> actual,
                long timeout,
                TimeUnit unit,
                Worker worker,
                Consumer<? super T> onDropped) {
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

        /** gate 关闭时转发首项并 schedule reset；否则 onDropped。 */
        @Override
        public void onNext(T t) {
            if (!gate) {
                gate = true;

                downstream.onNext(t);

                Disposable d = get();
                if (d != null) {
                    d.dispose();
                }
                DisposableHelper.replace(this, worker.schedule(this, timeout, unit));
            } else if (onDropped != null) {
                try {
                    onDropped.accept(t);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.dispose();
                    downstream.onError(ex);
                    worker.dispose();
                }
            }
        }

        /** 定时任务：gate=false 开启下一节流窗口。 */
        @Override
        public void run() {
            gate = false;
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
            worker.dispose();
        }

        @Override
        public void onComplete() {
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
    }
}
