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
package org.redisson.reactive;

import org.redisson.api.BatchOptions;
import org.redisson.api.BatchResult;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.command.BatchService;
import org.redisson.command.CommandBatchService;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.NodeSource;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.misc.CompletableFutureWrapper;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 响应式批处理命令服务：组合 {@link CommandBatchService} 与 {@link CommandReactiveService}，
 * 将多条 Redis 命令攒批后通过 {@link #executeAsync()} 一次性提交。
 * {@link #reactive} 会立即 subscribe 以触发批内命令入队。
 *
 * @author Nikita Koksharov
 *
 */
public class CommandReactiveBatchService extends CommandReactiveService implements BatchService {

    /** 底层异步批处理实现。 */
    private final CommandBatchService batchService;

    /** 创建 REACTIVE 引用类型的批处理服务。 */
    public CommandReactiveBatchService(ConnectionManager connectionManager, CommandReactiveExecutor commandExecutor, BatchOptions options) {
        super(connectionManager, commandExecutor.getObjectBuilder());
        batchService = new CommandBatchService(commandExecutor, options, RedissonObjectBuilder.ReferenceType.REACTIVE);
    }

    /**
     * 包装 supplier：首次 call 时 transfer 到共享 CompletableFuture，
     * 并 subscribe Mono 以驱动批命令注册。
     */
    @Override
    public <R> Mono<R> reactive(Callable<CompletionStage<R>> supplier) {
        Mono<R> mono = super.reactive(new Callable<CompletionStage<R>>() {
            final CompletableFuture<R> future = new CompletableFuture<>();
            final AtomicBoolean lock = new AtomicBoolean();
            @Override
            public RFuture<R> call() throws Exception {
                // 批内每条 reactive 调用仅执行一次 supplier
                if (lock.compareAndSet(false, true)) {
                    transfer(supplier.call().toCompletableFuture(), future);
                }
                return new CompletableFutureWrapper<>(future);
            }
        });
        mono.subscribe();
        return mono;
    }
    
    @Override
    protected <R> CompletableFuture<R> createPromise() {
        return batchService.createPromise();
    }
    
    @Override
    public <V, R> RFuture<R> async(boolean readOnlyMode, NodeSource nodeSource,
                                        Codec codec, RedisCommand<V> command, Object[] params, boolean ignoreRedirect, boolean noRetry) {
        return batchService.async(readOnlyMode, nodeSource, codec, command, params, ignoreRedirect, noRetry);
    }

    /** 提交批处理并返回聚合结果 Future。 */
    public RFuture<BatchResult<?>> executeAsync() {
        return batchService.executeAsync();
    }

    /** 批模式禁用 eval 脚本缓存。 */
    @Override
    protected boolean isEvalCacheActive() {
        return false;
    }

    /** 丢弃未执行的批命令。 */
    public RFuture<Void> discardAsync() {
        return batchService.discardAsync();
    }
}
