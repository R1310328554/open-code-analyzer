/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * {@link CompletableFuture} 链式组合工具：在指定线程池上衔接后续 Future。
 */
public class FutureUtils {

    /** 当前 Future 完成后，将结果或异常传递给 nextFuture。 */
    public static <T> CompletableFuture<T> appendNextFuture(CompletableFuture<T> future,
        CompletableFuture<T> nextFuture, ExecutorService executor) {
        future.whenCompleteAsync((t, throwable) -> {
            if (throwable != null) {
                nextFuture.completeExceptionally(throwable);
            } else {
                nextFuture.complete(t);
            }
        }, executor);
        return nextFuture;
    }

    /** 为 Future 追加一个在 executor 上执行的后续阶段。 */
    public static <T> CompletableFuture<T> addExecutor(CompletableFuture<T> future, ExecutorService executor) {
        return appendNextFuture(future, new CompletableFuture<>(), executor);
    }

    /** 创建并以给定异常完成的 Future。 */
    public static <T> CompletableFuture<T> completeExceptionally(Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }
}
