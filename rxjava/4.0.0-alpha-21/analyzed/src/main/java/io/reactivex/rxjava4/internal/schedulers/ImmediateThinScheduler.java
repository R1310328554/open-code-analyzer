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

package io.reactivex.rxjava4.internal.schedulers;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.disposables.*;

/**
 * 轻量即时 Scheduler：仅支持当前线程立即执行、无延迟/无周期任务。
 * <p>
 * 不支持递归调度；返回的 Disposable dispose 无效（任务在 schedule 返回前已执行）。
 */
public final class ImmediateThinScheduler extends Scheduler {

    /**
     * 即时（thin）Scheduler 单例。
     */
    public static final Scheduler INSTANCE = new ImmediateThinScheduler();

    static final Worker WORKER = new ImmediateThinWorker();

    static final Disposable DISPOSED;

    static {
        DISPOSED = Disposable.empty();
        DISPOSED.dispose();
    }

    private ImmediateThinScheduler() {
        // singleton class
    }

    /** 同步 run.run() 并返回已 dispose 的 DISPOSED。 */
    @NonNull
    @Override
    public Disposable scheduleDirect(@NonNull Runnable run) {
        run.run();
        return DISPOSED;
    }

    /** 不支持延迟执行。 */
    @NonNull
    @Override
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("This scheduler doesn't support delayed execution");
    }

    @NonNull
    @Override
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, TimeUnit unit) {
        throw new UnsupportedOperationException("This scheduler doesn't support periodic execution");
    }

    @NonNull
    @Override
    public Worker createWorker() {
        return WORKER;
    }

    /** 无状态 Worker：schedule 立即执行，dispose 无效果。 */
    static final class ImmediateThinWorker extends Worker {

        /** 无操作：本 Worker 不追踪任务。 */
        @Override
        public void dispose() {
            // This worker is always stateless and won't track tasks
        }

        @Override
        public boolean isDisposed() {
            return false; // dispose() has no effect
        }

        /** 当前线程同步执行 run 并返回 DISPOSED。 */
        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable run) {
            run.run();
            return DISPOSED;
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            throw new UnsupportedOperationException("This scheduler doesn't support delayed execution");
        }

        @NonNull
        @Override
        public Disposable schedulePeriodically(@NonNull Runnable run, long initialDelay, long period, TimeUnit unit) {
            throw new UnsupportedOperationException("This scheduler doesn't support periodic execution");
        }
    }
}
