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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.CompletableFuture;

/**
 * {@link RExecutorFuture} 的软引用包装，用于任务 Future 缓存与内存回收。
 * <p>
 * 同时持有底层 {@link CompletableFuture} 与 requestId，
 * 在引用队列中被清理时可取消未完成 promise。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonExecutorFutureReference extends SoftReference<RExecutorFuture<?>> {

    /** 关联的底层 CompletableFuture。 */
    private final CompletableFuture<?> promise;
    /** 远程任务请求 ID。 */
    private final String requestId;
    
    /** 构造软引用，绑定 referent、引用队列与 promise。 */
    public RedissonExecutorFutureReference(String requestId, RExecutorFuture<?> referent, ReferenceQueue<? super RExecutorFuture<?>> q, CompletableFuture<?> promise) {
        super(referent, q);
        this.requestId = requestId;
        this.promise = promise;
    }
    
    /** 返回底层 promise。 */
    public CompletableFuture<?> getPromise() {
        return promise;
    }
    
    /** 返回任务 requestId。 */
    public String getRequestId() {
        return requestId;
    }

}
