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

import org.redisson.api.RExecutorBatchFuture;
import org.redisson.api.RExecutorFuture;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 批量远程执行任务的整体 Future，包装 {@link CompletableFuture} 并持有各子任务 Future 列表。
 * <p>
 * 实现 {@link RExecutorBatchFuture}，可通过 {@link #getTaskFutures()} 访问单个任务状态。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonExecutorBatchFuture extends CompletableFutureWrapper<Void> implements RExecutorBatchFuture {

    /** 批量中各子任务的 Future 列表。 */
    private final List<RExecutorFuture<?>> futures;

    /** 以整体完成 Future 与子任务列表构造。 */
    public RedissonExecutorBatchFuture(CompletableFuture<Void> future, List<RExecutorFuture<?>> futures) {
        super(future);
        this.futures = futures;
    }
    
    /** 返回批量中各子任务的 Future。 */
    @Override
    public List<RExecutorFuture<?>> getTaskFutures() {
        return futures;
    }
    
}
