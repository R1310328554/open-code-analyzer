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

/* ===== [OCA 中文解析] =====
文件意图总览

RxJava 全局插件与 hook 注入：错误处理、Scheduler 装饰、assembly/subscribe 拦截及各类 onXxx 回调。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.plugins;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.Flow.Subscriber;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.schedulers.*;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.parallel.ParallelFlowable;
import io.reactivex.rxjava4.schedulers.Schedulers;
/* ===== [OCA 中文解析] =====
class RxJavaPlugins — 意图说明

volatile hook 字段与 onAssembly/onSubscribe 拦截入口。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 【说明】Utility class to inject handlers to certain standard RxJava operations.；/
 * Utility class to inject handlers to certain standard RxJava operations.
 */
public final class RxJavaPlugins {
    @Nullable
    static volatile Consumer<? super Throwable> errorHandler;

    @Nullable
    static volatile Function<? super Runnable, ? extends Runnable> onScheduleHandler;

    @Nullable
    static volatile Function<? super Supplier<Scheduler>, ? extends Scheduler> onInitComputationHandler;

    @Nullable
    static volatile Function<? super Supplier<Scheduler>, ? extends Scheduler> onInitSingleHandler;

    @Nullable
    static volatile Function<? super Supplier<Scheduler>, ? extends Scheduler> onInitCachedHandler;

    @Nullable
    static volatile Function<? super Supplier<Scheduler>, ? extends Scheduler> onInitVirtualHandler;

    @Nullable
    static volatile Function<? super Supplier<Scheduler>, ? extends Scheduler> onInitNewThreadHandler;

    @Nullable
    static volatile Function<? super Scheduler, ? extends Scheduler> onComputationHandler;

    @Nullable
    static volatile Function<? super Scheduler, ? extends Scheduler> onSingleHandler;

    @Nullable
    static volatile Function<? super Scheduler, ? extends Scheduler> onCachedHandler;

    @Nullable
    static volatile Function<? super Scheduler, ? extends Scheduler> onVirtualHandler;

    @Nullable
    static volatile Function<? super Scheduler, ? extends Scheduler> onNewThreadHandler;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super Flowable, ? extends Flowable> onFlowableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super ConnectableFlowable, ? extends ConnectableFlowable> onConnectableFlowableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super Observable, ? extends Observable> onObservableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super ConnectableObservable, ? extends ConnectableObservable> onConnectableObservableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super Maybe, ? extends Maybe> onMaybeAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super Single, ? extends Single> onSingleAssembly;

    @Nullable
    static volatile Function<? super Completable, ? extends Completable> onCompletableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super ParallelFlowable, ? extends ParallelFlowable> onParallelAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super Streamable, ? extends Streamable> onStreamableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile BiFunction<? super Flowable, @NonNull ? super Subscriber, @NonNull ? extends Subscriber> onFlowableSubscribe;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile BiFunction<? super Maybe, @NonNull ? super MaybeObserver, @NonNull ? extends MaybeObserver> onMaybeSubscribe;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile BiFunction<? super Observable, @NonNull ? super Observer, @NonNull ? extends Observer> onObservableSubscribe;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile BiFunction<? super Single, @NonNull ? super SingleObserver, @NonNull ? extends SingleObserver> onSingleSubscribe;

    @Nullable
    static volatile BiFunction<? super Completable, @NonNull ? super CompletableObserver, @NonNull ? extends CompletableObserver> onCompletableSubscribe;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile BiFunction<? super ParallelFlowable, @NonNull ? super Subscriber<@NonNull ?>[], @NonNull ? extends Subscriber<@NonNull ?>[]> onParallelSubscribe;

    @Nullable
    static volatile BooleanSupplier onBeforeBlocking;

    /** Prevents changing the plugins. */
    static volatile boolean lockdown;

    /**
 * 【说明】If true, attempting to run a blockingX operation on a (by default)；computatio...
     * If true, attempting to run a blockingX operation on a (by default)
     * computation or single scheduler will throw an IllegalStateException.
     */
    static volatile boolean failNonBlockingScheduler;

    /**
 * 【说明】Prevents changing the plugins from then on.；<p>This allows container-like env...
     * Prevents changing the plugins from then on.
     * <p>This allows container-like environments to prevent clients
     * messing with plugins.
     */
    /** 静态方法 lockdown：void 返回值工具入口。 */

    /** 静态方法 lockdown：配置或执行 hook。 */


    public static void lockdown() {
        lockdown = true;
    }

    /**
     * 若满足条件则返回 true： the plugins were locked down.
     * @return 插件是否已锁定
     */
    /** 静态方法 isLockdown：boolean 返回值工具入口。 */

    /** 静态方法 isLockdown：返回插件/配置布尔状态。 */


    public static boolean isLockdown() {
        return lockdown;
    }

    /**
     * Enables or disables the blockingX operators to fail
     * with an IllegalStateException on a non-blocking
     * scheduler such as computation or single.
     * <p>History: 2.0.5 - experimental
     * @param enable 启用或禁用 the feature
     * @since 2.1
     */
    /** 静态方法 setFailOnNonBlockingScheduler：void 返回值工具入口。 */

