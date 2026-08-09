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

package io.reactivex.rxjava4.core;

import java.io.Serial;
import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 由终端 stage 与 disposable 组成，用于取消序列。
 * @param <T> 各 stage 的返回与元素类型
 * @since 4.0.0
 */
public final class CompletionStageDisposable<T> implements AutoCloseable {

    // record classes can't have extra fields, why?
    // also I have to write out the constructor instead of declaring it in the record definition, FFS

    static final Cleaner cleaner = Cleaner.create();

    static volatile Consumer<Cleaner.Cleanable> trackAllocations;

    static final class State extends AtomicBoolean implements Runnable {

        /** */
        @Serial
        private static final long serialVersionUID = 262854674341831347L;

        Throwable allocationTrace;

        @Override
        public void run() {
            if (!get()) {
                RxJavaPlugins.onError(
                        new IllegalStateException("CompletionStageDisposable was not awaited or ignored explicitly",
                                allocationTrace));
            }
        }

    }

    final CompletionStage<T> stage;
    final Disposable disposable;
    final State state;
    final Cleaner.Cleanable cleanable;

    /**
     * 使用参数构造实例。
     * @param stage 待 await 的 stage
     * @param disposable 用于异步取消的 disposable
     */
    public CompletionStageDisposable(@NonNull CompletionStage<T> stage, @NonNull Disposable disposable) {
        Objects.requireNonNull(stage, "stage is null");
        Objects.requireNonNull(disposable, "disposable is null");
        this.stage = stage;
        this.disposable = disposable;
        this.state = new State();
        this.cleanable = cleaner.register(this, state);
        if (trackAllocations != null) {
            state.allocationTrace = new StackOverflowError("CompletionStageDisposable::AllocationTrace");
            trackAllocations.accept(this.cleanable);
        } else {
            state.allocationTrace = null;
        }
    }
    /**
     * 等待当前 stage 完成。
     * <p>
     * 原样重新抛出原始 unchecked 异常。
     * @throws CancellationException 若计算被取消
     * @throws CompletionException 若原始异常为 checked 异常
     */
    public void await() {
        state.lazySet(true);
        try {
            stage.toCompletableFuture().join();
        } catch (CompletionException ce) {
            throw ExceptionHelper.unwrapOrThrow(ce);
        }
    }

    /**
     * 表明本实例故意不 await 其 stage。
     */
    public void ignore() {
        state.lazySet(true);
    }

    @Override
    public void close() {
        try {
            state.lazySet(true);
            disposable.dispose();
        } finally {
            cleanable.clean();
        }
    }

    /**
     * 设置分配追踪回调，用于追踪 CompletionStageDisposable 泄漏位置。
     * @param callback 建立新追踪时调用的回调
     */
    public static void setAllocationTrace(Consumer<Cleaner.Cleanable> callback) {
        trackAllocations = callback;
    }

    /**
     * 返回当前捕获分配堆栈的 consumer。
     * @return 当前捕获分配堆栈的 consumer
     */
    public static Consumer<Cleaner.Cleanable> getAllocationTrace() {
        return trackAllocations;
    }

    /***
     * 返回关联的 completion stage 值。
     * @return 关联的 completion stage 值
     */
    public CompletionStage<T> stage() {
        return stage;
    }

    /**
     * 返回关联的 disposable 值。
     * @return 关联的 disposable 值
     */
    public Disposable disposable() {
        return disposable;
    }
}
