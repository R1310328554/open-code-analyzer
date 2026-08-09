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

import java.util.concurrent.CompletableFuture;

/**
 * 远程执行器任务结果的 {@link CompletableFuture} 扩展。
 * <p>
 * 绑定任务 {@code requestId}，并关联任务入队（add）阶段的 Future，
 * 便于取消流程在队列侧与执行侧协同。
 *
 * @author Nikita Koksharov
 *
 */
public class RemotePromise<T> extends CompletableFuture<T> {

    /** 远程任务的唯一请求标识。 */
    private final String requestId;
    /** 任务写入 Redis 队列时的异步结果 Future。 */
    private CompletableFuture<Boolean> addFuture;
    
    /** @param requestId 远程任务请求 ID */
    public RemotePromise(String requestId) {
        super();
        this.requestId = requestId;
    }
    
    /** 返回绑定的任务请求 ID。 */
    public String getRequestId() {
        return requestId;
    }
    
    /** 关联任务入队阶段的 Future（由 {@link TasksService} 设置）。 */
    public void setAddFuture(CompletableFuture<Boolean> addFuture) {
        this.addFuture = addFuture;
    }
    /** 返回任务入队 Future，可能为 null。 */
    public CompletableFuture<Boolean> getAddFuture() {
        return addFuture;
    }
    
    /** 直接调用父类 cancel，绕过异步取消钩子。 */
    public void doCancel(boolean mayInterruptIfRunning) {
        super.cancel(mayInterruptIfRunning);
    }

    /** 异步取消占位实现，默认返回 {@code false}。 */
    public CompletableFuture<Boolean> cancelAsync(boolean mayInterruptIfRunning) {
        return CompletableFuture.completedFuture(false);
    }

}
