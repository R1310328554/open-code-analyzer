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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.*;
/**
 * 在指定 {@link Scheduler} 上异步订阅上游 {@link MaybeSource}。
 *
 * @param <T> 元素类型
 */
public final class MaybeSubscribeOn<T> extends AbstractMaybeWithUpstream<T, T> {

    final Scheduler scheduler;

    /**
     * @param source 上游 MaybeSource
     * @param scheduler 订阅调度器
     */
    public MaybeSubscribeOn(MaybeSource<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    /** 先 onSubscribe(parent)，再 scheduleDirect 订阅上游。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        SubscribeOnMaybeObserver<T> parent = new SubscribeOnMaybeObserver<>(observer);
        observer.onSubscribe(parent);

        parent.task.replace(scheduler.scheduleDirect(new SubscribeTask<>(parent, source)));
    }

    /** 在 scheduler 线程执行 source.subscribe(observer)。 */
    record SubscribeTask<T>(MaybeObserver<? super T> observer, MaybeSource<T> source) implements Runnable {

        @Override
            public void run() {
                source.subscribe(observer);
            }
        }

    /** task 持有 schedule 的 Disposable；事件 relay 到 downstream。 */
    static final class SubscribeOnMaybeObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T>, Disposable {

        final SequentialDisposable task;

        @Serial
        private static final long serialVersionUID = 8571289934935992137L;

        final MaybeObserver<? super T> downstream;

        SubscribeOnMaybeObserver(MaybeObserver<? super T> downstream) {
            this.downstream = downstream;
            this.task = new SequentialDisposable();
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
            task.dispose();
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