    /** 静态方法 setFailOnNonBlockingScheduler：配置或执行 hook。 */


    public static void setFailOnNonBlockingScheduler(boolean enable) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        failNonBlockingScheduler = enable;
    }

    /**
     * 若满足条件则返回 true： the blockingX operators fail
     * with an IllegalStateException on a non-blocking scheduler
     * such as computation or single.
     * <p>History: 2.0.5 - experimental
     * @return true if the blockingX operators fail on a non-blocking scheduler
     * @since 2.1
     */
    /** 静态方法 isFailOnNonBlockingScheduler：boolean 返回值工具入口。 */

    /** 静态方法 isFailOnNonBlockingScheduler：返回插件/配置布尔状态。 */


    public static boolean isFailOnNonBlockingScheduler() {
        return failNonBlockingScheduler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Scheduler, ? extends Scheduler> getComputationSchedulerHandler() {
        return onComputationHandler;
    }

    /**
     * 返回 hook consumer.
     * @return hook Consumer，可能为 null
     */
    @Nullable
    public static Consumer<? super Throwable> getErrorHandler() {
        return errorHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Supplier<Scheduler>, ? extends Scheduler> getInitComputationSchedulerHandler() {
        return onInitComputationHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Supplier<Scheduler>, ? extends Scheduler> getInitCachedSchedulerHandler() {
        return onInitCachedHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Supplier<Scheduler>, ? extends Scheduler> getInitVirtualSchedulerHandler() {
        return onInitVirtualHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Supplier<Scheduler>, ? extends Scheduler> getInitNewThreadSchedulerHandler() {
        return onInitNewThreadHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Supplier<Scheduler>, ? extends Scheduler> getInitSingleSchedulerHandler() {
        return onInitSingleHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Scheduler, ? extends Scheduler> getCachedSchedulerHandler() {
        return onCachedHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Scheduler, ? extends Scheduler> getVirtualSchedulerHandler() {
        return onVirtualHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Scheduler, ? extends Scheduler> getNewThreadSchedulerHandler() {
        return onNewThreadHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Runnable, ? extends Runnable> getScheduleHandler() {
        return onScheduleHandler;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Scheduler, ? extends Scheduler> getSingleSchedulerHandler() {
        return onSingleHandler;
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler a {@link Supplier} which returns the hook's input value
     * @return  value returned by the hook, not null
     * @throws NullPointerException 若参数为 null the supplier parameter or its result are null
     */
    @NonNull
    /** 静态方法 initComputationScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler initComputationScheduler(@NonNull Supplier<Scheduler> defaultScheduler) {
        Objects.requireNonNull(defaultScheduler, "Scheduler Supplier can't be null");
        Function<? super Supplier<Scheduler>, ? extends Scheduler> f = onInitComputationHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler); // JIT will skip this
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler a {@link Supplier} which returns the hook's input value
     * @return  value returned by the hook, not null
     * @throws NullPointerException 若参数为 null the supplier parameter or its result are null
     */
    @NonNull
    /** 静态方法 initCachedScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler initCachedScheduler(@NonNull Supplier<Scheduler> defaultScheduler) {
        Objects.requireNonNull(defaultScheduler, "Scheduler Supplier can't be null");
        Function<? super Supplier<Scheduler>, ? extends Scheduler> f = onInitCachedHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler a {@link Supplier} which returns the hook's input value
     * @return  value returned by the hook, not null
     * @throws NullPointerException 若参数为 null the supplier parameter or its result are null
     */
    @NonNull
    /** 静态方法 initVirtualScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler initVirtualScheduler(@NonNull Supplier<Scheduler> defaultScheduler) {
        Objects.requireNonNull(defaultScheduler, "Scheduler Supplier can't be null");
        Function<? super Supplier<Scheduler>, ? extends Scheduler> f = onInitVirtualHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler a {@link Supplier} which returns the hook's input value
     * @return  value returned by the hook, not null
     * @throws NullPointerException 若参数为 null the supplier parameter or its result are null
     */
    @NonNull
    /** 静态方法 initNewThreadScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler initNewThreadScheduler(@NonNull Supplier<Scheduler> defaultScheduler) {
        Objects.requireNonNull(defaultScheduler, "Scheduler Supplier can't be null");
        Function<? super Supplier<Scheduler>, ? extends Scheduler> f = onInitNewThreadHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler a {@link Supplier} which returns the hook's input value
     * @return  value returned by the hook, not null
     * @throws NullPointerException 若参数为 null the supplier parameter or its result are null
     */
    @NonNull
    /** 静态方法 initSingleScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler initSingleScheduler(@NonNull Supplier<Scheduler> defaultScheduler) {
        Objects.requireNonNull(defaultScheduler, "Scheduler Supplier can't be null");
        Function<? super Supplier<Scheduler>, ? extends Scheduler> f = onInitSingleHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler the hook's input value
     * @return  value returned by the hook
     */
    @NonNull
    /** 静态方法 onComputationScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler onComputationScheduler(@NonNull Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onComputationHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * Called when an undeliverable error occurs.
     * <p>
     * Undeliverable errors are those {@code Observer.onError()} invocations that are not allowed to happen on
     * the given consumer type ({@code Observer}, {@code Subscriber}, etc.) due to protocol restrictions
     * because the consumer has either disposed/cancelled its {@code Disposable}/{@code Subscription} or
     * has already terminated with an {@code onError()} or {@code onComplete()} signal.
     * <p>
     * By default, this global error handler prints the stacktrace via {@link Throwable#printStackTrace()}
     * and calls {@link java.lang.Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)}
     * on the current thread.
     * <p>
     * Note that on some platforms, the platform runtime terminates the current application with an error if such
     * uncaught exceptions happen. In this case, it is recommended the application installs a global error
     * handler via the {@link #setErrorHandler(Consumer)} plugin method.
     *
     * @param error 错误 to report
     * @see #getErrorHandler()
     * @see #setErrorHandler(Consumer)
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling">Error handling Wiki</a>
     */
    /** 静态方法 onError：void 返回值工具入口。 */

    /** 静态方法 onError：配置或执行 hook。 */


    public static void onError(@NonNull Throwable error) {
        Consumer<? super Throwable> f = errorHandler;

        if (error == null) {
            error = ExceptionHelper.createNullPointerException("onError called with a null Throwable.");
        } else {
            if (!isBug(error)) {
                error = new UndeliverableException(error);
            }
        }

        if (f != null) {
            try {
                f.accept(error);
                return;
            } catch (Throwable e) {
                // Exceptions.throwIfFatal(e); TODO decide
                e.printStackTrace(); // NOPMD
                uncaught(e);
            }
        }

        error.printStackTrace(); // NOPMD
        uncaught(error);
    }

    /**
     * 检查是否 the given error is one of the already named
     * bug cases that should pass through {@link #onError(Throwable)}
     * as is.
     * @param error 错误 to check
     * @return true if 错误 should pass through, false if
     * it may be wrapped into an UndeliverableException
     */
    static boolean isBug(Throwable error) {
        // user forgot to add the onError handler in subscribe
        if (error instanceof OnErrorNotImplementedException) {
            return true;
        }
        // the sender didn't honor the request amount
        if (error instanceof MissingBackpressureException) {
            return true;
        }
        // it's either due to an operator bug or concurrent onNext
        if (error instanceof QueueOverflowException) {
            return true;
        }
        // general protocol violations
        // it's either due to an operator bug or concurrent onNext
        if (error instanceof IllegalStateException) {
            return true;
        }
        // nulls are generally not allowed
        // likely an operator bug or missing null-check
        if (error instanceof NullPointerException) {
            return true;
        }
        // bad arguments, likely invalid user input
        if (error instanceof IllegalArgumentException) {
            return true;
        }
        // Crash while handling an exception
        if (error instanceof CompositeException) {
            return true;
        }
        // everything else is probably due to lifecycle limits
        return false;
    }

    static void uncaught(@NonNull Throwable error) {
        Thread currentThread = Thread.currentThread();
        UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        handler.uncaughtException(currentThread, error);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler the hook's input value
     * @return  value returned by the hook
     */
    @NonNull
    /** 静态方法 onCachedScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler onCachedScheduler(@NonNull Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onCachedHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler the hook's input value
     * @return  value returned by the hook
     */
    @NonNull
    /** 静态方法 onVirtualScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler onVirtualScheduler(@NonNull Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onVirtualHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler the hook's input value
     * @return  value returned by the hook
     */
    @NonNull
    /** 静态方法 onNewThreadScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler onNewThreadScheduler(@NonNull Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onNewThreadHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
 * 【说明】Called when a task is scheduled.；/
     * Called when a task is scheduled.
     * @param run the runnable instance
     * @return  replacement runnable
     */
    @NonNull
    /** 静态方法 onSchedule：Runnable 返回值工具入口。 */

    public static Runnable onSchedule(@NonNull Runnable run) {
        Objects.requireNonNull(run, "run is null");

        Function<? super Runnable, ? extends Runnable> f = onScheduleHandler;
        if (f == null) {
            return run;
        }
        return apply(f, run);
    }

    /**
     * 调用 associated hook function.
     * @param defaultScheduler the hook's input value
     * @return  value returned by the hook
     */
    @NonNull
    /** 静态方法 onSingleScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler onSingleScheduler(@NonNull Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onSingleHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
 * 【说明】Removes all handlers and resets to default behavior.；/
     * Removes all handlers and resets to default behavior.
     */
    /** 静态方法 reset：void 返回值工具入口。 */

    /** 静态方法 reset：配置或执行 hook。 */


    public static void reset() {
        setErrorHandler(null);
        setScheduleHandler(null);

        setComputationSchedulerHandler(null);
        setInitComputationSchedulerHandler(null);

        setCachedSchedulerHandler(null);
        setInitCachedSchedulerHandler(null);

        setVirtualSchedulerHandler(null);
        setInitVirtualSchedulerHandler(null);

        setSingleSchedulerHandler(null);
        setInitSingleSchedulerHandler(null);

        setNewThreadSchedulerHandler(null);
        setInitNewThreadSchedulerHandler(null);

        setOnFlowableAssembly(null);
        setOnFlowableSubscribe(null);

        setOnObservableAssembly(null);
        setOnObservableSubscribe(null);

        setOnSingleAssembly(null);
        setOnSingleSubscribe(null);

        setOnCompletableAssembly(null);
        setOnCompletableSubscribe(null);

        setOnConnectableFlowableAssembly(null);
        setOnConnectableObservableAssembly(null);

        setOnMaybeAssembly(null);
        setOnMaybeSubscribe(null);

        setOnParallelAssembly(null);
        setOnParallelSubscribe(null);

        setOnStreamableAssembly(null);

        setFailOnNonBlockingScheduler(false);
        setOnBeforeBlocking(null);
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed
     */
    /** 静态方法 setComputationSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setComputationSchedulerHandler：配置或执行 hook。 */


    public static void setComputationSchedulerHandler(@Nullable Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onComputationHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed
     */
    /** 静态方法 setErrorHandler：void 返回值工具入口。 */

    /** 静态方法 setErrorHandler：配置或执行 hook。 */


    public static void setErrorHandler(@Nullable Consumer<? super Throwable> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        errorHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed, but 函数 may not return null
     */
    /** 静态方法 setInitComputationSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setInitComputationSchedulerHandler：配置或执行 hook。 */


    public static void setInitComputationSchedulerHandler(@Nullable Function<? super Supplier<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitComputationHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed, but 函数 may not return null
     */
    /** 静态方法 setInitCachedSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setInitCachedSchedulerHandler：配置或执行 hook。 */


    public static void setInitCachedSchedulerHandler(@Nullable Function<? super Supplier<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitCachedHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed, but 函数 may not return null
     */
    /** 静态方法 setInitVirtualSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setInitVirtualSchedulerHandler：配置或执行 hook。 */


    public static void setInitVirtualSchedulerHandler(@Nullable Function<? super Supplier<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitVirtualHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed, but 函数 may not return null
     */
    /** 静态方法 setInitNewThreadSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setInitNewThreadSchedulerHandler：配置或执行 hook。 */


    public static void setInitNewThreadSchedulerHandler(@Nullable Function<? super Supplier<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitNewThreadHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed, but 函数 may not return null
     */
    /** 静态方法 setInitSingleSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setInitSingleSchedulerHandler：配置或执行 hook。 */


    public static void setInitSingleSchedulerHandler(@Nullable Function<? super Supplier<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitSingleHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed
     */
    /** 静态方法 setCachedSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setCachedSchedulerHandler：配置或执行 hook。 */


    public static void setCachedSchedulerHandler(@Nullable Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onCachedHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed
     */
    /** 静态方法 setVirtualSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setVirtualSchedulerHandler：配置或执行 hook。 */


    public static void setVirtualSchedulerHandler(@Nullable Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onVirtualHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed
     */
    /** 静态方法 setNewThreadSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setNewThreadSchedulerHandler：配置或执行 hook。 */


    public static void setNewThreadSchedulerHandler(@Nullable Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onNewThreadHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed
     */
    /** 静态方法 setScheduleHandler：void 返回值工具入口。 */

    /** 静态方法 setScheduleHandler：配置或执行 hook。 */


    public static void setScheduleHandler(@Nullable Function<? super Runnable, ? extends Runnable> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onScheduleHandler = handler;
    }

    /**
     * 设置 specific hook function.
     * @param handler the hook function to set, null allowed
     */
    /** 静态方法 setSingleSchedulerHandler：void 返回值工具入口。 */

    /** 静态方法 setSingleSchedulerHandler：配置或执行 hook。 */


    public static void setSingleSchedulerHandler(@Nullable Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onSingleHandler = handler;
    }

    /**
 * 【说明】Revokes the lockdown, only for testing purposes.；/
     * Revokes the lockdown, only for testing purposes.
     */
    /* test. */static void unlock() {
        lockdown = false;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static Function<? super Completable, ? extends Completable> getOnCompletableAssembly() {
        return onCompletableAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    public static BiFunction<? super Completable, @NonNull ? super CompletableObserver, @NonNull ? extends CompletableObserver> getOnCompletableSubscribe() {
        return onCompletableSubscribe;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public static Function<? super Flowable, ? extends Flowable> getOnFlowableAssembly() {
        return onFlowableAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public static Function<? super ConnectableFlowable, ? extends ConnectableFlowable> getOnConnectableFlowableAssembly() {
        return onConnectableFlowableAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static BiFunction<? super Flowable, @NonNull ? super Subscriber, @NonNull ? extends Subscriber> getOnFlowableSubscribe() {
        return onFlowableSubscribe;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static BiFunction<? super Maybe, @NonNull ? super MaybeObserver, @NonNull ? extends MaybeObserver> getOnMaybeSubscribe() {
        return onMaybeSubscribe;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static Function<? super Maybe, ? extends Maybe> getOnMaybeAssembly() {
        return onMaybeAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static Function<? super Streamable, ? extends Streamable> getOnStreamableAssembly() {
        return onStreamableAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static Function<? super Single, ? extends Single> getOnSingleAssembly() {
        return onSingleAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static BiFunction<? super Single, @NonNull ? super SingleObserver, @NonNull ? extends SingleObserver> getOnSingleSubscribe() {
        return onSingleSubscribe;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static Function<? super Observable, ? extends Observable> getOnObservableAssembly() {
        return onObservableAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static Function<? super ConnectableObservable, ? extends ConnectableObservable> getOnConnectableObservableAssembly() {
        return onConnectableObservableAssembly;
    }

    /**
     * 返回当前 hook function.
     * @return hook 函数，可能为 null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static BiFunction<? super Observable, @NonNull ? super Observer, @NonNull ? extends Observer> getOnObservableSubscribe() {
        return onObservableSubscribe;
    }

    /**
     * 设置 specific hook function.
     * @param onCompletableAssembly the hook function to set, null allowed
     */
    /** 静态方法 setOnCompletableAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnCompletableAssembly：配置或执行 hook。 */


    public static void setOnCompletableAssembly(@Nullable Function<? super Completable, ? extends Completable> onCompletableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onCompletableAssembly = onCompletableAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onCompletableSubscribe the hook function to set, null allowed
     */
    /** 静态方法 setOnCompletableSubscribe：void 返回值工具入口。 */

    /** 静态方法 setOnCompletableSubscribe：配置或执行 hook。 */


    public static void setOnCompletableSubscribe(
            @Nullable BiFunction<? super Completable, @NonNull ? super CompletableObserver, @NonNull ? extends CompletableObserver> onCompletableSubscribe) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onCompletableSubscribe = onCompletableSubscribe;
    }

    /**
     * 设置 specific hook function.
     * @param onFlowableAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnFlowableAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnFlowableAssembly：配置或执行 hook。 */


    public static void setOnFlowableAssembly(@Nullable Function<? super Flowable, ? extends Flowable> onFlowableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onFlowableAssembly = onFlowableAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onMaybeAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnMaybeAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnMaybeAssembly：配置或执行 hook。 */


    public static void setOnMaybeAssembly(@Nullable Function<? super Maybe, ? extends Maybe> onMaybeAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onMaybeAssembly = onMaybeAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onStreamableAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnStreamableAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnStreamableAssembly：配置或执行 hook。 */


    public static void setOnStreamableAssembly(@Nullable Function<? super Streamable, ? extends Streamable> onStreamableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onStreamableAssembly = onStreamableAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onConnectableFlowableAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnConnectableFlowableAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnConnectableFlowableAssembly：配置或执行 hook。 */


    public static void setOnConnectableFlowableAssembly(@Nullable Function<? super ConnectableFlowable, ? extends ConnectableFlowable> onConnectableFlowableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onConnectableFlowableAssembly = onConnectableFlowableAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onFlowableSubscribe the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnFlowableSubscribe：void 返回值工具入口。 */

    /** 静态方法 setOnFlowableSubscribe：配置或执行 hook。 */


    public static void setOnFlowableSubscribe(@Nullable BiFunction<? super Flowable, @NonNull ? super Subscriber, @NonNull ? extends Subscriber> onFlowableSubscribe) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onFlowableSubscribe = onFlowableSubscribe;
    }

    /**
     * 设置 specific hook function.
     * @param onMaybeSubscribe the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnMaybeSubscribe：void 返回值工具入口。 */

    /** 静态方法 setOnMaybeSubscribe：配置或执行 hook。 */


    public static void setOnMaybeSubscribe(@Nullable BiFunction<? super Maybe, @NonNull MaybeObserver, @NonNull ? extends MaybeObserver> onMaybeSubscribe) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onMaybeSubscribe = onMaybeSubscribe;
    }

    /**
     * 设置 specific hook function.
     * @param onObservableAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnObservableAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnObservableAssembly：配置或执行 hook。 */


    public static void setOnObservableAssembly(@Nullable Function<? super Observable, ? extends Observable> onObservableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onObservableAssembly = onObservableAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onConnectableObservableAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnConnectableObservableAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnConnectableObservableAssembly：配置或执行 hook。 */


    public static void setOnConnectableObservableAssembly(@Nullable Function<? super ConnectableObservable, ? extends ConnectableObservable> onConnectableObservableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onConnectableObservableAssembly = onConnectableObservableAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onObservableSubscribe the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnObservableSubscribe：void 返回值工具入口。 */

    /** 静态方法 setOnObservableSubscribe：配置或执行 hook。 */


    public static void setOnObservableSubscribe(
            @Nullable BiFunction<? super Observable, @NonNull ? super Observer, @NonNull ? extends Observer> onObservableSubscribe) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onObservableSubscribe = onObservableSubscribe;
    }

    /**
     * 设置 specific hook function.
     * @param onSingleAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnSingleAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnSingleAssembly：配置或执行 hook。 */


    public static void setOnSingleAssembly(@Nullable Function<? super Single, ? extends Single> onSingleAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onSingleAssembly = onSingleAssembly;
    }

    /**
     * 设置 specific hook function.
     * @param onSingleSubscribe the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnSingleSubscribe：void 返回值工具入口。 */

    /** 静态方法 setOnSingleSubscribe：配置或执行 hook。 */


    public static void setOnSingleSubscribe(@Nullable BiFunction<? super Single, @NonNull ? super SingleObserver, @NonNull ? extends SingleObserver> onSingleSubscribe) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaPlugins.onSingleSubscribe = onSingleSubscribe;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @param subscriber 订阅者
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> Subscriber<? super T> onSubscribe(@NonNull Flowable<T> source, @NonNull Subscriber<? super T> subscriber) {
        BiFunction<? super Flowable, @NonNull ? super Subscriber, @NonNull ? extends Subscriber> f = onFlowableSubscribe;
        if (f != null) {
            return apply(f, source, subscriber);
        }
        return subscriber;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @param observer Observer
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> Observer<? super T> onSubscribe(@NonNull Observable<T> source, @NonNull Observer<? super T> observer) {
        BiFunction<? super Observable, @NonNull ? super Observer, @NonNull ? extends Observer> f = onObservableSubscribe;
        if (f != null) {
            return apply(f, source, observer);
        }
        return observer;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @param observer Observer
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> SingleObserver<? super T> onSubscribe(@NonNull Single<T> source, @NonNull SingleObserver<? super T> observer) {
        BiFunction<? super Single, @NonNull ? super SingleObserver, @NonNull ? extends SingleObserver> f = onSingleSubscribe;
        if (f != null) {
            return apply(f, source, observer);
        }
        return observer;
    }

    /**
     * 调用 associated hook function.
     * @param source 上游源 hook's input value
     * @param observer Observer
     * @return  value returned by the hook
     */
    @NonNull
    /** 静态方法 onSubscribe：CompletableObserver 返回值工具入口。 */

    public static CompletableObserver onSubscribe(@NonNull Completable source, @NonNull CompletableObserver observer) {
        BiFunction<? super Completable, @NonNull ? super CompletableObserver, @NonNull ? extends CompletableObserver> f = onCompletableSubscribe;
        if (f != null) {
            return apply(f, source, observer);
        }
        return observer;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @param observer 订阅者
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> MaybeObserver<? super T> onSubscribe(@NonNull Maybe<T> source, @NonNull MaybeObserver<? super T> observer) {
        BiFunction<? super Maybe, @NonNull ? super MaybeObserver, @NonNull ? extends MaybeObserver> f = onMaybeSubscribe;
        if (f != null) {
            return apply(f, source, observer);
        }
        return observer;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @param subscribers the array of subscribers
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "unchecked" })
    @NonNull
    public static <@NonNull T> Subscriber<? super T>[] onSubscribe(@NonNull ParallelFlowable<? extends T> source, @NonNull Subscriber<? super T>[] subscribers) {
        var f = onParallelSubscribe;
        if (f != null) {
            return (@NonNull Subscriber<@NonNull ? super @NonNull T>[]) apply(f, source, subscribers);
        }
        return subscribers;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> Maybe<T> onAssembly(@NonNull Maybe<T> source) {
        Function<? super Maybe, ? extends Maybe> f = onMaybeAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> Flowable<T> onAssembly(@NonNull Flowable<T> source) {
        Function<? super Flowable, ? extends Flowable> f = onFlowableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> Streamable<T> onAssembly(@NonNull Streamable<T> source) {
        Function<? super Streamable, ? extends Streamable> f = onStreamableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> ConnectableFlowable<T> onAssembly(@NonNull ConnectableFlowable<T> source) {
        Function<? super ConnectableFlowable, ? extends ConnectableFlowable> f = onConnectableFlowableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> Observable<T> onAssembly(@NonNull Observable<T> source) {
        Function<? super Observable, ? extends Observable> f = onObservableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> ConnectableObservable<T> onAssembly(@NonNull ConnectableObservable<T> source) {
        Function<? super ConnectableObservable, ? extends ConnectableObservable> f = onConnectableObservableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 调用 associated hook function.
     * @param <T> 值 type
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> Single<T> onAssembly(@NonNull Single<T> source) {
        Function<? super Single, ? extends Single> f = onSingleAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 调用 associated hook function.
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     */
    @NonNull
    /** 静态方法 onAssembly：Completable 返回值工具入口。 */

    public static Completable onAssembly(@NonNull Completable source) {
        Function<? super Completable, ? extends Completable> f = onCompletableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 设置 specific hook function.
     * <p>History: 2.0.6 - experimental; 2.1 - beta
     * @param handler the hook function to set, null allowed
     * @since 2.2
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnParallelAssembly：void 返回值工具入口。 */

    /** 静态方法 setOnParallelAssembly：配置或执行 hook。 */


    public static void setOnParallelAssembly(@Nullable Function<? super ParallelFlowable, ? extends ParallelFlowable> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onParallelAssembly = handler;
    }

    /**
     * 返回当前 hook function.
     * <p>History: 2.0.6 - experimental; 2.1 - beta
     * @return hook 函数，可能为 null
     * @since 2.2
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public static Function<? super ParallelFlowable, ? extends ParallelFlowable> getOnParallelAssembly() {
        return onParallelAssembly;
    }

    /**
     * 设置 specific hook function.
     * <p>History: 3.0.11 - experimental
     * @param handler the hook function to set, null allowed
     * @since 3.1.0
     */
    @SuppressWarnings("rawtypes")
    /** 静态方法 setOnParallelSubscribe：void 返回值工具入口。 */

    /** 静态方法 setOnParallelSubscribe：配置或执行 hook。 */


    public static void setOnParallelSubscribe(@Nullable BiFunction<? super ParallelFlowable, @NonNull ? super Subscriber<@NonNull ?>[],
            @NonNull ? extends Subscriber<@NonNull ?>[]> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onParallelSubscribe = handler;
    }

    /**
     * 返回当前 hook function.
     * <p>History: 3.0.11 - experimental
     * @return hook 函数，可能为 null
     * @since 3.1.0
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public static BiFunction<? super ParallelFlowable, @NonNull ? super Subscriber<@NonNull ?>[], @NonNull ? extends Subscriber<@NonNull ?>[]> getOnParallelSubscribe() {
        return onParallelSubscribe;
    }

    /**
     * 调用 associated hook function.
     * <p>History: 2.0.6 - experimental; 2.1 - beta
     * @param <T> 值 type of the source
     * @param source 上游源 hook's input value
     * @return  value returned by the hook
     * @since 2.2
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <@NonNull T> ParallelFlowable<T> onAssembly(@NonNull ParallelFlowable<T> source) {
        Function<? super ParallelFlowable, ? extends ParallelFlowable> f = onParallelAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
 * 【说明】Called before an operator attempts a blocking operation；such as awaiting a co...
     * Called before an operator attempts a blocking operation
     * such as awaiting a condition or signal
     * and should return true to indicate the operator
     * should not block but throw an IllegalArgumentException.
     * <p>History: 2.0.5 - experimental
     * @return true if the blocking should be prevented
     * @see #setFailOnNonBlockingScheduler(boolean)
     * @since 2.1
     */
    /** 静态方法 onBeforeBlocking：boolean 返回值工具入口。 */

    /** 静态方法 onBeforeBlocking：返回插件/配置布尔状态。 */


    public static boolean onBeforeBlocking() {
        BooleanSupplier f = onBeforeBlocking;
        if (f != null) {
            try {
                return f.getAsBoolean();
            } catch (Throwable ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        return false;
    }

    /**
 * 【说明】Set the handler that is called when an operator attempts a blocking；await; th...
     * Set the handler that is called when an operator attempts a blocking
     * await; the handler should return true to prevent the blocking
     * and to signal an IllegalStateException instead.
     * <p>History: 2.0.5 - experimental
     * @param handler the handler to set, null resets to the default handler
     * that always returns false
     * @see #onBeforeBlocking()
     * @since 2.1
     */
    /** 静态方法 setOnBeforeBlocking：void 返回值工具入口。 */

    /** 静态方法 setOnBeforeBlocking：配置或执行 hook。 */


    public static void setOnBeforeBlocking(@Nullable BooleanSupplier handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onBeforeBlocking = handler;
    }

    /**
     * 返回当前 blocking handler or null if no custom handler
     * is set.
     * <p>History: 2.0.5 - experimental
     * @return  current blocking handler or null if not specified
     * @since 2.1
     */
    @Nullable
    /** 静态方法 getOnBeforeBlocking：BooleanSupplier 返回值工具入口。 */

    public static BooleanSupplier getOnBeforeBlocking() {
        return onBeforeBlocking;
    }

    /**
 * 【说明】Create an instance of the default {@link Scheduler} used for {@link Scheduler...
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#computation()}
     * except using {@code threadFactory} for thread creation.
     * <p>History: 2.0.5 - experimental
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return  created Scheduler instance
     * @since 2.1
     */
    @NonNull
    /** 静态方法 createComputationScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler createComputationScheduler(@NonNull ThreadFactory threadFactory) {
        return new ComputationScheduler(Objects.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
 * 【说明】Create an instance of the default {@link Scheduler} used for {@link Scheduler...
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#cached()}
     * except using {@code threadFactory} for thread creation.
     * <p>History: 2.0.5 - experimental
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return  created Scheduler instance
     * @since 4.0.0
     */
    @NonNull
    /** 静态方法 createCachedScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler createCachedScheduler(@NonNull ThreadFactory threadFactory) {
        return new CachedScheduler(Objects.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
 * 【说明】Create an instance of the default {@link Scheduler} used for {@link Scheduler...
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#newThread()}
     * except using {@code threadFactory} for thread creation.
     * <p>History: 2.0.5 - experimental
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return  created Scheduler instance
     * @since 2.1
     */
    @NonNull
    /** 静态方法 createNewThreadScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler createNewThreadScheduler(@NonNull ThreadFactory threadFactory) {
        return new NewThreadScheduler(Objects.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
 * 【说明】Create an instance of the default {@link Scheduler} used for {@link Scheduler...
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#single()}
     * except using {@code threadFactory} for thread creation.
     * <p>History: 2.0.5 - experimental
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return  created Scheduler instance
     * @since 2.1
     */
    @NonNull
    /** 静态方法 createSingleScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler createSingleScheduler(@NonNull ThreadFactory threadFactory) {
        return new SingleScheduler(Objects.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
 * 【说明】Create an instance of a {@link Scheduler} by wrapping an existing {@link Exec...
     * Create an instance of a {@link Scheduler} by wrapping an existing {@link Executor}.
     * <p>
     * This method allows creating an {@code Executor}-backed {@code Scheduler} before the {@link Schedulers} class
     * would initialize the standard {@code Scheduler}s.
     *
     * @param executor the {@code Executor} to wrap and turn into a {@code Scheduler}.
     * @param interruptibleWorker if {@code true}, the tasks submitted to the {@link io.reactivex.rxjava4.core.Scheduler.Worker Scheduler.Worker} will
     * be interrupted when the task is disposed.
     * @param fair if {@code true}, tasks submitted to the {@code Scheduler} or {@code Worker} will be executed by the underlying {@code Executor} one after the other, still
     * in a FIFO and non-overlapping manner, but allows interleaving with other tasks submitted to the underlying {@code Executor}.
     * If {@code false}, the underlying FIFO scheme will execute as many tasks as it can before giving up the underlying {@code Executor} thread.
     * @return  new {@code Scheduler} wrapping the {@code Executor}
     * @since 3.1.0
     */
    @NonNull
    /** 静态方法 createExecutorScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler createExecutorScheduler(@NonNull Executor executor, boolean interruptibleWorker, boolean fair) {
        return new ExecutorScheduler(executor, interruptibleWorker, fair);
    }

    /**
 * 【说明】Create an instance of a {@link Scheduler} by wrapping a supplier of a {@link ...
     * Create an instance of a {@link Scheduler} by wrapping a supplier of a {@link Executor}.
     * <p>
     * This method allows creating a deferred {@code Executor}-backed {@code Scheduler} before the {@link Schedulers} class
     * would initialize the standard {@code Scheduler}s.
     *
     * @param executorSupplier the {@code Executor} supplier to wrap and turn into a {@code Scheduler}.
     * @param interruptibleWorker if {@code true}, the tasks submitted to the {@link io.reactivex.rxjava4.core.Scheduler.Worker Scheduler.Worker} will
     * be interrupted when the task is disposed.
     * @param fair if {@code true}, tasks submitted to the {@code Scheduler} or {@code Worker} will be executed by the underlying {@code Executor} one after the other, still
     * in a FIFO and non-overlapping manner, but allows interleaving with other tasks submitted to the underlying {@code Executor}.
     * If {@code false}, the underlying FIFO scheme will execute as many tasks as it can before giving up the underlying {@code Executor} thread.
     * @return  new {@code Scheduler} wrapping the {@code Executor}
     * @since 4.0.0
     */
    @NonNull
    /** 静态方法 createDeferredExecutorScheduler：Scheduler 返回值工具入口。 */

    public static Scheduler createDeferredExecutorScheduler(@NonNull Supplier<? extends Executor> executorSupplier, boolean interruptibleWorker, boolean fair) {
        return new DeferredExecutorScheduler(executorSupplier, interruptibleWorker, fair);
    }

    /**
     * 包装 call to 函数 in try-catch and propagates thrown
     * checked exceptions as RuntimeException.
     * @param <T> the input type
     * @param <R> the output type
     * @param f 函数 to call, not null (not verified)
     * @param t the parameter value to 函数
     * @return  result of 函数 call
     */
    @NonNull
    static <@NonNull T, @NonNull R> R apply(@NonNull Function<T, R> f, @NonNull T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * 包装 call to 函数 in try-catch and propagates thrown
     * checked exceptions as RuntimeException.
     * @param <T> the first input type
     * @param <U> the second input type
     * @param <R> the output type
     * @param f 函数 to call, not null (not verified)
     * @param t the first parameter value to 函数
     * @param u the second parameter value to 函数
     * @return  result of 函数 call
     */
    @NonNull
    static <@NonNull T, @NonNull U, @NonNull R> R apply(@NonNull BiFunction<? super T, ? super U, ? extends R> f, @NonNull T t, @NonNull U u) {
        try {
            return f.apply(t, u);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * 包装 call to the Scheduler creation supplier in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     * @param s the {@link Supplier} which returns a {@link Scheduler}, not null (not verified). Cannot return null
     * @return  result of the supplier call, not null
     * @throws NullPointerException 若参数为 null the supplier parameter returns null
     */
    @NonNull
    static Scheduler callRequireNonNull(@NonNull Supplier<Scheduler> s) {
        try {
            return Objects.requireNonNull(s.get(), "Scheduler Supplier result can't be null");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * 包装 call to the Scheduler creation function in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     * @param f 函数 to call, not null (not verified). Cannot return null
     * @param s the parameter value to 函数
     * @return  result of 函数 call, not null
     * @throws NullPointerException 若参数为 null 函数 parameter returns null
     */
    @NonNull
    static Scheduler applyRequireNonNull(@NonNull Function<? super Supplier<Scheduler>, ? extends Scheduler> f, Supplier<Scheduler> s) {
        return Objects.requireNonNull(apply(f, s), "Scheduler Supplier result can't be null");
    }

    /** Helper class, no instances. */
    private RxJavaPlugins() {
        throw new IllegalStateException("No instances!");
    }
}
