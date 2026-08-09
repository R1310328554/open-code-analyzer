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

import org.redisson.api.options.ObjectParams;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandAsyncService;
import org.redisson.connection.ConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * {@link CommandReactiveExecutor} 默认实现：继承 {@link CommandAsyncService}，
 * 通过 Flux.create + onRequest 将异步 Redis 调用适配为 Reactor {@link Mono}。
 *
 * @author Nikita Koksharov
 *
 */
public class CommandReactiveService extends CommandAsyncService implements CommandReactiveExecutor {

    CommandReactiveService(CommandAsyncExecutor executor, boolean trackChanges) {
        super(executor, trackChanges);
    }

    /** 以 REACTIVE 引用类型构造。 */
    CommandReactiveService(ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder) {
        super(connectionManager, objectBuilder, RedissonObjectBuilder.ReferenceType.REACTIVE);
    }

    CommandReactiveService(CommandAsyncExecutor executor, ObjectParams objectParams) {
        super(executor, objectParams);
    }

    @Override
    public CommandReactiveExecutor copy(boolean trackChanges) {
        return new CommandReactiveService(this, trackChanges);
    }

    @Override
    public CommandReactiveExecutor copy(ObjectParams objectParams) {
        return new CommandReactiveService(this, objectParams);
    }

    /**
     * onRequest 时调用 supplier 获取 CompletionStage；
     * dispose 时 cancel Future；完成时 emit next + complete。
     */
    @Override
    public <R> Mono<R> reactive(Callable<CompletionStage<R>> supplier) {
        return Flux.<R>create(emitter -> {
            emitter.onRequest(n -> {
                CompletionStage<R> future;
                try {
                    future = supplier.call();
                } catch (Exception e) {
                    emitter.error(e);
                    return;
                }
                
                // 订阅取消时中断底层 Redis 操作
                emitter.onDispose(() -> {
                    future.toCompletableFuture().cancel(true);
                });

                future.whenComplete((v, e) -> {
                    if (e != null) {
                        //  unwrap CompletionException 便于下游处理
                        if (e instanceof CompletionException) {
                            e = e.getCause();
                        }
                        emitter.error(e);
                        return;
                    }

                    if (v != null) {
                        emitter.next(v);
                    }
                    emitter.complete();
                });
            });
        }).next();
    }

    }
