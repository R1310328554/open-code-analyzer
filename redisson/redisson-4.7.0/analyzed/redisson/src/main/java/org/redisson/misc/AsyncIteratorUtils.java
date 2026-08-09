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
import java.util.function.Function;

/**
 * 异步顺序迭代工具：对 Iterator 中每个元素依次应用异步 processor，
 * 采用蹦床模式避免大集合迭代时栈溢出。
 *
 * @author Konstantin Subbotin
 */
public final class AsyncIteratorUtils {

    private AsyncIteratorUtils() {
    }

    /**
     * 顺序处理迭代器每个元素：processor 返回 CompletionStage，
     * 全部完成后 result 以 Void 完成。
     *
     * @param <T> the type of elements in the iterator
     * @param iter the iterator to process
     * @param processor the async function to apply to each element
     * @return a CompletionStage that completes when all elements have been processed
     */
    public static <T> CompletionStage<Void> forEachAsync(Iterator<T> iter,
                                                          Function<T, CompletionStage<Void>> processor) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        processNext(iter, processor, result);
        return result;
    }

    private static <T> void processNext(Iterator<T> iter,
                                         Function<T, CompletionStage<Void>> processor,
                                         CompletableFuture<Void> result) {
        // 同步完成路径在同栈循环中推进
        while (true) {
            if (!iter.hasNext()) {
                result.complete(null);
                return;
            }

            T element = iter.next();
            CompletionStage<Void> stage = processor.apply(element);
            CompletableFuture<Void> cf = stage.toCompletableFuture();

            // 已同步完成则直接处理下一元素
            if (cf.isDone()) {
                if (cf.isCompletedExceptionally()) {
                    try {
                        cf.join();
                    } catch (CompletionException e) {
                        Throwable cause = e.getCause();
                        if (cause == null) {
                            cause = e;
                        }
                        result.completeExceptionally(cause);
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                    return;
                }
                continue; // 同栈帧继续
            }

            // 异步路径：注册回调后返回
            cf.whenComplete((r, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(unwrap(ex));
                    return;
                }
                processNext(iter, processor, result); // 蹦床
            });
            return;
        }
    }

    private static Throwable unwrap(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }
}
