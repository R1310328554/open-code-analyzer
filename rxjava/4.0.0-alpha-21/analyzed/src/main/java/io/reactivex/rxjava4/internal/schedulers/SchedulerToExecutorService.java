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

import java.io.Serial;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.core.Scheduler.Worker;
import io.reactivex.rxjava4.exceptions.*;

/**
 * 将 {@link Scheduler} 适配为 {@link ExecutorService}：
 * workerStore 有 Worker 时走 w.schedule，否则 scheduleDirect。
 * @param scheduler 底层 Scheduler
 * @param workerStore 持有 Worker 状态的原子引用
 * @since 4.0.0
 */
public record SchedulerToExecutorService(@NonNull Scheduler scheduler,
        @NonNull AtomicReference<Worker> workerStore) implements ExecutorService {

    /** Worker 可用则 schedule，否则 scheduleDirect。 */
    @Override
    public void execute(Runnable command) {
        if (workerStore.get() instanceof Worker w) {
            w.schedule(command);
        } else {
            scheduler.scheduleDirect(command);
        }
    }

    /** dispose Worker 或 getAndSet(SHUTDOWN) 后 dispose。 */
    @Override
    public void shutdown() {
        if (workerStore.get() instanceof Worker w) {
            w.dispose();
        } else {
            // FIXME, generally we don't want to shut down RxJava schedulers like this!
            // scheduler.shutdown();
            var w = workerStore.getAndSet(Scheduler.Worker.SHUTDOWN);
            if (w != null) {
                w.dispose();
            }
        }
    }

    /** 同 shutdown，返回空列表。 */
    @Override
    public List<Runnable> shutdownNow() {
        if (workerStore.get() instanceof Worker w) {
            w.dispose();
        } else {
            // FIXME, generally we don't want to shut down RxJava schedulers like this!
            // scheduler.shutdown();
            var w = workerStore.getAndSet(Scheduler.Worker.SHUTDOWN);
            if (w != null) {
                w.dispose();
            }
        }
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        var w = workerStore.get();
        return w != null && w.isDisposed();
    }

    @Override
    public boolean isTerminated() {
        return isShutdown();
    }

    /** 轮询 isTerminated 直至超时（Rx 场景下被动等待）。 */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        // FIXME no idea how to passively wait, not really applicable in Rx
        long totalTime = unit.convert(timeout, TimeUnit.MILLISECONDS);

        while (!isTerminated() && totalTime > 0) {
            totalTime--;
            Thread.sleep(1);
        }
        return totalTime > 0;
    }

    /** 通过 CompletableFuture.supplyAsync 在 Scheduler 上执行 Callable。 */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                throw Exceptions.propagate(ex);
            }
        }, this::execute);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                task.run();
                return result;
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                throw Exceptions.propagate(ex);
            }
        }, this::execute);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                throw Exceptions.propagate(ex);
            }
        }, this::execute);
    }

    /** 逐个 submit 并阻塞 get 等待全部完成。 */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        var result = new ArrayList<Future<T>>();
        for (var task : tasks) {
            result.add(submit(task));
        }
        for (var f : result) {
            try {
                f.get();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
            }
        }
        return result;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        var result = new ArrayList<Future<T>>();
        var signal = new CompletableFuture<Void>();
        var signaller = new CompletionSignaller(signal);
        for (var task : tasks) {
            signaller.countUp();
            result.add(submit(() -> {
                try {
                    return task.call();
                } finally {
                    signaller.countDown();
                }
            }));
        }

        signaller.countDown();

        try {
            signal.get(timeout, unit);
            // make sure we have tasks really finished
            for (var f : result) {
                getException(f);
            }
        } catch (TimeoutException | ExecutionException ex) {
            for (var f : result) {
                f.cancel(true);
            }
        }

        return result;
    }

    /** 并行 submit，首个 complete 的 CompletableFuture 胜出并 cancel 其余。 */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("The tasks parameter should contain at least one callable!");
        }

        var completionAll = new CompletableFuture<Void>();
        var signaller = new CompletionSignaller(completionAll);
        var completionPort = new CompletableFuture<CompletedIndexValue<T>>();

        var result = new ArrayList<Future<T>>();
        var i = 0;
        for (var task : tasks) {
            signaller.countUp();
            var j = i;
            result.add(submit(() -> {
                try {
                    var v = task.call();
                    completionPort.complete(new CompletedIndexValue<>(j, v));
                    return v;
                } finally {
                    signaller.countDown();
                }
            }));
            i++;
        }

        signaller.countDown();

        var completionEither = new CompletableFuture<CompletableFuture<?>>();

        completionPort.whenComplete((_, _) -> completionEither.complete(completionPort));
        completionAll.whenComplete((_, _) -> completionEither.complete(completionAll));

        var resultCompletable = completionEither.get();

        if (resultCompletable == completionPort) {
            var k = completionPort.getNow(null);
            for (int j = 0; j < result.size(); j++) {
                if (j != k.index()) {
                    result.get(j).cancel(true);
                }
            }
            return k.value();
        }

        List<Throwable> errors = new ArrayList<>();
        for (var f : result) {
            errors.add(getException(f));
        }
        var composite = new CompositeException(errors);
        throw new ExecutionException(composite);
    }

    /** invokeAny 中记录获胜任务索引与返回值。 */
    record CompletedIndexValue<T>(int index, T value) {
    }

    /** 计数 invokeAny/invokeAll 中未完成任务数，归零时 complete signal。 */
    static final class CompletionSignaller extends AtomicInteger {
        @Serial
        private static final long serialVersionUID = 4179219399191354619L;

        final CompletableFuture<Void> signal;

        CompletionSignaller(CompletableFuture<Void> signal) {
            super(1);
            this.signal = signal;
        }

        void countUp() {
            incrementAndGet();
        }

        void countDown() {
            if (decrementAndGet() == 0) {
                signal.complete(null);
            }
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("The tasks parameter should contain at least one callable!");
        }

        var completionAll = new CompletableFuture<Void>();
        var signaller = new CompletionSignaller(completionAll);
        var completionPort = new CompletableFuture<CompletedIndexValue<T>>();

        var result = new ArrayList<Future<T>>();
        var i = 0;
        for (var task : tasks) {
            signaller.countUp();
            var j = i;
            result.add(submit(() -> {
                try {
                    var v = task.call();
                    completionPort.complete(new CompletedIndexValue<>(j, v));
                    return v;
                } finally {
                    signaller.countDown();
                }
            }));
            i++;
        }

        signaller.countDown();

        var completionEither = new CompletableFuture<CompletableFuture<?>>();

        completionPort.whenComplete((_, _) -> completionEither.complete(completionPort));
        completionAll.whenComplete((_, _) -> completionEither.complete(completionAll));

        var resultCompletable = completionEither.get(timeout, unit);

        if (resultCompletable == completionPort) {
            var k = completionPort.getNow(null);
            for (int j = 0; j < result.size(); j++) {
                if (j != k.index()) {
                    result.get(j).cancel(true);
                }
            }
            return k.value();
        }

        List<Throwable> errors = new ArrayList<>();
        for (var f : result) {
            errors.add(getException(f));
        }
        var composite = new CompositeException(errors);
        throw new ExecutionException(composite);
    }

    static Throwable getException(Future<?> f) throws InterruptedException {
        try {
            f.get();
        } catch (ExecutionException ex) {
            return ex.getCause();
        }
        return null;
    }
}
