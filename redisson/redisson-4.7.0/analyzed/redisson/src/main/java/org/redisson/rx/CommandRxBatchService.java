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
package org.redisson.rx;

import io.reactivex.rxjava3.core.Flowable;
import org.redisson.api.BatchOptions;
import org.redisson.api.BatchResult;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.command.BatchService;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.NodeSource;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RxJava 批量命令服务：在 {@link CommandRxService} 之上委托 {@link CommandBatchService}。
 * <p>
 * {@link #flowable} 对 supplier 做单次触发并立即 subscribe，使批量命令在订阅时入队；
 * {@link #executeAsync}/{@link #discardAsync} 走底层 batch 执行/丢弃。
 *
 * @author Nikita Koksharov
 *
 */
public class CommandRxBatchService extends CommandRxService implements BatchService {

    /** 底层异步批量命令实现，ReferenceType 为 RXJAVA。 */
    private final CommandBatchService batchService;

    /** 包装 executor 为 CommandBatchService，并继承 CommandRxService 的 Rx 能力。 */
    CommandRxBatchService(ConnectionManager connectionManager, CommandAsyncExecutor executor, BatchOptions options) {
        super(connectionManager, executor.getObjectBuilder());
        batchService = new CommandBatchService(executor, options, RedissonObjectBuilder.ReferenceType.RXJAVA);
    }
    
    /** 批量模式下 flowable：AtomicBoolean 保证 supplier 仅调用一次，并 eager subscribe 触发入队。 */
    @Override
    public <R> Flowable<R> flowable(Callable<CompletionStage<R>> supplier) {
        Flowable<R> flowable = super.flowable(new Callable<CompletionStage<R>>() {
            final CompletableFuture<R> future = new CompletableFuture<>();
            final AtomicBoolean lock = new AtomicBoolean();
            @Override
            public RFuture<R> call() throws Exception {
                // 仅首个订阅路径执行 supplier，结果 transfer 到共享 CompletableFuture
                if (lock.compareAndSet(false, true)) {
                    transfer(supplier.call().toCompletableFuture(), future);
                }
                return new CompletableFutureWrapper<>(future);
            }
        });
        flowable.subscribe();
        return flowable;
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

    /** 提交并执行已入队的批量命令，返回各命令结果。 */
    public RFuture<BatchResult<?>> executeAsync() {
        return batchService.executeAsync();
    }

    @Override
    protected boolean isEvalCacheActive() {
        return false;
    }

    /** 丢弃尚未执行的批量命令队列。 */
    public RFuture<Void> discardAsync() {
        return batchService.discardAsync();
    }
}
