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

import org.redisson.api.RExecutorFuture;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * 单个远程执行任务的 Future，包装底层 {@link CompletableFuture} 并暴露任务 ID。
 * <p>
 * 实现 {@link RExecutorFuture}，供客户端跟踪 Redis 中登记的任务。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class RedissonExecutorFuture<V> extends CompletableFutureWrapper<V> implements RExecutorFuture<V> {

    /** Redis 中登记的任务请求 ID。 */
    private final String taskId;
    
    /** 从 {@link RemotePromise} 构造，taskId 取自 requestId。 */
    public RedissonExecutorFuture(RemotePromise<V> promise) {
        this(promise, promise.getRequestId());
    }
    
    /** 指定底层 Future 与 taskId 构造。 */
    public RedissonExecutorFuture(CompletableFuture<V> promise, String taskId) {
        super(promise);
        this.taskId = taskId;
    }

    /** 返回远程任务 ID。 */
    @Override
    public String getTaskId() {
        return taskId;
    }

}
