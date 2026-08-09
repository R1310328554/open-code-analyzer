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
package org.redisson.executor;

import java.util.concurrent.*;

import org.redisson.api.RFuture;
import org.redisson.api.RScheduledExecutorService;

/**
 * 基于 {@link RScheduledExecutorService} 的 {@link CompletionService} 实现。
 * <p>
 * 任务提交后，完成时（无论成功或失败）将对应 {@link RFuture} 放入完成队列，
 * 可通过 {@code take}/{@code poll} 按完成顺序获取。
 *  
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class RedissonCompletionService<V> implements CompletionService<V> {

    /** 底层远程调度执行器服务。 */
    protected final RScheduledExecutorService executorService;

    /** 已完成任务的 Future 队列。 */
    protected final BlockingQueue<RFuture<V>> completionQueue;
    
    /** 使用默认 {@link LinkedBlockingQueue} 构造。 */
    public RedissonCompletionService(RScheduledExecutorService executorService) {
        this(executorService, null);
    }

    /** 使用自定义完成队列构造。 */
    public RedissonCompletionService(RScheduledExecutorService executorService, BlockingQueue<RFuture<V>> completionQueue) {
        if (executorService == null) {
            throw new NullPointerException("executorService can't be null");
        }
        
        this.executorService = executorService;
        if (completionQueue == null) {
            completionQueue = new LinkedBlockingQueue<RFuture<V>>();
        }
        
        this.completionQueue = completionQueue;
    }

    /** 提交 Callable，完成时入队。 */
    @Override
    public Future<V> submit(Callable<V> task) {
        if (task == null) {
            throw new NullPointerException("taks can't be null");
        }
        
        RFuture<V> f = executorService.submit(task);
        // 无论成功失败，完成回调中将 Future 放入完成队列
        f.whenComplete((res, e) -> {
            completionQueue.add(f);
        });
        return f;
    }

    /** 提交 Runnable 并指定结果值，完成时入队。 */
    @Override
    public Future<V> submit(Runnable task, V result) {
        if (task == null) {
            throw new NullPointerException("taks can't be null");
        }
        
        RFuture<V> f = executorService.submit(task, result);
        f.whenComplete((res, e) -> {
            completionQueue.add(f);
        });
        return f;
    }

    /** 阻塞直到有已完成任务。 */
    @Override
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    /** 非阻塞取队首已完成任务，无则返回 null。 */
    @Override
    public Future<V> poll() {
        return completionQueue.poll();
    }

    /** 限时等待已完成任务。 */
    @Override
    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

}
