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
package org.redisson.mapreduce;

import org.redisson.api.RExecutorService;
import org.redisson.api.RFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MapReduce 子任务批量提交与等待工具。
 * <p>
 * 将多个 Runnable 异步提交到 {@link org.redisson.api.RExecutorService}，
 * 使用 {@link CompletableFuture#allOf} 等待全部完成，支持整体超时与中断取消。
 *
 * @author Nikita Koksharov
 *
 */
public class SubTasksExecutor {

    /** 已提交子任务的 CompletableFuture 列表。 */
    private final List<CompletableFuture<?>> futures = new ArrayList<>();
    /** MapReduce 远程执行器。 */
    private final RExecutorService executor;
    /** 协调任务开始时间，用于计算剩余超时。 */
    private final long startTime;
    /** 整体超时（毫秒）。 */
    private final long timeout;

    public SubTasksExecutor(RExecutorService executor, long startTime, long timeout) {
        this.executor = executor;
        this.startTime = startTime;
        this.timeout = timeout;
    }
    
    /** 异步提交一个子任务并记录 Future。 */
    public void submit(Runnable runnable) {
        RFuture<?> future = executor.submitAsync(runnable);
        futures.add(future.toCompletableFuture());
    }
    
    /** 取消所有未完成的子任务 Future。 */
    private void cancel(List<CompletableFuture<?>> futures) {
        for (CompletableFuture<?> future : futures) {
            future.cancel(true);
        }
    }
    
    private boolean isTimeoutExpired(long timeSpent) {
        return timeSpent > timeout && timeout > 0;
    }
    
    /**
     * 等待所有子任务完成。超时或中断返回 false 并取消任务；
     * timeout==0 时无限等待，ExecutionException 向上抛出。
     */
    public boolean await() throws Exception {
        if (Thread.currentThread().isInterrupted()) {
            cancel(futures);
            return false;
        }
        
        long timeSpent = System.currentTimeMillis() - startTime;
        if (isTimeoutExpired(timeSpent)) {
            cancel(futures);
            throw new MapReduceTimeoutException();
        }

        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            if (timeout > 0) {
                try {
                    future.get(timeout - timeSpent, TimeUnit.MILLISECONDS);
                } catch (ExecutionException e) {
                    // skip
                } catch (TimeoutException e) {
                    cancel(futures);
                    throw new MapReduceTimeoutException();
                }
            } else if (timeout == 0) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw (Exception) e.getCause();
                }
            }
        } catch (InterruptedException e) {
            cancel(futures);
            return false;
        }
        return true;
    }
    
}
