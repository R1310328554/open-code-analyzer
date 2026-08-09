/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.util;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CompletableFuture 辅助工具，提供失败 Future 构造、批量等待与超时取值。
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public final class AsyncUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncUtils.class);

    /** 构造已异常完成的 Future。 */
    public static <R> CompletableFuture<R> newFailedFuture(Throwable ex) {
        CompletableFuture<R> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

    /** 等待全部 Future 完成后收集非 null 结果（allOf 语义）。 */
    public static <R> CompletableFuture<List<R>> sequenceFuture(List<CompletableFuture<R>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(AsyncUtils::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            );
    }

    /** 并行等待各 Future 并收集非 null 结果，单个失败不影响其余。 */
    public static <R> CompletableFuture<List<R>> sequenceSuccessFuture(List<CompletableFuture<R>> futures) {
        return CompletableFuture.supplyAsync(() -> futures.parallelStream()
            .map(AsyncUtils::getValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );
    }

    /** 带 10 秒超时的阻塞取值，异常时记录日志并返回 null。 */
    public static <T> T getValue(CompletableFuture<T> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            LOG.error("getValue for async result failed", ex);
        }
        return null;
    }

    /** 判断 Future 是否已成功完成（非异常、非取消）。 */
    public static boolean isSuccessFuture(CompletableFuture future) {
        return future.isDone() && !future.isCompletedExceptionally() && !future.isCancelled();
    }

    private AsyncUtils() {}
}
