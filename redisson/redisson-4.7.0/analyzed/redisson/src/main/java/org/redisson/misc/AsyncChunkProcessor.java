/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.misc;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 异步分块迭代工具：以蹦床（trampoline）模式处理 Iterator 分块，
 * 避免大迭代器上递归回调导致 {@link StackOverflowError}。
 * <p>
 * 同步完成的 chunk 在同栈循环中继续；异步完成则注册 whenComplete 再递归 processNext。
 *
 * @author Konstantin Subbotin
 */
public final class AsyncChunkProcessor {

    private AsyncChunkProcessor() {
    }

    /**
     * 单次分块执行上下文：异步 Future 与成功回调。
     * chunkHandler 返回 null 表示迭代结束。
     *
     * @param <R> the result type of the async operation
     */
    public static final class ChunkExecution<R> {
        private final CompletionStage<R> future;
        private final Consumer<R> onSuccess;

        public ChunkExecution(CompletionStage<R> future, Consumer<R> onSuccess) {
            this.future = future;
            this.onSuccess = onSuccess;
        }

        public CompletionStage<R> future() {
            return future;
        }

        public Consumer<R> onSuccess() {
            return onSuccess;
        }
    }

    /**
     * 按 chunkHandler 驱动迭代器分块处理，直至 handler 返回 null 或迭代耗尽。
     * 栈安全：同步/异步完成均不增长调用栈深度。
     *
     * @param <R> the result type of each chunk operation
     * @param iter the iterator to process
     * @param chunkSize the chunk size hint passed to handler
     * @param chunkHandler builds and executes a chunk, returns null when done
     * @return a CompletionStage that completes when all chunks have been processed
     */
    public static <R> CompletionStage<Void> processAll(
            Iterator<String> iter,
            int chunkSize,
            BiFunction<Iterator<String>, Integer, ChunkExecution<R>> chunkHandler) {

        CompletableFuture<Void> result = new CompletableFuture<>();
        processNext(iter, chunkSize, chunkHandler, result);
        return result;
    }

    private static <R> void processNext(
            Iterator<String> iter,
            int chunkSize,
            BiFunction<Iterator<String>, Integer, ChunkExecution<R>> chunkHandler,
            CompletableFuture<Void> result) {

        // 循环处理同步完成的 chunk，避免栈增长
        while (true) {
            ChunkExecution<R> execution = chunkHandler.apply(iter, chunkSize);

            // null 表示所有分块已处理完毕
            if (execution == null) {
                result.complete(null);
                return;
            }

            CompletableFuture<R> cf = execution.future().toCompletableFuture();

            // 同步完成：在当前栈帧继续下一 chunk
            if (cf.isDone()) {
                if (cf.isCompletedExceptionally()) {
                    propagateException(cf, result);
                    return;
                }
                execution.onSuccess().accept(cf.join());
                continue; // 同栈帧处理下一 chunk
            }

            // 异步完成：注册回调后返回，打断栈链
            cf.whenComplete((r, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(unwrap(ex));
                    return;
                }
                execution.onSuccess().accept(r);
                processNext(iter, chunkSize, chunkHandler, result); // 蹦床递归
            });
            return;
        }
    }

    private static void propagateException(CompletableFuture<?> cf, CompletableFuture<Void> result) {
        try {
            cf.join();
        } catch (CompletionException e) {
            result.completeExceptionally(unwrap(e));
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
    }

    private static Throwable unwrap(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }
}
