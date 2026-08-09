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
package org.redisson.command;

import java.util.concurrent.CompletableFuture;

/**
 * 批量（Pipeline/事务）场景下的 {@link CompletableFuture} 扩展。
 * <p>除命令结果 Future 外，还提供 {@link #getSentPromise()} 表示命令
 * 是否已成功写入连接（Redis 队列模式下的发送完成信号）。
 * <p>批量模式下 {@link #cancel} 恒返回 {@code false}，避免误取消整批命令。
 *
 * @author Nikita Koksharov
 *
 */
public class BatchPromise<T> extends CompletableFuture<T> {

    /** 命令已发送到 Redis 连接时 complete 的 Future。 */
    private final CompletableFuture<Void> sentPromise = new CompletableFuture<>();
    
    /** 创建未完成的批量 Promise。 */
    public BatchPromise() {
        super();
    }
    
    /** 返回“命令已发送”Future，用于 Redis 原子队列模式的协调。 */
    public CompletableFuture<Void> getSentPromise() {
        return sentPromise;
    }
    
    /** 批量模式下不允许取消单条命令。 */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

}
